package com.sarkarisetu.model;

import jakarta.persistence.*;

@Entity
public class Tool {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String slug;
    @Column(length = 300)
    private String description;
    private boolean enabled = true;
    private int sortOrder = 10;

    public Tool() {}
    public Tool(String name, String slug, String description, int sortOrder) { this.name = name; this.slug = slug; this.description = description; this.sortOrder = sortOrder; }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getSlug() { return slug; } public void setSlug(String v) { slug = v; }
    public String getDescription() { return description; } public void setDescription(String v) { description = v; }
    public boolean isEnabled() { return enabled; } public void setEnabled(boolean v) { enabled = v; }
    public int getSortOrder() { return sortOrder; } public void setSortOrder(int v) { sortOrder = v; }
}
