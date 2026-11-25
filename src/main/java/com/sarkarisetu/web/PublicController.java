package com.sarkarisetu.web;

import com.sarkarisetu.config.SiteProperties;
import com.sarkarisetu.model.*;
import com.sarkarisetu.repo.*;
import com.sarkarisetu.service.MockTestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class PublicController {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String[] PALETTE = {"#1d3fbf", "#0b8a5f", "#e8590c", "#7c3aed", "#0e7490", "#be185d", "#475569", "#b91c1c"};

    private final PostRepository posts; private final SectionRepository sections; private final ToolRepository tools;
    private final PageRepository pages; private final MessageRepository messages; private final SubscriberRepository subs;
    private final SiteProperties site; private final MockTestService mocks;

    public PublicController(PostRepository posts, SectionRepository sections, ToolRepository tools, PageRepository pages,
                            MessageRepository messages, SubscriberRepository subs, SiteProperties site, MockTestService mocks) {
        this.posts = posts; this.sections = sections; this.tools = tools; this.pages = pages; this.messages = messages;
        this.subs = subs; this.site = site; this.mocks = mocks;
    }

    // ---------- small helper classes used by the templates ----------
    public static class Block {
        private final Section section; private final List<Post> posts; private final String color;
        public Block(Section s, List<Post> p, String c) { section = s; posts = p; color = c; }
        public Section getSection() { return section; } public List<Post> getPosts() { return posts; } public String getColor() { return color; }
    }
    public static class Stat {
        private final String label; private final long count;
        public Stat(String l, long c) { label = l; count = c; }
        public String getLabel() { return label; } public long getCount() { return count; }
    }
    public static class Cat {
        private final String name, href, color, icon;
        public Cat(String n, String h, String c, String i) { name = n; href = h; color = c; icon = i; }
        public String getName() { return name; } public String getHref() { return href; } public String getColor() { return color; } public String getIcon() { return icon; }
    }

    private static ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND); }
    private static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    private static String colorFor(List<Section> all, Section s) {
        for (int i = 0; i < all.size(); i++) if (all.get(i).getId().equals(s.getId())) return PALETTE[i % PALETTE.length];
        return PALETTE[0];
    }
    private static String icon(String slug) {
        switch (slug) {
            case "jobs": return "M3 8h18v12H3zM9 8V5h6v3";
            case "result": return "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM8 12l3 3 5-6";
            case "answer-key": return "M9 6h11M9 12h11M9 18h11M4 6l1 1 2-2M4 12l1 1 2-2M4 18l1 1 2-2";
            case "syllabus": return "M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2zM4 19a2 2 0 0 1 2-2h13";
            case "admission": return "M3 10l9-6 9 6M5 10v9M19 10v9M9 19v-5h6v5M3 19h18";
            case "scheme": return "M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z";
            default: return "M5 3h14v18H5zM9 8h6M9 12h6M9 16h4";
        }
    }
    private Pageable pg(int page, int size, String field) { return PageRequest.of(Math.max(page, 0), size, Sort.by(Sort.Order.desc(field))); }
    private List<Post> latest(Section s, int n) { return posts.findBySectionAndStatus(s, Post.Status.PUBLISHED, pg(0, n, "publishedAt")).getContent(); }

    // ---------- home ----------
    @GetMapping("/")
    public String home(Model m) {
        Pageable top = PageRequest.of(0, Math.max(site.getTrendingCount(), 1), Sort.by(Sort.Order.desc("pinned"), Sort.Order.desc("views")));
        m.addAttribute("trending", posts.findByStatus(Post.Status.PUBLISHED, top).getContent());
        List<Section> vis = sections.findByVisibleTrueOrderBySortOrderAsc();
        List<Block> blocks = new ArrayList<>(), extra = new ArrayList<>();
        List<Stat> stats = new ArrayList<>();
        List<Cat> cats = new ArrayList<>();
        for (Section s : vis) {
            if (s.getPlacement() == Section.Placement.TAB) {
                blocks.add(new Block(s, latest(s, 5), colorFor(vis, s)));
                stats.add(new Stat(s.getName(), posts.countBySectionAndStatus(s, Post.Status.PUBLISHED)));
            }
            if (cats.size() < 6) cats.add(new Cat(s.getName(), "/s/" + s.getSlug(), colorFor(vis, s), icon(s.getSlug())));
        }
        cats.add(new Cat("Mock Tests", "/mock-tests", "#b91c1c", "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM12 7v5l3 2"));
        cats.add(new Cat("Tools", "/tools", "#475569", "M14.7 6.3a4 4 0 0 0-5.4 5.4L3 18l3 3 6.3-6.3a4 4 0 0 0 5.4-5.4l-3 3-2.4-.6-.6-2.4z"));
        for (String slug : site.getHomeSectionList())
            sections.findBySlug(slug).filter(Section::isVisible).ifPresent(s -> extra.add(new Block(s, latest(s, 6), colorFor(vis, s))));
        m.addAttribute("blocks", blocks); m.addAttribute("extraBlocks", extra); m.addAttribute("stats", stats); m.addAttribute("cats", cats);
        LocalDate today = LocalDate.now();
        m.addAttribute("closing", posts.findTop8ByStatusAndLastDateBetweenOrderByLastDateAsc(Post.Status.PUBLISHED, today, today.plusDays(7)));
        m.addAttribute("blogPosts", sections.findBySlug("blog").map(b -> latest(b, 3)).orElse(List.of()));
        pages.findBySlugAndVisibleTrue("faq").ifPresent(f -> m.addAttribute("faq", fill(f.getBody())));
        m.addAttribute("metaDescription", site.getTagline());
        return "index";
    }

    private String fill(String body) {
        return body == null ? "" : body.replace("{site}", site.getName()).replace("{domain}", site.getBaseUrl())
                .replace("{email}", site.getContactEmail()).replace("{phone}", site.getPhone()).replace("{address}", site.getAddress());
    }

    // ---------- section list ----------
    @GetMapping("/s/{slug}")
    public String section(@PathVariable String slug, @RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String tag, @RequestParam(defaultValue = "latest") String sort, Model m) {
        Section s = sections.findBySlug(slug).filter(Section::isVisible).orElseThrow(PublicController::notFound);
        Sort so = "views".equals(sort) ? Sort.by(Sort.Order.desc("views"))
                : "oldest".equals(sort) ? Sort.by(Sort.Order.asc("publishedAt")) : Sort.by(Sort.Order.desc("publishedAt"));
        Pageable p = PageRequest.of(Math.max(page, 0), site.getPageSize(), so);
        boolean hasTag = tag != null && !tag.isBlank();
        Page<Post> result = hasTag
                ? posts.findBySectionAndStatusAndTagsContainingIgnoreCase(s, Post.Status.PUBLISHED, tag.trim(), p)
                : posts.findBySectionAndStatus(s, Post.Status.PUBLISHED, p);
        String sortPart = "latest".equals(sort) ? "" : "sort=" + enc(sort) + "&";
        String tagPart = hasTag ? "tag=" + enc(tag.trim()) + "&" : "";
        m.addAttribute("section", s); m.addAttribute("page", result); m.addAttribute("tag", hasTag ? tag.trim() : "");
        m.addAttribute("sort", sort);
        m.addAttribute("pagerBase", "/s/" + slug + "?" + tagPart + sortPart);
        m.addAttribute("tagBase", "/s/" + slug + "?" + sortPart);
        m.addAttribute("sortBase", "/s/" + slug + "?" + tagPart);
        m.addAttribute("metaDescription", s.getName() + " on " + site.getName());
        return "list";
    }

    // ---------- post ----------
    @GetMapping("/post/{slug}")
    public String post(@PathVariable String slug, Model m) {
        Post p = posts.findBySlugAndStatus(slug, Post.Status.PUBLISHED).orElseThrow(PublicController::notFound);
        posts.addView(p.getId());
        m.addAttribute("post", p); m.addAttribute("preview", false);
        m.addAttribute("related", p.getSection() == null ? List.of()
                : posts.findTop5BySectionAndStatusAndIdNotOrderByPublishedAtDesc(p.getSection(), Post.Status.PUBLISHED, p.getId()));
        m.addAttribute("metaDescription", p.getSummary());
        m.addAttribute("ogTitle", p.getTitle());
        return "post";
    }

    // ---------- search ----------
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, Model m) {
        String term = q.trim();
        Page<Post> result = term.isEmpty() ? Page.empty()
                : posts.findByStatusAndTitleContainingIgnoreCase(Post.Status.PUBLISHED, term, pg(page, site.getPageSize(), "publishedAt"));
        m.addAttribute("q", term); m.addAttribute("page", result);
        m.addAttribute("pagerBase", "/search?q=" + enc(term) + "&");
        m.addAttribute("noindex", true);
        return "search";
    }

    /** Small JSON list for the search box suggestions. */
    @GetMapping(value = "/search/suggest", produces = "application/json")
    @ResponseBody
    public String suggest(@RequestParam(defaultValue = "") String q) {
        String t = q.trim();
        if (t.length() < 2) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (Post p : posts.findByStatusAndTitleContainingIgnoreCase(Post.Status.PUBLISHED, t, PageRequest.of(0, 6, Sort.by(Sort.Order.desc("views")))).getContent()) {
            if (sb.length() > 1) sb.append(',');
            sb.append("{\"t\":\"").append(json(p.getTitle())).append("\",\"u\":\"/post/").append(p.getSlug()).append("\"}");
        }
        return sb.append("]").toString();
    }
    private static String json(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " "); }

    // ---------- menus, tools, pages ----------
    @GetMapping("/more")
    public String more(Model m) { m.addAttribute("tools", tools.findByEnabledTrueOrderBySortOrderAsc()); return "more"; }

    @GetMapping("/saved")
    public String saved(Model m) { m.addAttribute("noindex", true); return "saved"; }

    @GetMapping("/tools")
    public String toolsIndex(Model m) { m.addAttribute("tools", tools.findByEnabledTrueOrderBySortOrderAsc()); return "tools/index"; }

    @GetMapping("/tools/{slug}")
    public String tool(@PathVariable String slug, Model m) {
        Tool t = tools.findBySlug(slug).filter(Tool::isEnabled).orElseThrow(PublicController::notFound);
        m.addAttribute("tool", t); m.addAttribute("metaDescription", t.getDescription());
        return "tools/" + t.getSlug();
    }

    @GetMapping("/p/{slug}")
    public String page(@PathVariable String slug, Model m) {
        StaticPage sp = pages.findBySlugAndVisibleTrue(slug).orElseThrow(PublicController::notFound);
        m.addAttribute("pageTitle", sp.getTitle()); m.addAttribute("pageBody", fill(sp.getBody()));
        return "page";
    }

    // ---------- contact and subscribe ----------
    @GetMapping("/contact")
    public String contact() { return "contact"; }

    @PostMapping("/contact")
    public String send(@RequestParam String name, @RequestParam String email, @RequestParam(defaultValue = "") String subject,
                       @RequestParam String message, @RequestParam(required = false) String website, RedirectAttributes ra) {
        if (website != null && !website.isBlank()) { ra.addFlashAttribute("ok", "Thanks, your message was sent."); return "redirect:/contact"; } // spam trap
        if (name.isBlank() || message.isBlank() || !EMAIL.matcher(email.trim()).matches()) {
            ra.addFlashAttribute("err", "Please fill name, a valid email and a message."); return "redirect:/contact";
        }
        ContactMessage c = new ContactMessage();
        c.setName(cut(name, 100)); c.setEmail(cut(email, 150)); c.setSubject(cut(subject, 150)); c.setMessage(cut(message, 3000));
        messages.save(c);
        ra.addFlashAttribute("ok", "Thanks, your message was sent.");
        return "redirect:/contact";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam String email, RedirectAttributes ra) {
        String e = email.trim();
        if (!EMAIL.matcher(e).matches()) ra.addFlashAttribute("subMsg", "Please enter a valid email.");
        else {
            if (!subs.existsByEmailIgnoreCase(e)) { Subscriber s = new Subscriber(); s.setEmail(cut(e, 150)); subs.save(s); }
            ra.addFlashAttribute("subMsg", "You are subscribed. Thank you!");
        }
        return "redirect:/#alerts";
    }

    // ---------- sitemap, robots, feed ----------
    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    @ResponseBody
    public String sitemap() {
        String b = site.getBaseUrl();
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        x.append(url(b + "/")).append(url(b + "/contact")).append(url(b + "/mock-tests")).append(url(b + "/tools"));
        for (Section s : sections.findByVisibleTrueOrderBySortOrderAsc()) x.append(url(b + "/s/" + s.getSlug()));
        for (Tool t : tools.findByEnabledTrueOrderBySortOrderAsc()) x.append(url(b + "/tools/" + t.getSlug()));
        for (MockTestService.Test t : mocks.all()) x.append(url(b + "/mock-tests/" + t.getSlug()));
        for (StaticPage p : pages.findByVisibleTrueAndInFooterTrue()) x.append(url(b + "/p/" + p.getSlug()));
        for (Post p : posts.findByStatusOrderByPublishedAtDesc(Post.Status.PUBLISHED)) x.append(url(b + "/post/" + p.getSlug()));
        return x.append("</urlset>").toString();
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain")
    @ResponseBody
    public String robots() { return "User-agent: *\nDisallow: /admin/\nDisallow: /search\nSitemap: " + site.getBaseUrl() + "/sitemap.xml\n"; }

    @GetMapping(value = "/feed.xml", produces = "application/rss+xml")
    @ResponseBody
    public String feed() {
        String b = site.getBaseUrl();
        StringBuilder x = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rss version=\"2.0\"><channel>");
        x.append("<title>").append(esc(site.getName())).append("</title><link>").append(esc(b)).append("</link><description>")
         .append(esc(site.getTagline())).append("</description>");
        for (Post p : posts.findByStatus(Post.Status.PUBLISHED, pg(0, 30, "publishedAt")).getContent()) {
            x.append("<item><title>").append(esc(p.getTitle())).append("</title><link>").append(esc(b + "/post/" + p.getSlug()))
             .append("</link><guid>").append(esc(b + "/post/" + p.getSlug())).append("</guid>");
            if (p.getPublishedAt() != null)
                x.append("<pubDate>").append(p.getPublishedAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("</pubDate>");
            if (p.getSummary() != null && !p.getSummary().isBlank()) x.append("<description>").append(esc(p.getSummary())).append("</description>");
            x.append("</item>");
        }
        return x.append("</channel></rss>").toString();
    }

    private static String url(String loc) { return "<url><loc>" + esc(loc) + "</loc></url>\n"; }
    private static String esc(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
    private static String cut(String s, int n) { String t = s == null ? "" : s.trim(); return t.length() > n ? t.substring(0, n) : t; }
}
