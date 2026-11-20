package com.alumni.searcher.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "phantombuster")
public class PhantomBusterProperties {

    private boolean mockEnabled = true;
    private String baseUrl = "https://api.phantombuster.com/api/v2";
    private String apiKey;
    private String agentId;
    private String sessionCookie;
    private int maxProfiles = 10;
    private long pollIntervalMs = 5000;
    private long timeoutSeconds = 180;

    public boolean isMockEnabled() { return mockEnabled; }
    public void setMockEnabled(boolean mockEnabled) { this.mockEnabled = mockEnabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getSessionCookie() { return sessionCookie; }
    public void setSessionCookie(String sessionCookie) { this.sessionCookie = sessionCookie; }
    public int getMaxProfiles() { return maxProfiles; }
    public void setMaxProfiles(int maxProfiles) { this.maxProfiles = maxProfiles; }
    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }
    public long getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(long timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
