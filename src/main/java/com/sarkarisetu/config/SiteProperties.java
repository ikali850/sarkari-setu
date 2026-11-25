package com.sarkarisetu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

/** All values come from site.properties. Edit that file, restart, done. */
@Component
@ConfigurationProperties(prefix = "site")
public class SiteProperties {
    private String name = "Sarkari Setu";
    private String tagline = "";
    private String baseUrl = "http://localhost:8080";
    private String contactEmail = "";
    private String phone = "";
    private String address = "";
    private String telegramUrl = "";
    private String whatsappUrl = "";
    private String instagramUrl = "";
    private String youtubeUrl = "";
    private String analyticsId = "";
    private String themeColor = "#1d3fbf";
    private String footerNote = "";
    private String disclaimerNote = "";
    private String filterTags = "";
    private String ogImage = "";
    private boolean cookieNotice = true;
    private String homeSections = "answer-key,syllabus,admission";
    private int trendingCount = 10;
    private int pageSize = 12;

    public List<String> getFilterTagList() {
        return filterTags == null || filterTags.isBlank() ? List.of()
                : Arrays.stream(filterTags.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getTagline() { return tagline; } public void setTagline(String v) { tagline = v; }
    public String getBaseUrl() { return baseUrl; } public void setBaseUrl(String v) { baseUrl = v; }
    public String getContactEmail() { return contactEmail; } public void setContactEmail(String v) { contactEmail = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public String getAddress() { return address; } public void setAddress(String v) { address = v; }
    public String getTelegramUrl() { return telegramUrl; } public void setTelegramUrl(String v) { telegramUrl = v; }
    public String getWhatsappUrl() { return whatsappUrl; } public void setWhatsappUrl(String v) { whatsappUrl = v; }
    public String getInstagramUrl() { return instagramUrl; } public void setInstagramUrl(String v) { instagramUrl = v; }
    public String getYoutubeUrl() { return youtubeUrl; } public void setYoutubeUrl(String v) { youtubeUrl = v; }
    public String getAnalyticsId() { return analyticsId; } public void setAnalyticsId(String v) { analyticsId = v; }
    public String getThemeColor() { return themeColor; } public void setThemeColor(String v) { themeColor = v; }
    public String getFooterNote() { return footerNote; } public void setFooterNote(String v) { footerNote = v; }
    public String getDisclaimerNote() { return disclaimerNote; } public void setDisclaimerNote(String v) { disclaimerNote = v; }
    public List<String> getHomeSectionList() {
        return homeSections == null || homeSections.isBlank() ? List.of()
                : Arrays.stream(homeSections.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
    public String getHomeSections() { return homeSections; } public void setHomeSections(String v) { homeSections = v; }
    public String getOgImage() { return ogImage; } public void setOgImage(String v) { ogImage = v; }
    public boolean isCookieNotice() { return cookieNotice; } public void setCookieNotice(boolean v) { cookieNotice = v; }
    public String getFilterTags() { return filterTags; } public void setFilterTags(String v) { filterTags = v; }
    public int getTrendingCount() { return trendingCount; } public void setTrendingCount(int v) { trendingCount = v; }
    public int getPageSize() { return pageSize; } public void setPageSize(int v) { pageSize = v; }
}
