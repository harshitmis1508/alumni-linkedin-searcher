package com.alumni.searcher.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real provider: scrapes LinkedIn through a PhantomBuster "LinkedIn Search Export" agent.
 * Active when phantombuster.mock-enabled=false.
 */
@Component
@ConditionalOnProperty(prefix = "phantombuster", name = "mock-enabled", havingValue = "false")
public class PhantomBusterAlumniDataProvider implements AlumniDataProvider {

    private final PhantomBusterClient client;
    private final PhantomBusterProperties props;
    private final LinkedInSearchUrlBuilder urlBuilder;
    private final PhantomBusterResponseParser parser;

    public PhantomBusterAlumniDataProvider(PhantomBusterClient client,
                                           PhantomBusterProperties props,
                                           LinkedInSearchUrlBuilder urlBuilder,
                                           PhantomBusterResponseParser parser) {
        this.client = client;
        this.props = props;
        this.urlBuilder = urlBuilder;
        this.parser = parser;
    }

    @Override
    public List<AlumniCandidate> fetchAlumni(AlumniSearchCriteria criteria) {
        Map<String, Object> argument = new LinkedHashMap<>();
        argument.put("searches", urlBuilder.build(criteria));
        argument.put("numberOfProfiles", props.getMaxProfiles());
        if (StringUtils.hasText(props.getSessionCookie())) {
            argument.put("sessionCookie", props.getSessionCookie());
        }

        String containerId = client.launchAgent(argument);
        client.waitForCompletion(containerId);
        JsonNode result = client.fetchResultObject(containerId);

        // Some phantoms return links to result files instead of inline results.
        if (result != null && result.isObject()) {
            for (String field : List.of("jsonUrl", "jsonURL")) {
                if (result.hasNonNull(field)) {
                    result = client.downloadJson(result.get(field).asText());
                    break;
                }
            }
        }
        return parser.parse(result);
    }
}
