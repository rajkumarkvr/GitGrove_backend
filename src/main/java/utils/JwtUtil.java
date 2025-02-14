package utils;

import io.jsonwebtoken.*;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import models.dao.UserDAO;
import io.jsonwebtoken.io.Decoders;
import javax.crypto.SecretKey;

public class JwtUtil {
	private static final String SECRET = "uLD6znrRh/pz1+pnrgcuSgvNG5rNReeuRBeh+EydeR8=";
	private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
	
	static JwtUtil jwtUtil = null;
	
	
	private JwtUtil() {
		
	}
	
	public static JwtUtil getInstance() {
		if(jwtUtil == null) {
			jwtUtil = new JwtUtil();
		}
		return jwtUtil;
	}

	public String generateToken(String username) {
		return Jwts.builder().subject(username) // New method in 0.12.6
				.issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + 3600 * 10000)) // 1-hour expiry
				.signWith(SECRET_KEY) // No need to specify algorithm explicitly
				.compact();
	}

	public Claims validateToken(String token) {
		try {
			return Jwts.parser().verifyWith(SECRET_KEY) // New method in 0.12.6
					.build().parseSignedClaims(token).getPayload();
		} catch (JwtException e) {
			throw new RuntimeException("Invalid or expired JWT token", e);
		}
	}
	
	public String validateAndExtendToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String username = claims.getSubject();
            
            if(UserDAO.getInstance().userNameExists(username)) {
            	Date expiration = claims.getExpiration();
                long remainingTime = expiration.getTime() - System.currentTimeMillis();

                // If the token is close to expiring (e.g., < 10 minutes left), extend it
                if (remainingTime < (10 * 60 * 1000)*24) { 
                    return generateToken(claims.getSubject()); // Generate a new token
                }
            }
            
            else {
            	throw new RuntimeException("Token expired, please log in again.");            }
            
            return token;
            
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired, please log in again.");
        } catch (JwtException e) {
            throw new RuntimeException("Invalid token.");
        }
    }


}
