package com.ecommerce.api_gateway.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import java.security.Key;


@Service
public class JwtService {

    public void validateToken(final String token) {
        Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
    }

    private Key getSignKey() {
        byte[] key = Decoders.BASE64.decode("fbe777eb105eee640fb10da655901406eb9fb5f45666c6866e4f64f601bd358f");
        return Keys.hmacShaKeyFor(key);
    }



}
