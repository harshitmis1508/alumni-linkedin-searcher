package com.alumni.searcher.provider;

import com.alumni.searcher.exception.PhantomBusterException;
import com.alumni.searcher.exception.PhantomBusterTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;

/**
 * Thin HTTP wrapper around the PhantomBuster v2 API:
 * launch agent -> poll container until finished -> fetch result object.
 * All PhantomBuster-specific HTTP details live here.
 */
@Component
public class PhantomBusterClient {

    private static final Logger log = LoggerFactory.getLogger(PhantomBusterClient.class);

    private final RestClient restClient;
    private final RestClient plainClient;
    private final PhantomBusterProperties props;
    private final ObjectMapper objectMapper;

    public PhantomBusterClient(PhantomBusterProperties props, ObjectMapper objectMapper, RestClient.Builder builder) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.restClient = builder.clone()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("X-Phantombuster-Key", props.getApiKey() == null ? "" : props.getApiKey())
                .build();
        // Used for downloading result files from external storage (must NOT carry our API key).
        this.plainClient = RestClient.create();
    }

    /** Launches the configured agent and returns the container id of the run. */
    public String launchAgent(Map<String, Object> argument) {
        assertConfigured();
        Map<String, Object> body = Map.of("id", props.getAgentId(), "argument", argument);
        JsonNode response = execute(() -> restClient.post()
                .uri("/agents/launch")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class));
        String containerId = response == null ? null : response.path("containerId").asText(null);
        if (!StringUtils.hasText(containerId)) {
            throw new PhantomBusterException("PhantomBuster did not return a containerId");
        }
        log.info("PhantomBuster agent launched, containerId={}", containerId);
        return containerId;
    }

    /** Polls until the container reports status=finished, or throws on timeout. */
    public void waitForCompletion(String containerId) {
        long deadline = System.nanoTime() + Duration.ofSeconds(props.getTimeoutSeconds()).toNanos();
        while (System.nanoTime() < deadline) {
            JsonNode out = execute(() -> restClient.get()
                    .uri("/containers/fetch-output?id={id}", containerId)
                    .retrieve()
                    .body(JsonNode.class));
            String status = out == null ? "" : out.path("status").asText("");
            log.debug("PhantomBuster container {} status={}", containerId, status);
            if ("finished".equalsIgnoreCase(status)) {
                return;
            }
            sleep(props.getPollIntervalMs());
        }
        throw new PhantomBusterTimeoutException(
                "PhantomBuster agent did not finish within " + props.getTimeoutSeconds() + " seconds");
    }

    /** Fetches the result object of a finished container (may be an array, an object, or null). */
    public JsonNode fetchResultObject(String containerId) {
        JsonNode response = execute(() -> restClient.get()
                .uri("/containers/fetch-result-object?id={id}", containerId)
                .retrieve()
                .body(JsonNode.class));
        if (response == null) {
            return null;
        }
        JsonNode result = response.path("resultObject");
        if (result.isTextual()) {
            String text = result.asText();
            if (!StringUtils.hasText(text) || "null".equals(text)) {
                return null;
            }
            try {
                return objectMapper.readTree(text);
            } catch (JsonProcessingException e) {
                throw new PhantomBusterException("Could not parse PhantomBuster result", e);
            }
        }
        return result.isMissingNode() || result.isNull() ? null : result;
    }

    /** Downloads a JSON file (e.g. result .json hosted by PhantomBuster) without sending the API key. */
    public JsonNode downloadJson(String url) {
        return execute(() -> plainClient.get().uri(url).retrieve().body(JsonNode.class));
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(props.getApiKey()) || !StringUtils.hasText(props.getAgentId())) {
            throw new PhantomBusterException(
                    "PhantomBuster is not configured: set PHANTOMBUSTER_API_KEY and PHANTOMBUSTER_AGENT_ID "
                            + "(or enable mock mode with PHANTOMBUSTER_MOCK_ENABLED=true)");
        }
    }

    private <T> T execute(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException e) {
            throw new PhantomBusterException(
                    "PhantomBuster API returned HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            throw new PhantomBusterException("Could not reach PhantomBuster API", e);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PhantomBusterException("Interrupted while waiting for PhantomBuster", e);
        }
    }
}
