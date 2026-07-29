package com.shopmanager.message.sender.whatsapp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WhatsAppApiClient {

    private final RestTemplate restTemplate;
    private final WhatsAppProperties properties;

    /**
     * Sends a plain text WhatsApp message via the Meta (WhatsApp Cloud) API.
     * Returns the provider message id.
     */
    public String sendText(String toPhone, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toPhone);
        body.put("type", "text");
        body.put("text", Map.of("body", message));
        return sendMessage(body);
    }

    public String sendMessage(Map<String, Object> body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        properties.getBaseUrl() + "/" +
                                properties.getPhoneNumberId() + "/messages",
                        entity,
                        Map.class
                );

        // Meta returns { "messages": [ { "id": "wamid..." } ] }. Fail loudly
        // rather than NPE if the shape is unexpected.
        Map<?, ?> responseBody = response.getBody();
        if (responseBody == null) {
            throw new IllegalStateException("Empty response from WhatsApp API");
        }
        Object messages = responseBody.get("messages");
        if (!(messages instanceof List) || ((List<?>) messages).isEmpty()) {
            throw new IllegalStateException("Unexpected WhatsApp API response: " + responseBody);
        }
        Object first = ((List<?>) messages).get(0);
        return ((Map<?, ?>) first).get("id").toString();
    }
}