package com.sarkarisetu.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "contact_message")
public class ContactMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String subject;
    @Column(length = 3000)
    private String message;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean handled;

    public String getCreatedText() { return createdAt.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm")); }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getSubject() { return subject; } public void setSubject(String v) { subject = v; }
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public boolean isHandled() { return handled; } public void setHandled(boolean v) { handled = v; }
}
