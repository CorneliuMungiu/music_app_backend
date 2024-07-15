package com.backend.musicApp.services;

import com.backend.musicApp.dto.AccessTokenDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class SecurityService {
    private final String AUTH_URL = "http://localhost:9000/realms/musicAppRealm/protocol/openid-connect/token";
    RestTemplate restTemplate = new RestTemplate();

    /**
     * Authenticates the user using the provided authorization code.
     *
     * @param code The authorization code received from the authorization server.
     * @return AccessTokenDto containing the access token and other information.
     */
    public AccessTokenDto authUser(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "music_app_client");
        map.add("client_secret", "CLIENT_SECRET");
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", "http://localhost:8080/api/auth");
        map.add("scope", "offline_access");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<AccessTokenDto> response = restTemplate
                .exchange(AUTH_URL,
                        HttpMethod.POST,
                        entity,
                        AccessTokenDto.class);
        return response.getBody();
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken The refresh token to use for obtaining a new access token.
     * @return AccessTokenDto containing the new access token and other information.
     */
    public AccessTokenDto refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "music_app_client");
        map.add("client_secret", "CLIENT_SECRET");
        map.add("grant_type", "refresh_token");
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<AccessTokenDto> response = restTemplate
                .exchange(AUTH_URL, HttpMethod.POST, entity, AccessTokenDto.class);

        return response.getBody();
    }

}
