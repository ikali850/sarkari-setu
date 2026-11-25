package com.sarkarisetu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reads mock tests from mock-tests.txt (a copy next to the jar, ./mock-tests.txt, wins over the built-in one). */
@Service
public class MockTestService {
    private static final Logger log = LoggerFactory.getLogger(MockTestService.class);

    public static class Question {
        private final String text, explain; private final List<String> options; private final int correct;
        public Question(String text, List<String> options, int correct, String explain) { this.text = text; this.options = options; this.correct = correct; this.explain = explain; }
        public String getText() { return text; } public List<String> getOptions() { return options; } public String getExplain() { return explain; }
        public String getCorrectLetter() { return String.valueOf((char) ('A' + correct)); }
    }

    public static class Test {
        private final String slug, title, description; private final int minutes; private final List<Question> questions = new ArrayList<>();
        public Test(String slug, String title, int minutes, String description) { this.slug = slug; this.title = title; this.minutes = minutes; this.description = description; }
        public String getSlug() { return slug; } public String getTitle() { return title; } public int getMinutes() { return minutes; }
        public String getDescription() { return description; } public List<Question> getQuestions() { return questions; }
        public int getCount() { return questions.size(); }
    }

    private final Map<String, Test> tests = new LinkedHashMap<>();

    public MockTestService() {
        Test cur = null;
        for (String line : readLines()) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("#")) continue;
            if (l.startsWith("@test ")) {
                String[] c = l.substring(6).split("\\|", -1);
                if (c.length < 4) { log.warn("mock-tests: bad @test line: {}", l); cur = null; continue; }
                int mins = 20;
                try { mins = Integer.parseInt(c[2].trim()); } catch (NumberFormatException ignored) { }
                cur = new Test(c[0].trim(), c[1].trim(), mins, c[3].trim());
                tests.put(cur.getSlug(), cur);
                continue;
            }
            if (cur == null) continue;
            String[] c = l.split("\\|", -1);
            if (c.length < 6 || c[5].trim().isEmpty()) { log.warn("mock-tests: bad question line: {}", l); continue; }
            int correct = Character.toUpperCase(c[5].trim().charAt(0)) - 'A';
            if (correct < 0 || correct > 3) { log.warn("mock-tests: bad answer letter: {}", l); continue; }
            cur.getQuestions().add(new Question(c[0].trim(), List.of(c[1].trim(), c[2].trim(), c[3].trim(), c[4].trim()), correct, c.length > 6 ? c[6].trim() : ""));
        }
        log.info("Mock tests loaded: {}", tests.size());
    }

    public List<Test> all() { return new ArrayList<>(tests.values()); }
    public Optional<Test> find(String slug) { return Optional.ofNullable(tests.get(slug)); }

    private List<String> readLines() {
        try {
            Path ext = Path.of("mock-tests.txt");
            if (Files.exists(ext)) return Files.readAllLines(ext, StandardCharsets.UTF_8);
            try (InputStream in = new ClassPathResource("mock-tests.txt").getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
            }
        } catch (IOException e) {
            log.warn("No mock-tests.txt found: {}", e.getMessage());
            return List.of();
        }
    }
}
