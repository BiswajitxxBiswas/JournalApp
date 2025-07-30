package net.biswajit.journalApp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60 * 2;

    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7;


    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }


    public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName, ACCESS_TOKEN_VALIDITY);
    }


    public String generateRefreshToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userName, REFRESH_TOKEN_VALIDITY);
    }

    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String type = (String) claims.get("type");
        return "refresh".equals(type);
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public String extractUserName(String token){
        return extractAllClaims(token).getSubject();
    }

    private String createToken(Map<String, Object> claims, String subject, long validityMillis) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validityMillis))
                .signWith(getSigningKey())
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return createToken(claims, subject, ACCESS_TOKEN_VALIDITY);
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
