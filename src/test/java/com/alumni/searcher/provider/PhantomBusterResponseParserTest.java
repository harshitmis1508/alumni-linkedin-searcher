package com.alumni.searcher.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhantomBusterResponseParserTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PhantomBusterResponseParser parser = new PhantomBusterResponseParser();

    @Test
    void parsesArrayOfProfiles() throws Exception {
        String json = """
                [{"fullName":"John Doe","headline":"Software Engineer at XYZ Corp",
                  "location":"New York, NY","profileUrl":"https://linkedin.com/in/john"}]
                """;

        List<AlumniCandidate> result = parser.parse(mapper.readTree(json));

        assertThat(result).hasSize(1);
        AlumniCandidate c = result.get(0);
        assertThat(c.name()).isEqualTo("John Doe");
        assertThat(c.currentRole()).isEqualTo("Software Engineer");
        assertThat(c.headline()).isEqualTo("Software Engineer at XYZ Corp");
        assertThat(c.location()).isEqualTo("New York, NY");
        assertThat(c.linkedinUrl()).isEqualTo("https://linkedin.com/in/john");
    }

    @Test
    void buildsNameFromFirstAndLastName() throws Exception {
        List<AlumniCandidate> result = parser.parse(
                mapper.readTree("[{\"firstName\":\"Jane\",\"lastName\":\"Smith\"}]"));

        assertThat(result).extracting(AlumniCandidate::name).containsExactly("Jane Smith");
    }

    @Test
    void readsProfilesNestedUnderResultsField() throws Exception {
        List<AlumniCandidate> result = parser.parse(
                mapper.readTree("{\"results\":[{\"name\":\"Jane Smith\"}]}"));

        assertThat(result).hasSize(1);
    }

    @Test
    void skipsEntriesWithoutName() throws Exception {
        List<AlumniCandidate> result = parser.parse(
                mapper.readTree("[{\"headline\":\"Engineer\"},{\"name\":\"Valid Person\"}]"));

        assertThat(result).extracting(AlumniCandidate::name).containsExactly("Valid Person");
    }

    @Test
    void parsesPassoutYearOnlyWhenItLooksLikeAYear() throws Exception {
        List<AlumniCandidate> result = parser.parse(mapper.readTree(
                "[{\"name\":\"A\",\"endYear\":\"2018\"},{\"name\":\"B\",\"endYear\":\"abc\"}]"));

        assertThat(result.get(0).passoutYear()).isEqualTo(2018);
        assertThat(result.get(1).passoutYear()).isNull();
    }

    @Test
    void returnsEmptyListForNullOrUnsupportedInput() throws Exception {
        assertThat(parser.parse(null)).isEmpty();
        assertThat(parser.parse(mapper.readTree("\"just text\""))).isEmpty();
        assertThat(parser.parse(mapper.readTree("{}"))).isEmpty();
    }

    @Test
    void extractRoleHandlesCommonHeadlineFormats() {
        assertThat(PhantomBusterResponseParser.extractRole("Data Scientist | AI Enthusiast")).isEqualTo("Data Scientist");
        assertThat(PhantomBusterResponseParser.extractRole("Engineer @ Google")).isEqualTo("Engineer");
        assertThat(PhantomBusterResponseParser.extractRole("Product Manager - Fintech")).isEqualTo("Product Manager");
        assertThat(PhantomBusterResponseParser.extractRole("Architect")).isEqualTo("Architect");
        assertThat(PhantomBusterResponseParser.extractRole(" ")).isNull();
        assertThat(PhantomBusterResponseParser.extractRole(null)).isNull();
    }
}
