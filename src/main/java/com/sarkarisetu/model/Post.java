package com.sarkarisetu.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
public class Post {
    public enum Status { DRAFT, PUBLISHED, UNPUBLISHED }

    /** Application fee row: one per category. */
    @Embeddable
    public static class Fee {
        private String category; private String amount; private String note;
        public Fee() {}
        public Fee(String c, String a, String n) { category = c; amount = a; note = n; }
        public String getCategory() { return category; } public void setCategory(String v) { category = v; }
        public String getAmount() { return amount; } public void setAmount(String v) { amount = v; }
        public String getNote() { return note; } public void setNote(String v) { note = v; }
    }

    /** Any extra label and value this notification has. */
    @Embeddable
    public static class Extra {
        private String label; private String detail;
        public Extra() {}
        public Extra(String l, String d) { label = l; detail = d; }
        public String getLabel() { return label; } public void setLabel(String v) { label = v; }
        public String getDetail() { return detail; } public void setDetail(String v) { detail = v; }
    }

    /** One stage. appliesTo is "All posts" or a post name, so stages can differ per post. */
    @Embeddable
    public static class Stage {
        private String name; private String appliesTo; private String whenText; private String state; private String link;
        public Stage() {}
        public Stage(String n, String a, String w, String s, String l) { name = n; appliesTo = a; whenText = w; state = s; link = l; }
        public String getName() { return name; } public void setName(String v) { name = v; }
        public String getAppliesTo() { return appliesTo; } public void setAppliesTo(String v) { appliesTo = v; }
        public String getWhenText() { return whenText; } public void setWhenText(String v) { whenText = v; }
        public String getState() { return state; } public void setState(String v) { state = v; }
        public String getLink() { return link; } public void setLink(String v) { link = v; }
        public String getStateClass() { return "Live".equalsIgnoreCase(state) ? "inf" : "Done".equalsIgnoreCase(state) ? "ok" : "mu"; }
    }

    /** Category-wise vacancies for one post. */
    @Embeddable
    public static class Vacancy {
        private String postName; private int ur; private int obc; private int sc; private int st; private int ews;
        public Vacancy() {}
        public Vacancy(String p, int ur, int obc, int sc, int st, int ews) { postName = p; this.ur = ur; this.obc = obc; this.sc = sc; this.st = st; this.ews = ews; }
        public int getTotal() { return ur + obc + sc + st + ews; }
        public String getPostName() { return postName; } public void setPostName(String v) { postName = v; }
        public int getUr() { return ur; } public void setUr(int v) { ur = v; }
        public int getObc() { return obc; } public void setObc(int v) { obc = v; }
        public int getSc() { return sc; } public void setSc(int v) { sc = v; }
        public int getSt() { return st; } public void setSt(int v) { st = v; }
        public int getEws() { return ews; } public void setEws(int v) { ews = v; }
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(unique = true, nullable = false)
    private String slug;
    @ManyToOne
    private Section section;
    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;
    private String organisation;
    private String totalPosts;
    private LocalDate lastDate;
    private String tags;
    @Column(length = 500)
    private String summary;
    @Column(columnDefinition = "TEXT")
    private String body;
    private long views;
    private boolean pinned;
    private boolean verified;
    private String verifiedBy;
    @ManyToOne
    private AppUser author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    @ElementCollection @CollectionTable(name = "post_fee", joinColumns = @JoinColumn(name = "post_id"))
    private List<Fee> fees = new ArrayList<>();
    @ElementCollection @CollectionTable(name = "post_extra", joinColumns = @JoinColumn(name = "post_id"))
    private List<Extra> extras = new ArrayList<>();
    @ElementCollection @CollectionTable(name = "post_stage", joinColumns = @JoinColumn(name = "post_id"))
    private List<Stage> stages = new ArrayList<>();
    @ElementCollection @CollectionTable(name = "post_vacancy", joinColumns = @JoinColumn(name = "post_id"))
    private List<Vacancy> vacancies = new ArrayList<>();

    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = LocalDateTime.now(); }

    /** The stage to highlight: first Live, else first Upcoming, else the last Done. */
    public Stage getCurrentStage() {
        for (Stage s : stages) if ("Live".equalsIgnoreCase(s.getState())) return s;
        for (Stage s : stages) if ("Upcoming".equalsIgnoreCase(s.getState())) return s;
        return stages.isEmpty() ? null : stages.get(stages.size() - 1);
    }
    public String getBadge() {
        Stage s = getCurrentStage();
        if (s != null) return s.getName() + ": " + s.getState();
        if (lastDate != null) return lastDate.isBefore(LocalDate.now()) ? "Closed" : "Apply open";
        return "";
    }
    public String getBadgeClass() {
        Stage s = getCurrentStage();
        if (s != null) return s.getStateClass();
        return lastDate != null && lastDate.isBefore(LocalDate.now()) ? "mu" : "inf";
    }
    /** Days until the last date (negative when closed, -999 when there is none). */
    public long getDaysLeft() { return lastDate == null ? -999 : java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), lastDate); }
    public String getDaysLeftText() {
        long d = getDaysLeft();
        if (d == -999) return "";
        if (d < 0) return "Closed";
        return d == 0 ? "Last day today" : (d == 1 ? "1 day left" : d + " days left");
    }
    public String getPublishedIso() { return publishedAt == null ? "" : publishedAt.toString(); }
    public String getModifiedIso() { return updatedAt == null ? "" : updatedAt.toString(); }
    public String getUpdatedOnText() {
        LocalDateTime d = publishedAt != null ? publishedAt : updatedAt;
        return d == null ? "" : "Updated on " + d.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
    public String getLastDateText() { return lastDate == null ? "" : lastDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")); }
    public String getUpdatedText() { return updatedAt == null ? "" : updatedAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")); }
    public String getPublishedText() { return publishedAt == null ? "" : publishedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy")); }

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getSlug() { return slug; } public void setSlug(String v) { slug = v; }
    public Section getSection() { return section; } public void setSection(Section v) { section = v; }
    public Status getStatus() { return status; } public void setStatus(Status v) { status = v; }
    public String getOrganisation() { return organisation; } public void setOrganisation(String v) { organisation = v; }
    public String getTotalPosts() { return totalPosts; } public void setTotalPosts(String v) { totalPosts = v; }
    public LocalDate getLastDate() { return lastDate; } public void setLastDate(LocalDate v) { lastDate = v; }
    public String getTags() { return tags; } public void setTags(String v) { tags = v; }
    public String getSummary() { return summary; } public void setSummary(String v) { summary = v; }
    public String getBody() { return body; } public void setBody(String v) { body = v; }
    public long getViews() { return views; } public void setViews(long v) { views = v; }
    public boolean isPinned() { return pinned; } public void setPinned(boolean v) { pinned = v; }
    public boolean isVerified() { return verified; } public void setVerified(boolean v) { verified = v; }
    public String getVerifiedBy() { return verifiedBy; } public void setVerifiedBy(String v) { verifiedBy = v; }
    public AppUser getAuthor() { return author; } public void setAuthor(AppUser v) { author = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getPublishedAt() { return publishedAt; } public void setPublishedAt(LocalDateTime v) { publishedAt = v; }
    public List<Fee> getFees() { return fees; } public void setFees(List<Fee> v) { fees = v; }
    public List<Extra> getExtras() { return extras; } public void setExtras(List<Extra> v) { extras = v; }
    public List<Stage> getStages() { return stages; } public void setStages(List<Stage> v) { stages = v; }
    public List<Vacancy> getVacancies() { return vacancies; } public void setVacancies(List<Vacancy> v) { vacancies = v; }
}
