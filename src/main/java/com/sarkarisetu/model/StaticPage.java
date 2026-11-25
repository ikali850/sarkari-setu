package com.sarkarisetu.model;

import jakarta.persistence.*;

/** About, Privacy Policy, Disclaimer, Terms and any page you add. Placeholders: {site} {domain} {email} {phone} {address} */
@Entity
@Table(name = "static_page")
public class StaticPage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(unique = true, nullable = false)
    private String slug;
    @Column(columnDefinition = "TEXT")
    private String body;
    private boolean visible = true;
    private boolean inFooter = true;

    public StaticPage() {}
    public StaticPage(String title, String slug, String body) { this.title = title; this.slug = slug; this.body = body; }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getSlug() { return slug; } public void setSlug(String v) { slug = v; }
    public String getBody() { return body; } public void setBody(String v) { body = v; }
    public boolean isVisible() { return visible; } public void setVisible(boolean v) { visible = v; }
    public boolean isInFooter() { return inFooter; } public void setInFooter(boolean v) { inFooter = v; }
}
