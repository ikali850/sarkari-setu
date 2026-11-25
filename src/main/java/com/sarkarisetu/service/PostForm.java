package com.sarkarisetu.service;

/** Flat form object for the post editor. Table-like data is typed as lines: "a | b | c". */
public class PostForm {
    private Long id;
    private Long sectionId;
    private String title, slug, organisation, totalPosts, lastDate, tags, summary, body;
    private String feesText, extraText, vacancyText, stagesText, action;
    private boolean check1, check2, check3, check4, check5;

    public boolean allChecked() { return check1 && check2 && check3 && check4 && check5; }

    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public Long getSectionId() { return sectionId; } public void setSectionId(Long v) { sectionId = v; }
    public String getTitle() { return title; } public void setTitle(String v) { title = v; }
    public String getSlug() { return slug; } public void setSlug(String v) { slug = v; }
    public String getOrganisation() { return organisation; } public void setOrganisation(String v) { organisation = v; }
    public String getTotalPosts() { return totalPosts; } public void setTotalPosts(String v) { totalPosts = v; }
    public String getLastDate() { return lastDate; } public void setLastDate(String v) { lastDate = v; }
    public String getTags() { return tags; } public void setTags(String v) { tags = v; }
    public String getSummary() { return summary; } public void setSummary(String v) { summary = v; }
    public String getBody() { return body; } public void setBody(String v) { body = v; }
    public String getFeesText() { return feesText; } public void setFeesText(String v) { feesText = v; }
    public String getExtraText() { return extraText; } public void setExtraText(String v) { extraText = v; }
    public String getVacancyText() { return vacancyText; } public void setVacancyText(String v) { vacancyText = v; }
    public String getStagesText() { return stagesText; } public void setStagesText(String v) { stagesText = v; }
    public String getAction() { return action; } public void setAction(String v) { action = v; }
    public boolean isCheck1() { return check1; } public void setCheck1(boolean v) { check1 = v; }
    public boolean isCheck2() { return check2; } public void setCheck2(boolean v) { check2 = v; }
    public boolean isCheck3() { return check3; } public void setCheck3(boolean v) { check3 = v; }
    public boolean isCheck4() { return check4; } public void setCheck4(boolean v) { check4 = v; }
    public boolean isCheck5() { return check5; } public void setCheck5(boolean v) { check5 = v; }
}
