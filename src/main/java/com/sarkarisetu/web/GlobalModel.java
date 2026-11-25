package com.sarkarisetu.web;

import com.sarkarisetu.config.SiteProperties;
import com.sarkarisetu.model.Post;
import com.sarkarisetu.model.Section;
import com.sarkarisetu.repo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Adds the values every page needs (site config, menus, logged-in user). */
@ControllerAdvice
public class GlobalModel {
    private final SiteProperties site; private final SectionRepository sections; private final PageRepository pages;
    private final UserRepository users; private final PostRepository posts; private final MessageRepository messages;

    public GlobalModel(SiteProperties site, SectionRepository sections, PageRepository pages, UserRepository users, PostRepository posts, MessageRepository messages) {
        this.site = site; this.sections = sections; this.pages = pages; this.users = users; this.posts = posts; this.messages = messages;
    }

    @ModelAttribute
    public void common(Model m, HttpServletRequest req, Authentication auth) {
        m.addAttribute("site", site);
        m.addAttribute("canonical", site.getBaseUrl() + req.getRequestURI());
        List<Section> vis = sections.findByVisibleTrueOrderBySortOrderAsc();
        m.addAttribute("tabSections", vis.stream().filter(s -> s.getPlacement() == Section.Placement.TAB).toList());
        m.addAttribute("moreSections", vis.stream().filter(s -> s.getPlacement() == Section.Placement.MORE).toList());
        m.addAttribute("footerPages", pages.findByVisibleTrueAndInFooterTrue());
        m.addAttribute("filterTags", site.getFilterTagList());
        if (req.getRequestURI().startsWith("/admin") && auth != null && auth.isAuthenticated()) {
            users.findByEmailIgnoreCase(auth.getName()).ifPresent(u -> {
                m.addAttribute("me", u);
                m.addAttribute("isSa", u.isSuperAdmin());
            });
            List<Section> all = sections.findAllByOrderBySortOrderAsc();
            Map<String, Long> counts = new HashMap<>();
            for (Section s : all) counts.put(s.getSlug(), posts.countBySection(s));
            m.addAttribute("adminSections", all);
            m.addAttribute("counts", counts);
            m.addAttribute("unread", messages.countByHandledFalse());
        }
    }
}
