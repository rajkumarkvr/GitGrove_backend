package controller.Authentication;

import java.io.IOException;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.User;
import models.dao.SessionDAO;
import models.dao.UserDAO;

/**
 * Servlet implementation class SignUp
 */
//@WebServlet("/SignUp")
public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private static final String COOKIE_KEY = "gitgrove_";
       
    public SignUp() {
        super();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username").trim();
		String password = request.getParameter("password").trim();
		String email = request.getParameter("email").trim();
		String profile_url = request.getParameter("profile_url").trim();
		String userAgent = request.getHeader("User-Agent");
		
		User user = null;
		boolean isSignedUp = false;
		
		if(username != null || password != null || email != null || profile_url != null) {
			user =  UserDAO.getInstance().signUp(username, email, password, profile_url);
			if(user != null)
				isSignedUp = true;
		}
		
		
		JSONObject jsonObject = new JSONObject();
		
		if(isSignedUp) {
			jsonObject.put("username", user.getUsername());
			jsonObject.put("email", user.getEmailaddress());
			jsonObject.put("profile_url", user.getProfile_url());
			response.setStatus(200);
			
			String token = Jwts.builder()
	                .setSubject(username)
	                .setIssuedAt(new Date())
	                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
	                .signWith(SECRET_KEY) 
	                .compact();
				
			Cookie cookie = new Cookie(COOKIE_KEY+username, token);
			
			SessionDAO.getInstance().storeSession(user.getId(), token, userAgent);
			response.addCookie(cookie);
			response.setHeader("Authorization", "Bearer " + token);	
		}
			
		else {
			response.setStatus(400);
		}
		
		JSONObject wrappedJsonObject = new JSONObject();
		wrappedJsonObject.put("user", jsonObject);
		
		response.getWriter().write(wrappedJsonObject.toString());
		
	}

}
