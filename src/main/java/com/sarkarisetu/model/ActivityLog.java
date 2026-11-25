package com.sarkarisetu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String who;
    @Column(length = 300)
    private String action;
    private LocalDateTime happenedAt = LocalDateTime.now();

    public ActivityLog() {}
    public ActivityLog(String who, String action) { this.who = who; this.action = action; }
    public String getHappenedText() { return happenedAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")); }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getWho() { return who; } public void setWho(String v) { who = v; }
    public String getAction() { return action; } public void setAction(String v) { action = v; }
    public LocalDateTime getHappenedAt() { return happenedAt; } public void setHappenedAt(LocalDateTime v) { happenedAt = v; }
}
