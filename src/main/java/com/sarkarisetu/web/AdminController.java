package com.sarkarisetu.web;

import com.sarkarisetu.model.*;
import com.sarkarisetu.repo.*;
import com.sarkarisetu.service.PostForm;
import com.sarkarisetu.service.PostService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository users; private final PostRepository posts; private final SectionRepository sections;
    private final PageRepository pages; private final ToolRepository tools; private final MessageRepository messages;
    private final SubscriberRepository subs; private final ActivityRepository activity; private final PostService ps; private final PasswordEncoder enc;

    public AdminController(UserRepository users, PostRepository posts, SectionRepository sections, PageRepository pages, ToolRepository tools,
                           MessageRepository messages, SubscriberRepository subs, ActivityRepository activity, PostService ps, PasswordEncoder enc) {
        this.users = users; this.posts = posts; this.sections = sections; this.pages = pages; this.tools = tools;
        this.messages = messages; this.subs = subs; this.activity = activity; this.ps = ps; this.enc = enc;
    }

    // ---------- helpers ----------
    private AppUser me(Authentication a) { return users.findByEmailIgnoreCase(a.getName()).orElseThrow(); }
    private boolean sa(Authentication a) { return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_SUPER_ADMIN")); }
    private void requireSa(Authentication a) { if (!sa(a)) throw new ResponseStatusException(HttpStatus.FORBIDDEN); }
    private void log(Authentication a, String text) { activity.save(new ActivityLog(me(a).getName(), text)); }
    private Post post(Long id) { return posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }

    // ---------- login, dashboard ----------
    @GetMapping("/login")
    public String login() { return "admin/login"; }

    @GetMapping({"", "/"})
    public String dashboard(Model m) {
        m.addAttribute("nPublished", posts.countByStatus(Post.Status.PUBLISHED));
        m.addAttribute("nDraft", posts.countByStatus(Post.Status.DRAFT));
        m.addAttribute("nUnpublished", posts.countByStatus(Post.Status.UNPUBLISHED));
        m.addAttribute("nSubs", subs.count());
        m.addAttribute("drafts", posts.findAll(Sort.by(Sort.Order.desc("updatedAt"))).stream()
                .filter(p -> p.getStatus() != Post.Status.PUBLISHED).limit(8).toList());
        m.addAttribute("activity", activity.findTop10ByOrderByHappenedAtDesc());
        m.addAttribute("top", posts.findByStatus(Post.Status.PUBLISHED, PageRequest.of(0, 5, Sort.by(Sort.Order.desc("views")))).getContent());
        LocalDate today = LocalDate.now();
        m.addAttribute("closing", posts.findTop8ByStatusAndLastDateBetweenOrderByLastDateAsc(Post.Status.PUBLISHED, today, today.plusDays(7)));
        List<Section> all = sections.findAllByOrderBySortOrderAsc();
        long max = 1;
        List<long[]> raw = new ArrayList<>();
        for (Section sec : all) { long n = posts.countBySectionAndStatus(sec, Post.Status.PUBLISHED); raw.add(new long[]{n}); max = Math.max(max, n); }
        List<Counted> bars = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) bars.add(new Counted(all.get(i).getName(), raw.get(i)[0], (int) (raw.get(i)[0] * 100 / max)));
        m.addAttribute("bars", bars);
        return "admin/dashboard";
    }

    // ---------- posts ----------
    @GetMapping("/posts")
    public String list(@RequestParam(required = false) String section, @RequestParam(required = false) String status,
                       @RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, Model m) {
        String term = q == null ? "" : q.trim().toLowerCase();
        List<Post> all = posts.findAll(Sort.by(Sort.Order.desc("updatedAt")));
        List<Post> filtered = all.stream()
                .filter(p -> section == null || section.isBlank() || (p.getSection() != null && p.getSection().getSlug().equals(section)))
                .filter(p -> status == null || status.isBlank() || p.getStatus().name().equalsIgnoreCase(status))
                .filter(p -> term.isEmpty() || (p.getTitle() != null && p.getTitle().toLowerCase().contains(term))).toList();
        int size = 15, pageNo = Math.max(page, 0), from = Math.min(pageNo * size, filtered.size());
        List<Post> slice = filtered.subList(from, Math.min(from + size, filtered.size()));
        m.addAttribute("list", slice);
        m.addAttribute("pageInfo", new PageImpl<>(slice, PageRequest.of(pageNo, size), filtered.size()));
        String base = "/admin/posts?" + (section == null || section.isBlank() ? "" : "section=" + section + "&")
                + (status == null || status.isBlank() ? "" : "status=" + status + "&") + (term.isEmpty() ? "" : "q=" + java.net.URLEncoder.encode(term, java.nio.charset.StandardCharsets.UTF_8) + "&");
        m.addAttribute("pagerBase", base);
        m.addAttribute("fSection", section == null ? "" : section);
        m.addAttribute("fStatus", status == null ? "" : status); m.addAttribute("fq", q == null ? "" : q);
        m.addAttribute("nAll", all.size());
        m.addAttribute("nPub", all.stream().filter(p -> p.getStatus() == Post.Status.PUBLISHED).count());
        m.addAttribute("nDra", all.stream().filter(p -> p.getStatus() == Post.Status.DRAFT).count());
        m.addAttribute("nUnp", all.stream().filter(p -> p.getStatus() == Post.Status.UNPUBLISHED).count());
        return "admin/posts";
    }

    @GetMapping("/posts/new")
    public String neu(Model m) {
        m.addAttribute("form", new PostForm()); m.addAttribute("status", "NEW"); m.addAttribute("allSections", sections.findAllByOrderBySortOrderAsc());
        return "admin/post-form";
    }

    @GetMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id, Model m) {
        Post p = post(id);
        m.addAttribute("form", ps.toForm(p)); m.addAttribute("status", p.getStatus().name()); m.addAttribute("postSlug", p.getSlug());
        m.addAttribute("allSections", sections.findAllByOrderBySortOrderAsc());
        return "admin/post-form";
    }

    @PostMapping("/posts/save")
    public String save(PostForm f, Authentication a, RedirectAttributes ra) {
        if (f.getTitle() == null || f.getTitle().isBlank()) { ra.addFlashAttribute("err", "Title is required."); return f.getId() == null ? "redirect:/admin/posts/new" : "redirect:/admin/posts/" + f.getId() + "/edit"; }
        Post p = f.getId() == null ? new Post() : post(f.getId());
        if (p.getId() == null) p.setAuthor(me(a));
        ps.apply(f, p);
        String msg = "Saved.";
        if ("publish".equals(f.getAction())) {
            if (f.allChecked() && p.getSection() != null) {
                p.setStatus(Post.Status.PUBLISHED);
                if (p.getPublishedAt() == null) p.setPublishedAt(LocalDateTime.now());
                p.setVerified(true); p.setVerifiedBy(me(a).getName());
                msg = "Published.";
            } else ra.addFlashAttribute("err", "Not published: choose a section and tick all 5 verification checks. Saved as " + p.getStatus().name().toLowerCase() + ".");
        }
        posts.save(p);
        log(a, msg.equals("Published.") ? "published " + p.getTitle() : "saved " + p.getTitle());
        if (!ra.getFlashAttributes().containsKey("err")) ra.addFlashAttribute("ok", msg);
        return "redirect:/admin/posts/" + p.getId() + "/edit";
    }

    @PostMapping("/posts/{id}/publish")
    public String publish(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        Post p = post(id);
        if (!p.isVerified() || p.getSection() == null) { ra.addFlashAttribute("err", "Open the editor and complete the 5 verification checks first."); return "redirect:/admin/posts"; }
        p.setStatus(Post.Status.PUBLISHED);
        if (p.getPublishedAt() == null) p.setPublishedAt(LocalDateTime.now());
        posts.save(p); log(a, "published " + p.getTitle()); ra.addFlashAttribute("ok", "Published.");
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/unpublish")
    public String unpublish(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        Post p = post(id); p.setStatus(Post.Status.UNPUBLISHED); posts.save(p);
        log(a, "unpublished " + p.getTitle()); ra.addFlashAttribute("ok", "Unpublished.");
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/duplicate")
    public String duplicate(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        Post p = post(id);
        Post c = new Post();
        c.setTitle(p.getTitle() + " (copy)"); c.setSlug(ps.uniqueSlug(p.getSlug() + "-copy", p.getTitle(), null));
        c.setSection(p.getSection()); c.setOrganisation(p.getOrganisation()); c.setTotalPosts(p.getTotalPosts());
        c.setLastDate(p.getLastDate()); c.setTags(p.getTags()); c.setSummary(p.getSummary()); c.setBody(p.getBody());
        c.setStatus(Post.Status.DRAFT); c.setAuthor(me(a));
        for (Post.Fee f : p.getFees()) c.getFees().add(new Post.Fee(f.getCategory(), f.getAmount(), f.getNote()));
        for (Post.Extra x : p.getExtras()) c.getExtras().add(new Post.Extra(x.getLabel(), x.getDetail()));
        for (Post.Stage x : p.getStages()) c.getStages().add(new Post.Stage(x.getName(), x.getAppliesTo(), x.getWhenText(), x.getState(), x.getLink()));
        for (Post.Vacancy v : p.getVacancies()) c.getVacancies().add(new Post.Vacancy(v.getPostName(), v.getUr(), v.getObc(), v.getSc(), v.getSt(), v.getEws()));
        posts.save(c); log(a, "duplicated " + p.getTitle());
        ra.addFlashAttribute("ok", "Copy created as a draft. Edit it and publish.");
        return "redirect:/admin/posts/" + c.getId() + "/edit";
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        requireSa(a);
        Post p = post(id); log(a, "deleted " + p.getTitle()); posts.delete(p); ra.addFlashAttribute("ok", "Deleted.");
        return "redirect:/admin/posts";
    }

    @GetMapping("/posts/{id}/preview")
    public String preview(@PathVariable Long id, Model m) {
        m.addAttribute("post", post(id)); m.addAttribute("preview", true); m.addAttribute("related", List.of());
        return "post";
    }

    // ---------- sections (Super Admin) ----------
    @GetMapping("/sections")
    public String sectionsPage(Model m) {
        m.addAttribute("section", new Section());
        m.addAttribute("kinds", Section.Kind.values());
        m.addAttribute("placements", Section.Placement.values());
        return "admin/sections";
    }

    @PostMapping("/sections/save")
    public String sectionSave(@ModelAttribute Section s, Authentication a, RedirectAttributes ra) {
        s.setSlug(PostService.slugify(s.getSlug() == null || s.getSlug().isBlank() ? s.getName() : s.getSlug()));
        if (s.getName() == null || s.getName().isBlank() || s.getSlug().isEmpty()) { ra.addFlashAttribute("err", "Name is required."); return "redirect:/admin/sections"; }
        try { sections.save(s); log(a, "saved section " + s.getName()); ra.addFlashAttribute("ok", "Section saved."); }
        catch (DataIntegrityViolationException e) { ra.addFlashAttribute("err", "That slug is already used."); }
        return "redirect:/admin/sections";
    }

    @PostMapping("/sections/{id}/delete")
    public String sectionDelete(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        Section s = sections.findById(id).orElseThrow();
        if (posts.countBySection(s) > 0) ra.addFlashAttribute("err", "This section still has posts. Hide it instead.");
        else { sections.delete(s); log(a, "deleted section " + s.getName()); ra.addFlashAttribute("ok", "Section deleted."); }
        return "redirect:/admin/sections";
    }

    // ---------- pages (Super Admin) ----------
    @GetMapping("/pages")
    public String pagesList(Model m) { m.addAttribute("list", pages.findAllByOrderByTitleAsc()); return "admin/pages"; }

    @GetMapping("/pages/new")
    public String pageNew(Model m) { m.addAttribute("staticPage", new StaticPage()); return "admin/page-form"; }

    @GetMapping("/pages/{id}/edit")
    public String pageEdit(@PathVariable Long id, Model m) { m.addAttribute("staticPage", pages.findById(id).orElseThrow()); return "admin/page-form"; }

    @PostMapping("/pages/save")
    public String pageSave(@ModelAttribute StaticPage sp, Authentication a, RedirectAttributes ra) {
        sp.setSlug(PostService.slugify(sp.getSlug() == null || sp.getSlug().isBlank() ? sp.getTitle() : sp.getSlug()));
        try { pages.save(sp); log(a, "saved page " + sp.getTitle()); ra.addFlashAttribute("ok", "Page saved."); }
        catch (DataIntegrityViolationException e) { ra.addFlashAttribute("err", "That slug is already used."); }
        return "redirect:/admin/pages";
    }

    @PostMapping("/pages/{id}/delete")
    public String pageDelete(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        StaticPage sp = pages.findById(id).orElseThrow(); pages.delete(sp); log(a, "deleted page " + sp.getTitle()); ra.addFlashAttribute("ok", "Page deleted.");
        return "redirect:/admin/pages";
    }

    // ---------- tools (Super Admin) ----------
    @GetMapping("/tools")
    public String toolsList(Model m) { m.addAttribute("list", tools.findAllByOrderBySortOrderAsc()); return "admin/tools"; }

    @PostMapping("/tools/save")
    public String toolSave(@RequestParam Long id, @RequestParam String name, @RequestParam(defaultValue = "") String description,
                           @RequestParam(defaultValue = "10") int sortOrder, @RequestParam(defaultValue = "false") boolean enabled,
                           Authentication a, RedirectAttributes ra) {
        Tool t = tools.findById(id).orElseThrow();
        t.setName(name); t.setDescription(description); t.setSortOrder(sortOrder); t.setEnabled(enabled);
        tools.save(t); log(a, "updated tool " + t.getName()); ra.addFlashAttribute("ok", "Tool saved.");
        return "redirect:/admin/tools";
    }

    // ---------- messages ----------
    @GetMapping("/messages")
    public String messagesList(Model m) { m.addAttribute("list", messages.findAllByOrderByCreatedAtDesc()); return "admin/messages"; }

    @PostMapping("/messages/{id}/handled")
    public String messageHandled(@PathVariable Long id) {
        ContactMessage c = messages.findById(id).orElseThrow(); c.setHandled(!c.isHandled()); messages.save(c);
        return "redirect:/admin/messages";
    }

    // ---------- subscribers (Super Admin) ----------
    @GetMapping("/subscribers")
    public String subscribersList(Model m) { m.addAttribute("list", subs.findAllByOrderByCreatedAtDesc()); return "admin/subscribers"; }

    @GetMapping("/subscribers/export.csv")
    public ResponseEntity<String> subscribersCsv() {
        StringBuilder sb = new StringBuilder("email,subscribed\n");
        for (Subscriber s : subs.findAllByOrderByCreatedAtDesc()) sb.append(s.getEmail().replace(",", "")).append(',').append(s.getCreatedText()).append('\n');
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subscribers.csv")
                .contentType(MediaType.parseMediaType("text/csv")).body(sb.toString());
    }

    // ---------- users (Super Admin) ----------
    @GetMapping("/users")
    public String usersList(Model m) { m.addAttribute("list", users.findAll(Sort.by("name"))); m.addAttribute("activity", activity.findTop10ByOrderByHappenedAtDesc()); return "admin/users"; }

    @PostMapping("/users/save")
    public String userSave(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                           @RequestParam AppUser.Role role, Authentication a, RedirectAttributes ra) {
        if (name.isBlank() || !email.contains("@") || password.length() < 8) { ra.addFlashAttribute("err", "Name, valid email and a password of at least 8 characters are required."); return "redirect:/admin/users"; }
        if (users.findByEmailIgnoreCase(email.trim()).isPresent()) { ra.addFlashAttribute("err", "That email already has an account."); return "redirect:/admin/users"; }
        AppUser u = new AppUser(); u.setName(name.trim()); u.setEmail(email.trim()); u.setRole(role); u.setPasswordHash(enc.encode(password));
        users.save(u); log(a, "added user " + u.getName() + " (" + u.getRoleLabel() + ")"); ra.addFlashAttribute("ok", "User created.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String userToggle(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        AppUser u = users.findById(id).orElseThrow();
        if (u.getEmail().equalsIgnoreCase(a.getName())) { ra.addFlashAttribute("err", "You cannot disable your own account."); return "redirect:/admin/users"; }
        u.setEnabled(!u.isEnabled()); users.save(u); log(a, (u.isEnabled() ? "enabled " : "disabled ") + u.getName());
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/password")
    public String userPassword(@PathVariable Long id, @RequestParam String newPassword, Authentication a, RedirectAttributes ra) {
        if (newPassword.length() < 8) { ra.addFlashAttribute("err", "Password must be at least 8 characters."); return "redirect:/admin/users"; }
        AppUser u = users.findById(id).orElseThrow(); u.setPasswordHash(enc.encode(newPassword)); users.save(u);
        log(a, "reset password for " + u.getName()); ra.addFlashAttribute("ok", "Password updated.");
        return "redirect:/admin/users";
    }

    // ---------- trending (Super Admin) ----------
    @GetMapping("/trending")
    public String trending(Model m) {
        m.addAttribute("list", posts.findAll(Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("views"))).stream()
                .filter(p -> p.getStatus() == Post.Status.PUBLISHED).limit(20).toList());
        return "admin/trending";
    }

    @PostMapping("/trending/{id}/pin")
    public String pin(@PathVariable Long id, Authentication a) {
        Post p = post(id); p.setPinned(!p.isPinned()); posts.save(p); log(a, (p.isPinned() ? "pinned " : "unpinned ") + p.getTitle());
        return "redirect:/admin/trending";
    }

    // ---------- settings, profile ----------
    @GetMapping("/settings")
    public String settings() { return "admin/settings"; }

    @GetMapping("/profile")
    public String profile() { return "admin/profile"; }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String current, @RequestParam String newPassword, Authentication a, RedirectAttributes ra) {
        AppUser u = me(a);
        if (!enc.matches(current, u.getPasswordHash())) ra.addFlashAttribute("err", "Current password is wrong.");
        else if (newPassword.length() < 8) ra.addFlashAttribute("err", "New password must be at least 8 characters.");
        else { u.setPasswordHash(enc.encode(newPassword)); users.save(u); ra.addFlashAttribute("ok", "Password changed."); }
        return "redirect:/admin/profile";
    }
}
