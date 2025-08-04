package net.biswajit.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.UserRepository;
import net.biswajit.journalApp.service.RedisService;
import net.biswajit.journalApp.service.UserService;
import net.biswajit.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseCookie;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.biswajit.journalApp.service.UserService.passwordEncoder;

@RestController
@RequestMapping("/auth/google")
@Slf4j
public class GoogleAuthController {

    @Value("${frontendLink}")
    private String frontendLink;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RedisService redisService;

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            final String tokenEndpoint = "https://oauth2.googleapis.com/token";

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || !tokenResponse.hasBody()) {
                log.error("Token endpoint did not return valid response: {}", tokenResponse.getStatusCode());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token received from Google.");
            }

            Map<String, Object> tokenBody = tokenResponse.getBody();

            if (tokenBody.containsKey("error")) {
                log.error("Token endpoint error: {}", tokenBody.get("error_description"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenBody.get("error_description"));
            }

            String idToken = (String) tokenBody.get("id_token");
            if (idToken == null) {
                log.error("No id_token in token response: {}", tokenBody);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token received.");
            }

            final String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            if (!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
                log.error("Could not fetch user info from tokeninfo endpoint.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user info from Google.");
            }

            Map userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");
            if (email == null) {
                log.error("Google account does not provide email.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Google account has no email.");
            }

            User user = userService.findByEmail(email);

            if(user == null){
                user = new User();
                user.setEmail(email);
                user.setUserName(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRoles(Collections.singletonList("USER"));
                user.setMailVerify(true);
                userService.saveNewUser(user);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());

            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            redisService.set(
                    "refresh_token:"+ refreshToken,
                    user.getId(),
                    7 * 24 * 60 * 60L
            );

            ResponseCookie accessCookie = ResponseCookie
                    .from("access_token", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(24 * 60 * 60L)      // 24 hours in seconds
                    .sameSite("None")
                    .build();

            ResponseCookie refreshCookie = ResponseCookie
                    .from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60L) // 7 days in seconds
                    .sameSite("None")
                    .build();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
            responseHeaders.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
            responseHeaders.add(HttpHeaders.LOCATION, frontendLink+"/dashboard");

            return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(responseHeaders)
                    .build();

        } catch (Exception e) {
            log.error("Exception occurred while handling Google callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed.");
        }
    }
}
