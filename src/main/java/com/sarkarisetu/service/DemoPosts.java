package com.sarkarisetu.service;

import com.sarkarisetu.model.AppUser;
import com.sarkarisetu.model.Post;
import com.sarkarisetu.model.Section;
import com.sarkarisetu.repo.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads the sample posts from demo-data.txt (see the format at the top of that file). */
@Component
public class DemoPosts {
    private static final Logger log = LoggerFactory.getLogger(DemoPosts.class);
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final Pattern TOKEN = Pattern.compile("\\{d([+-]\\d+)\\}");

    private final PostRepository posts;
    private final Set<String> used = new HashSet<>();
    @Value("${site.demo-file:./demo-data.txt}")
    private String externalFile;

    public DemoPosts(PostRepository posts) { this.posts = posts; }

    public int seed(Map<String, Section> sections, AppUser author) {
        int n = 0, lineNo = 0;
        for (String line : readLines()) {
            lineNo++;
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] c = line.split("\\|", -1);
            if (c.length < 14) { log.warn("demo-data line {} skipped: needs 14 columns, found {}", lineNo, c.length); continue; }
            Section sec = sections.get(c[0].trim());
            if (sec == null) { log.warn("demo-data line {} skipped: unknown section '{}'", lineNo, c[0]); continue; }
            try {
                posts.save(build(c, sec, author, n));
                n++;
            } catch (RuntimeException e) {
                log.warn("demo-data line {} skipped: {}", lineNo, e.getMessage());
            }
        }
        return n;
    }

    private Post build(String[] c, Section sec, AppUser a, int index) {
        Post p = new Post();
        p.setTitle(c[1].trim()); p.setSection(sec); p.setAuthor(a);
        p.setOrganisation(c[2].trim()); p.setTags(c[3].trim()); p.setTotalPosts(c[4].trim());
        if (!c[5].isBlank()) p.setLastDate(LocalDate.now().plusDays(Long.parseLong(c[5].trim())));
        Post.Status st = Post.Status.valueOf(c[6].trim().toUpperCase());
        p.setStatus(st);
        if (st != Post.Status.DRAFT) {
            p.setVerified(true); p.setVerifiedBy(a.getName());
            p.setPublishedAt(LocalDateTime.now().minusDays(Long.parseLong(c[8].trim())).minusHours((index * 5L) % 24));
            p.setViews(Long.parseLong(c[7].trim()));
        }
        for (String[] r : rows(c[9])) p.getFees().add(new Post.Fee(r[0], at(r, 1), at(r, 2)));
        for (String[] r : rows(c[10])) p.getExtras().add(new Post.Extra(r[0], at(r, 1)));
        for (String[] r : rows(c[11])) p.getVacancies().add(new Post.Vacancy(r[0], num(at(r, 1)), num(at(r, 2)), num(at(r, 3)), num(at(r, 4)), num(at(r, 5))));
        for (String[] r : rows(c[12])) p.getStages().add(new Post.Stage(r[0], at(r, 1), at(r, 2), at(r, 3), at(r, 4)));
        p.setSummary(dates(c[13]).trim());
        p.setBody("<p>This is <b>sample content</b> for " + p.getTitle() + ". Replace it with your own write-up from Admin.</p>"
                + "<h2>Selection process</h2><p>Written exam, then document verification and medical test, as per the official notification.</p>"
                + "<h2>How to apply</h2><ol><li>Open the official website.</li><li>Register and fill the form.</li>"
                + "<li>Upload your photo and signature.</li><li>Pay the fee and print the form.</li></ol>");
        String base = PostService.slugify(p.getTitle()), slug = base;
        int i = 2;
        while (!used.add(slug)) slug = base + "-" + i++;
        p.setSlug(slug);
        return p;
    }

    private static List<String[]> rows(String col) {
        List<String[]> out = new java.util.ArrayList<>();
        for (String part : dates(col).split(";")) {
            if (part.isBlank()) continue;
            String[] r = part.split("~", -1);
            for (int k = 0; k < r.length; k++) r[k] = r[k].trim();
            out.add(r);
        }
        return out;
    }
    private static String at(String[] r, int i) { return i < r.length ? r[i] : ""; }
    private static int num(String s) { try { return Integer.parseInt(s.replace(",", "")); } catch (Exception e) { return 0; } }

    /** Replaces {d-7} or {d+10} with a real date relative to today. */
    private static String dates(String s) {
        Matcher m = TOKEN.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) m.appendReplacement(sb, F.format(LocalDate.now().plusDays(Long.parseLong(m.group(1)))));
        m.appendTail(sb);
        return sb.toString();
    }

    private List<String> readLines() {
        try {
            Path ext = Path.of(externalFile);
            if (Files.exists(ext)) {
                log.info("Reading demo data from {}", ext.toAbsolutePath());
                return Files.readAllLines(ext, StandardCharsets.UTF_8);
            }
            try (InputStream in = new ClassPathResource("demo-data.txt").getInputStream()) {
                log.info("Reading demo data from the built-in demo-data.txt");
                return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
            }
        } catch (IOException e) {
            log.error("Could not read demo data: {}", e.getMessage());
            return List.of();
        }
    }
}
