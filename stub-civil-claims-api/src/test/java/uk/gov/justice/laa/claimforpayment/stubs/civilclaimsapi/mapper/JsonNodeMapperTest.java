package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonNodeMapperTest {

  private JsonNodeMapper jsonNodeMapper;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    jsonNodeMapper = new JsonNodeMapper(objectMapper);
  }

  @Test
  void toMap_shouldReturnNull_whenPayloadIsNull() {
    Map<String, Object> result = jsonNodeMapper.toMap(null);
    assertThat(result).isNull();
  }


  @Test
  void toMap_shouldConvertJsonNodeToMap() throws Exception {
    JsonNode jsonNode = objectMapper.readTree("""
        {
          "name": "John",
          "age": 30
        }
        """);

    Map<String, Object> result = jsonNodeMapper.toMap(jsonNode);

    assertThat(result)
        .containsEntry("name", "John")
        .containsEntry("age", 30);
  }


  @Test
  void toMap_shouldConvertTextualJsonNodeToMap() {
    JsonNode jsonNode =
        objectMapper.getNodeFactory()
            .textNode("""
                {"name":"John","age":30}
                """);

    Map<String, Object> result = jsonNodeMapper.toMap(jsonNode);

    assertThat(result)
        .containsEntry("name", "John")
        .containsEntry("age", 30);
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
    Map<String, Object> payload = Map.of(
        "name", "John",
        "age", 30);

    JsonNode result = jsonNodeMapper.toJsonNode(payload);

    assertThat(result.get("name").asText()).isEqualTo("John");
    assertThat(result.get("age").asInt()).isEqualTo(30);
  }

  @Test
  void toJsonNode_shouldConvertNullMapToJsonNode() {
    JsonNode result = jsonNodeMapper.toJsonNode(null);

    assertThat(result.isNull()).isTrue();
  }
}
