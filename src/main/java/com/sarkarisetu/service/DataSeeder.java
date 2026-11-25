package com.sarkarisetu.service;

import com.sarkarisetu.model.*;
import com.sarkarisetu.model.Section.Kind;
import com.sarkarisetu.model.Section.Placement;
import com.sarkarisetu.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fills an empty database on first run: first Super Admin, sections, tools, basic pages, optional sample posts. */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final UserRepository users; private final SectionRepository sections; private final ToolRepository tools;
    private final PageRepository pages; private final PostRepository posts; private final PasswordEncoder enc; private final DemoPosts demoPosts;
    @Value("${site.seed-admin-name:Super Admin}") private String adminName;
    @Value("${site.seed-admin-email:admin@example.com}") private String adminEmail;
    @Value("${site.seed-admin-password:ChangeMe@123}") private String adminPassword;
    @Value("${site.seed-demo:true}") private boolean demo;

    public DataSeeder(UserRepository users, SectionRepository sections, ToolRepository tools, PageRepository pages, PostRepository posts, PasswordEncoder enc, DemoPosts demoPosts) {
        this.demoPosts = demoPosts; this.users = users; this.sections = sections; this.tools = tools; this.pages = pages; this.posts = posts; this.enc = enc;
    }

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            AppUser u = new AppUser();
            u.setName(adminName); u.setEmail(adminEmail); u.setRole(AppUser.Role.SUPER_ADMIN);
            u.setPasswordHash(enc.encode(adminPassword));
            users.save(u);
            log.warn("First Super Admin created: {}. Change the password after logging in (Admin > My profile).", adminEmail);
        }
        if (sections.count() == 0) {
            sections.save(new Section("Latest Jobs", "jobs", Kind.RECRUITMENTS, Placement.TAB, 1));
            sections.save(new Section("Admit Card", "admit-card", Kind.STAGE_LIST, Placement.TAB, 2));
            sections.save(new Section("Result", "result", Kind.STAGE_LIST, Placement.TAB, 3));
            sections.save(new Section("Syllabus", "syllabus", Kind.STAGE_LIST, Placement.MORE, 4));
            sections.save(new Section("Admission", "admission", Kind.POSTS, Placement.MORE, 5));
            sections.save(new Section("Answer Key", "answer-key", Kind.STAGE_LIST, Placement.MORE, 6));
            sections.save(new Section("Scheme", "scheme", Kind.POSTS, Placement.MORE, 7));
            sections.save(new Section("Blog", "blog", Kind.POSTS, Placement.MORE, 8));
        }
        if (tools.count() == 0) {
            tools.save(new Tool("Age Calculator", "age-calculator", "Find your exact age on any date and check age limit eligibility.", 1));
            tools.save(new Tool("Photo and Signature Resizer", "photo-resizer", "Resize and compress a photo or signature to the size and KB limit a form asks for.", 2));
            tools.save(new Tool("Percentage Calculator", "percentage-calculator", "Marks to percentage and CGPA to percentage.", 3));
            tools.save(new Tool("Typing Speed Test", "typing-test", "Check your typing speed (WPM) and accuracy in one minute.", 4));
        }
        if (pages.count() == 0) seedPages();
        if (demo && posts.count() == 0) {
            java.util.Map<String, Section> bySlug = new java.util.HashMap<>();
            for (Section sec : sections.findAll()) bySlug.put(sec.getSlug(), sec);
            int n = demoPosts.seed(bySlug, users.findAll().get(0));
            log.info("Added {} sample posts (set site.seed-demo=false to skip).", n);
        }
    }

    private void seedPages() {
        pages.save(new StaticPage("About Us", "about",
            "<p>{site} helps job seekers follow government recruitment from notification to result, in one place.</p>"
          + "<p>We collect information from official websites and notices and present it in a simple format. We are an information website and are not connected with any government department.</p>"
          + "<p>Contact us at <a href=\"mailto:{email}\">{email}</a>.</p>"));
        pages.save(new StaticPage("Privacy Policy", "privacy-policy",
            "<p>This Privacy Policy explains how {site} ({domain}) handles information when you use our website.</p>"
          + "<h2>Information we collect</h2><p>If you use our contact form or subscribe for alerts, we receive the details you type (name, email address and message). We do not ask for passwords, Aadhaar or payment details.</p>"
          + "<h2>Cookies and analytics</h2><p>We may use cookies and analytics tools to understand how the site is used. Third-party advertising partners, if any, may also use cookies. You can control cookies from your browser settings.</p>"
          + "<h2>External links</h2><p>Our pages link to official and third-party websites. We are not responsible for their content or privacy practices.</p>"
          + "<h2>Children</h2><p>This website is not directed at children under 13.</p>"
          + "<h2>Changes</h2><p>We may update this policy from time to time. The latest version is always on this page.</p>"
          + "<h2>Contact</h2><p>Questions about this policy: <a href=\"mailto:{email}\">{email}</a>.</p>"));
        pages.save(new StaticPage("Disclaimer", "disclaimer",
            "<p><b>{site} is not a government website or portal, and we are not government employees.</b> We are an independent information website. We do not conduct recruitment, issue admit cards, declare results or give jobs.</p>"
          + "<p>The information here is collected from official notifications and websites for general guidance. Dates, fees, eligibility and links can change, so always check the official website before you apply or pay any fee.</p>"
          + "<p>Never pay money to anyone for a government job. {site} does not guarantee any job, admission or result.</p>"));
        pages.save(new StaticPage("FAQs", "faq",
            "<details><summary>What is {site}?</summary><p>An independent information website that helps job seekers follow government recruitment from notification to admit card and result, in one place.</p></details>"
          + "<details><summary>Is this an official government website?</summary><p>No. We are not government employees and this is not an official portal. We only share information taken from official notifications and websites.</p></details>"
          + "<details><summary>Do you give jobs or take applications?</summary><p>No. We do not conduct recruitment or accept forms. Every apply link takes you to the official website.</p></details>"
          + "<details><summary>How do I know the information is correct?</summary><p>Each post is checked by hand against the official notification before it is published. Still, dates and fees can change, so always confirm on the official website.</p></details>"
          + "<details><summary>Do I have to pay to use this site?</summary><p>No, the site and its tools are free. Never pay money to anyone for a government job, and pay application fees only on the official website.</p></details>"
          + "<details><summary>How do I get updates for a recruitment?</summary><p>Subscribe with your email or join our Telegram channel.</p></details>"
          + "<details><summary>How do I report a mistake?</summary><p>Use the Contact Us page and tell us the post name and what is wrong. We will correct it.</p></details>"
          + "<details><summary>Are my saved posts and photos private?</summary><p>Yes. Saved posts stay in your browser, and the photo and age tools run on your device. Nothing is uploaded.</p></details>"));
        pages.save(new StaticPage("Terms and Conditions", "terms",
            "<p>By using {site} you agree to these terms. Content is provided as is, without warranty. You agree not to misuse the website or attempt to disrupt it. We may change or remove content at any time.</p>"
          + "<p>Questions: <a href=\"mailto:{email}\">{email}</a>.</p>"));
    }
}
