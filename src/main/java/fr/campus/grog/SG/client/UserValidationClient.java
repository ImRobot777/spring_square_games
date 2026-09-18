package fr.campus.grog.SG.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class UserValidationClient {

    private final RestClient restClient;

    // Constructor: inject remote base URL and initialize RestClient
    public UserValidationClient(@Value("${users.service.url}") String usersServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(usersServiceUrl)
                .build();
    }

    // Call Square Users service to check if user exists
    public boolean isUserValid(UUID userId) {
        try {
            Boolean isValid = this.restClient.get()
                    .uri("/users/{userId}/valid", userId)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(isValid);
        } catch (Exception e) {
            // Fallback: if remote service is unreachable or errors out, consider user invalid
            return false;
        }
    }
}
