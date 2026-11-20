package com.alumni.searcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "alumni")
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "job_role")
    private String currentRole;

    @Column(nullable = false)
    private String university;

    private String location;

    @Column(name = "linkedin_headline", length = 1000)
    private String linkedinHeadline;

    @Column(name = "passout_year")
    private Integer passoutYear;

    @Column(name = "linkedin_url", unique = true, length = 500)
    private String linkedinUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getLinkedinHeadline() { return linkedinHeadline; }
    public void setLinkedinHeadline(String linkedinHeadline) { this.linkedinHeadline = linkedinHeadline; }
    public Integer getPassoutYear() { return passoutYear; }
    public void setPassoutYear(Integer passoutYear) { this.passoutYear = passoutYear; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
