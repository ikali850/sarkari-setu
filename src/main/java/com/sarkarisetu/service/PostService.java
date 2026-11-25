package com.sarkarisetu.service;

import com.sarkarisetu.model.Post;
import com.sarkarisetu.repo.PostRepository;
import com.sarkarisetu.repo.SectionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/** Converts between a Post and the editor form (text lines to table rows and back). */
@Service
public class PostService {
    private final PostRepository posts;
    private final SectionRepository sections;

    public PostService(PostRepository posts, SectionRepository sections) { this.posts = posts; this.sections = sections; }

    public static String slugify(String s) {
        if (s == null) return "";
        String r = s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
        return r.length() > 120 ? r.substring(0, 120) : r;
    }

    public String uniqueSlug(String wanted, String title, Long selfId) {
        String base = slugify(wanted == null || wanted.isBlank() ? title : wanted);
        if (base.isEmpty()) base = "post";
        String slug = base;
        int i = 2;
        while (true) {
            var found = posts.findBySlug(slug);
            if (found.isEmpty() || (selfId != null && found.get().getId().equals(selfId))) return slug;
            slug = base + "-" + i++;
        }
    }

    private static List<String[]> rows(String text, String splitRegex) {
        List<String[]> out = new ArrayList<>();
        if (text == null) return out;
        for (String line : text.split("\\R")) {
            if (line.isBlank()) continue;
            out.add(line.trim().split(splitRegex, -1));
        }
        return out;
    }
    private static String at(String[] a, int i) { return i < a.length ? a[i].trim() : ""; }
    private static int num(String s) {
        try { return Integer.parseInt(s.replace(",", "").trim()); } catch (Exception e) { return 0; }
    }
    private static String nz(String s) { return s == null ? "" : s; }

    public PostForm toForm(Post p) {
        PostForm f = new PostForm();
        f.setId(p.getId());
        f.setSectionId(p.getSection() == null ? null : p.getSection().getId());
        f.setTitle(p.getTitle()); f.setSlug(p.getSlug()); f.setOrganisation(p.getOrganisation());
        f.setTotalPosts(p.getTotalPosts()); f.setTags(p.getTags()); f.setSummary(p.getSummary()); f.setBody(p.getBody());
        f.setLastDate(p.getLastDate() == null ? "" : p.getLastDate().toString());
        StringBuilder sb = new StringBuilder();
        for (Post.Fee x : p.getFees()) sb.append(nz(x.getCategory())).append(" | ").append(nz(x.getAmount())).append(" | ").append(nz(x.getNote())).append("\n");
        f.setFeesText(sb.toString()); sb.setLength(0);
        for (Post.Extra x : p.getExtras()) sb.append(nz(x.getLabel())).append(" | ").append(nz(x.getDetail())).append("\n");
        f.setExtraText(sb.toString()); sb.setLength(0);
        for (Post.Vacancy v : p.getVacancies())
            sb.append(nz(v.getPostName())).append(" | ").append(v.getUr()).append(" | ").append(v.getObc()).append(" | ")
              .append(v.getSc()).append(" | ").append(v.getSt()).append(" | ").append(v.getEws()).append("\n");
        f.setVacancyText(sb.toString()); sb.setLength(0);
        for (Post.Stage s : p.getStages())
            sb.append(nz(s.getName())).append(" | ").append(nz(s.getAppliesTo())).append(" | ").append(nz(s.getWhenText())).append(" | ")
              .append(nz(s.getState())).append(" | ").append(nz(s.getLink())).append("\n");
        f.setStagesText(sb.toString());
        return f;
    }

    public void apply(PostForm f, Post p) {
        p.setTitle(nz(f.getTitle()).trim());
        p.setSlug(uniqueSlug(f.getSlug(), f.getTitle(), p.getId()));
        p.setSection(f.getSectionId() == null ? null : sections.findById(f.getSectionId()).orElse(null));
        p.setOrganisation(nz(f.getOrganisation()).trim());
        p.setTotalPosts(nz(f.getTotalPosts()).trim());
        p.setTags(nz(f.getTags()).trim());
        String sum = nz(f.getSummary()).trim();
        p.setSummary(sum.length() > 500 ? sum.substring(0, 500) : sum);
        p.setBody(nz(f.getBody()));
        try { p.setLastDate(f.getLastDate() == null || f.getLastDate().isBlank() ? null : LocalDate.parse(f.getLastDate())); }
        catch (DateTimeParseException e) { p.setLastDate(null); }

        p.getFees().clear();
        for (String[] r : rows(f.getFeesText(), "\\s*\\|\\s*"))
            if (!at(r, 0).isEmpty()) p.getFees().add(new Post.Fee(at(r, 0), at(r, 1), at(r, 2)));
        p.getExtras().clear();
        for (String[] r : rows(f.getExtraText(), "\\s*\\|\\s*"))
            if (!at(r, 0).isEmpty()) p.getExtras().add(new Post.Extra(at(r, 0), at(r, 1)));
        p.getVacancies().clear();
        for (String[] r : rows(f.getVacancyText(), "\\s*[|\\t,]\\s*"))
            if (!at(r, 0).isEmpty()) p.getVacancies().add(new Post.Vacancy(at(r, 0), num(at(r, 1)), num(at(r, 2)), num(at(r, 3)), num(at(r, 4)), num(at(r, 5))));
        p.getStages().clear();
        for (String[] r : rows(f.getStagesText(), "\\s*\\|\\s*"))
            if (!at(r, 0).isEmpty()) p.getStages().add(new Post.Stage(at(r, 0), at(r, 1).isEmpty() ? "All posts" : at(r, 1), at(r, 2), at(r, 3).isEmpty() ? "Upcoming" : at(r, 3), at(r, 4)));
    }
}
