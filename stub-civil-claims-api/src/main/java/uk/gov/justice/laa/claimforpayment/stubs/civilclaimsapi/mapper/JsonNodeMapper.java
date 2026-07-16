package uk.gov.justice.laa.claimforpayment.stubs.civilclaimsapi.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
* Mapping between Map and JsonNode.
*/
@Component
public class JsonNodeMapper {

  private final ObjectMapper objectMapper;

  public JsonNodeMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Map<String, Object> toMap(JsonNode payload) {
    if (payload == null) {
      return null;
    }

    if (payload.isTextual()) {
      try {
        payload = objectMapper.readTree(payload.textValue());
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("Unable to parse payload JSON", e);
      }
    }

    return objectMapper.convertValue(payload, new TypeReference<>() {});
  }

  public JsonNode toJsonNode(Map<String, Object> payload) {
    return objectMapper.valueToTree(payload);
  }
}