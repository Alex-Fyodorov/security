package com.globus.biometric_auth_service.util;

import com.globus.biometric_auth_service.exception.UserNotFoundException;
import com.globus.biometric_auth_service.model.BiometricSettings;
import com.globus.biometric_auth_service.service.BiometricAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final BiometricAuthService biometricAuthService;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.lifetime}")
    private Integer lifetime;

    public String generateToken(UserDetails userDetails) {
        int userId = Integer.parseInt(userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        List<String> rolesList = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        claims.put("roles", rolesList);
        Optional<BiometricSettings> settings = biometricAuthService.findByUserId(userId);
        if (settings.isPresent()) {
            claims.put("accountId", settings.get().getId());
            claims.put("userId", settings.get().getUserId());
        } else {
            throw new UserNotFoundException("User with Id " + userId + " not found");
        }

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + lifetime);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(issuedDate)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public Integer getUserIdFromToken(String token) {
        String username = getAllClaimsFromToken(token).getSubject();
        return Integer.parseInt(username);
    }

    public String getDeviceInfoFromToken(String token) {
        return getAllClaimsFromToken(token).get("device", String.class);
    }

    public List<String> getRoles(String token) {
        return getClaimFromToken(token, (Function<Claims, List<String>>)
                claims -> claims.get("roles", List.class));
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Base64.getDecoder().decode(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
