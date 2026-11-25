package com.sarkarisetu.model;

import jakarta.persistence.*;

@Entity
public class Section {
    public enum Kind { RECRUITMENTS, STAGE_LIST, POSTS }
    public enum Placement { TAB, MORE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String slug;
    @Enumerated(EnumType.STRING)
    private Kind kind = Kind.POSTS;
    @Enumerated(EnumType.STRING)
    private Placement placement = Placement.MORE;
    private int sortOrder = 10;
    private boolean visible = true;

    public Section() {}
    public Section(String name, String slug, Kind kind, Placement placement, int sortOrder) {
        this.name = name; this.slug = slug; this.kind = kind; this.placement = placement; this.sortOrder = sortOrder;
    }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getSlug() { return slug; } public void setSlug(String v) { slug = v; }
    public Kind getKind() { return kind; } public void setKind(Kind v) { kind = v; }
    public Placement getPlacement() { return placement; } public void setPlacement(Placement v) { placement = v; }
    public int getSortOrder() { return sortOrder; } public void setSortOrder(int v) { sortOrder = v; }
    public boolean isVisible() { return visible; } public void setVisible(boolean v) { visible = v; }
}
