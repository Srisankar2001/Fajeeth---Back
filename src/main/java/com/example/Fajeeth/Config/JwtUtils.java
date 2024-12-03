package com.example.Fajeeth.Config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtils {
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
    @Value("${spring.app.jwtAccessTokenExpiration}")
    private int jwtAccessTokenExpiration;
    @Value("${spring.app.jwtRefreshTokenExpiration}")
    private int jwtRefreshTokenExpiration;

    public String getJwtFromHeader(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(token != null && token.startsWith("Bearer ")){
            return token.substring(7);
        }
        return null;
    }

    public HashMap<String,String> generateAccessToken(UserDetails userDetails){
        String username = userDetails.getUsername();
        Date expireAt = new Date(System.currentTimeMillis() + jwtAccessTokenExpiration);
        String accessToken = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expireAt)
                .signWith(key())
                .compact();
        HashMap<String,String> map = new HashMap<>();
        map.put("token",accessToken);
        map.put("expireAt", String.valueOf(expireAt));
        return map;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshTokenExpiration))
                .signWith(key())
                .compact();
    }
    public String getUsernameFromJwtToken(String token){
        return Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token).getPayload().getSubject();
    }
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public boolean validateJwtToken(String authToken){
        try{
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return true;
        }catch (Exception e){
           return false;
        }
    }
}
