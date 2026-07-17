package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.config.JacksonConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JsonNodeMapper.class,
    JacksonConfig.class
})
public class JsonNodeMapperTest {

  @Autowired
  private JsonNodeMapper jsonNodeMapper;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void toMap_shouldReturnNull_whenPayloadIsNull() {
    Map<String, Object> result = jsonNodeMapper.toMap(null);
    assertThat(result).isNull();
  }

  @Test
  void toMap_shouldConvertJsonNodeToMap() throws Exception {
    JsonNode jsonNode = objectMapper.readTree("""
        {
          "name": "Hagrid",
          "dob": "1928-12-06",
          "height": 350,
          "isMuggle": false,
          "address": {
            "street": "Hut",
            "city": "Hogwarts"
          },
          "pets": [
            {
              "type": "dog",
              "name": "Fluffy"
            },
            {
              "type": "dragon",
              "name": "Norbert"
            },
            {
              "type": "hippogriff",
              "name": "Buckbeak"
            }
          ]
        }
        """);

    Map<String, Object> result = jsonNodeMapper.toMap(jsonNode);

    assertThat(result)
        .containsEntry("name", "Hagrid")
        .containsEntry("dob", "1928-12-06")
        .containsEntry("height", 350)
        .containsEntry("isMuggle", false);

    assertThat(result.get("address"))
        .isEqualTo(
            Map.of(
                "street", "Hut",
                "city", "Hogwarts"
            )
        );

    assertThat(result.get("pets"))
        .isEqualTo(
            java.util.List.of(
                Map.of(
                    "type", "dog",
                    "name", "Fluffy"),
                Map.of(
                    "type", "dragon",
                    "name", "Norbert"),
                Map.of(
                    "type", "hippogriff",
                    "name", "Buckbeak"
                )
            )
        );
  }

  @Test
  void toMap_shouldConvertTextualJsonNodeToMap() {
    JsonNode jsonNode =
        objectMapper.getNodeFactory()
            .textNode("""
            {
              "name":"Hagrid",
              "dob":"1928-12-06",
              "height":350,
              "isMuggle":false,
              "address":{
                "street":"Hut",
                "city":"Hogwarts"
              },
              "pets":[
                {
                  "type":"dog",
                  "name":"Fluffy"
                }
              ]
            }
            """);

    Map<String, Object> result = jsonNodeMapper.toMap(jsonNode);

    assertThat(result)
        .containsEntry("name", "Hagrid")
        .containsEntry("dob", "1928-12-06")
        .containsEntry("height", 350)
        .containsEntry("isMuggle", false);

    assertThat(result.get("address"))
        .isEqualTo(
            Map.of(
                "street", "Hut",
                "city", "Hogwarts"
            )
        );

    assertThat(result.get("pets"))
        .isEqualTo(
            java.util.List.of(
                Map.of(
                    "type", "dog",
                    "name", "Fluffy"
                )
            )
        );
  }

  @Test
  void toMap_shouldThrowIllegalArgumentExceptionWhenTextualJsonIsInvalid() {
    JsonNode jsonNode =
        objectMapper.getNodeFactory()
            .textNode("{invalid-json}");

    assertThatThrownBy(() -> jsonNodeMapper.toMap(jsonNode))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unable to parse payload JSON");
  }

  @Test
  void toJsonNode_shouldConvertMapToJsonNode() {

    Map<String, Object> payload =
        Map.of(
            "name", "Hagrid",
            "dob", LocalDate.of(1928, 12, 6),
            "height", 350,
            "isMuggle", false,
            "address", Map.of(
                "street", "Hut",
                "city", "Hogwarts"),
            "pets", List.of(
                Map.of(
                    "type", "dog",
                    "name", "Fluffy"
                ),
                Map.of(
                    "type", "dragon",
                    "name", "Norbert"
                ),
                Map.of(
                    "type", "hippogriff",
                    "name", "Buckbeak"
                )
            )
        );

    JsonNode result = jsonNodeMapper.toJsonNode(payload);

    assertThat(result.get("name").asText()).isEqualTo("Hagrid");
    assertThat(result.get("dob").asText()).isEqualTo("1928-12-06");
    assertThat(result.get("height").asInt()).isEqualTo(350);
    assertThat(result.get("isMuggle").asBoolean()).isFalse();

    assertThat(result.at("/address/street").asText())
        .isEqualTo("Hut");

    assertThat(result.at("/address/city").asText())
        .isEqualTo("Hogwarts");

    assertThat(result.at("/pets/0/type").asText())
        .isEqualTo("dog");

    assertThat(result.at("/pets/0/name").asText())
        .isEqualTo("Fluffy");

    assertThat(result.at("/pets/1/type").asText())
        .isEqualTo("dragon");

    assertThat(result.at("/pets/1/name").asText())
        .isEqualTo("Norbert");

    assertThat(result.at("/pets/2/type").asText())
        .isEqualTo("hippogriff");

    assertThat(result.at("/pets/2/name").asText())
        .isEqualTo("Buckbeak");
  }

  @Test
  void toJsonNode_shouldConvertNullMapToJsonNode() {
    JsonNode result = jsonNodeMapper.toJsonNode(null);

    assertThat(result.isNull()).isTrue();
  }
}
