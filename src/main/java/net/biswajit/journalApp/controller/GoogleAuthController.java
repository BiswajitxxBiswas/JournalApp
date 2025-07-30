package net.biswajit.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.biswajit.journalApp.entity.User;
import net.biswajit.journalApp.repository.UserRepository;
import net.biswajit.journalApp.service.UserService;
import net.biswajit.journalApp.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.ResponseCookie;
import java.util.*;

import static net.biswajit.journalApp.service.UserService.passwordEncoder;

@RestController
@RequestMapping("/auth/google")
@Slf4j
public class GoogleAuthController {

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

    @GetMapping("/callback")
    public ResponseEntity<?> handleGoogleCallback(@RequestParam String code) {
        try {
            final String tokenEndpoint = "https://oauth2.googleapis.com/token";

            Map<String, String> params = new HashMap<>();
            params.put("code", code);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("redirect_uri", redirectUri);
            params.put("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<?> request = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful() || !tokenResponse.hasBody()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token received from Google.");
            }

            String idToken = (String) tokenResponse.getBody().get("id_token");
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token received.");
            }

            String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            if (userInfoResponse.getStatusCode() != HttpStatus.OK || userInfoResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user info from Google.");
            }

            Map userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");
            if (email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Google account has no email.");
            }

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setUserName(email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRoles(Collections.singletonList("USER"));
                user.setMailVerify(true);
                userRepository.save(user);
            }

            String jwtToken = jwtUtil.generateToken(email);

            ResponseCookie accessCookie = ResponseCookie.from("access_token", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(5 * 60 * 60L)
                    .sameSite("Lax")
                    .build();

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HttpHeaders.SET_COOKIE, accessCookie.toString());

            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(Map.of("user",user));

        } catch (Exception e) {
            log.error("Exception occurred while handling Google callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication failed.");
        }
    }
}
