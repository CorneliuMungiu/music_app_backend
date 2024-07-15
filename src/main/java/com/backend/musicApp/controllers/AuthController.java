package com.backend.musicApp.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.musicApp.dto.AccessTokenDto;
import com.backend.musicApp.model.User;
import com.backend.musicApp.repository.UserRepository;
import com.backend.musicApp.services.SecurityService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
public class AuthController {
    private final SecurityService securityService;
    private final UserRepository userRepository;

    public AuthController(SecurityService securityService, UserRepository userRepository) {
        this.securityService = securityService;
        this.userRepository = userRepository;
    }


    /**
     * Handles the OAuth authentication process.
     *
     * @param code The authorization code received from the OAuth provider.
     * @param httpServletResponse The HttpServletResponse object to add cookies to.
     * @return A RedirectView object that redirects the user to the homepage.
     */
    @GetMapping
    public RedirectView auth(@RequestParam String code, HttpServletResponse httpServletResponse) {
        AccessTokenDto accessTokenDto = securityService.authUser(code);
        Cookie cookieAccessToken = new Cookie("MusicAppAccessToken", accessTokenDto.accessToken());
        Cookie cookieRefreshToken = new Cookie("MusicAppRefreshToken", accessTokenDto.refreshToken());
        cookieAccessToken.setPath("/");
        cookieRefreshToken.setPath("/");

        httpServletResponse.addCookie(cookieAccessToken);
        httpServletResponse.addCookie(cookieRefreshToken);

        saveUserIfNotExists(accessTokenDto.accessToken());
        return new RedirectView("http://localhost:3000/");
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param requestBody A map containing the refresh token.
     * @return A ResponseEntity containing the new access token DTO.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenDto> refreshTokens(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        AccessTokenDto accessTokenDto = securityService.refreshToken(refreshToken);
        return ResponseEntity.ok(accessTokenDto);
    }


    /**
     * Logs the user out by redirecting to the logout URL of the OAuth provider.
     *
     * @return A RedirectView object that redirects the user to the OAuth provider's logout endpoint.
     */
    @GetMapping("/logout")
    public RedirectView logout(){
        return new RedirectView("http://localhost:9000/realms/musicAppRealm/protocol/openid-connect/logout?post_logout_redirect_uri=http://localhost:8080/api/auth/unauth&client_id=music_app_client");
    }

    /**
     * Handles the unauthentication process by clearing authentication cookies.
     *
     * @param httpServletResponse The HttpServletResponse object to add cookies to.
     * @return A RedirectView object that redirects the user to the homepage.
     */
    @GetMapping("/unauth")
    public RedirectView unauth(HttpServletResponse httpServletResponse){
        Cookie accessTokenCookie = new Cookie("MusicAppAccessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setMaxAge(0);

        Cookie refreshTokenCookie = new Cookie("MusicAppRefreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(0);

        httpServletResponse.addCookie(accessTokenCookie);
        httpServletResponse.addCookie(refreshTokenCookie);


        return new RedirectView("http://localhost:3000/");
    }

    /**
     * Saves the user to the repository if they don't already exist.
     *
     * @param accessToken The access token containing the user's information.
     */
    private void saveUserIfNotExists(String accessToken) {
        DecodedJWT decodedJWT = JWT.decode(accessToken);
        String username = decodedJWT.getClaim("name").asString();
        String email = decodedJWT.getClaim("email").asString();

        if (userRepository == null || !userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(username);
            user.setEmail(email);
            user.setPlayLists(new ArrayList<>());
            assert userRepository != null;
            userRepository.insert(user);
        }
    }

}
