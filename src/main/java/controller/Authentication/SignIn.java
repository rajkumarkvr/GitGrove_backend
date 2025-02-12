package controller.Authentication;

import java.util.Date;
import java.io.IOException;
import java.security.Key;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import models.User;
import models.dao.SessionDAO;
import models.dao.UserDAO;

public class SignIn extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private static final String COOKIE_KEY = "gitgrove_";
    
    public SignIn() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String usernameOrEmail = request.getParameter("username").trim();
		String password = request.getParameter("password").trim();
		String userAgent =request.getHeader("User-Agent");
		
		User user = null;
		boolean isSignedIn = false;
		
		if(usernameOrEmail != null || password != null) {
			user =  UserDAO.getInstance().signIn(usernameOrEmail, password);
			if(user != null)
				isSignedIn = true;
		}
		
		JSONObject jsonObject = new JSONObject();
		
		String username = usernameOrEmail;
		
		if(username.endsWith(".com")) {
			usernameOrEmail = user.getUsername();
		}
		
		if(isSignedIn) {
			response.setStatus(200);
			jsonObject.put("username", user.getUsername());
			jsonObject.put("email", user.getEmailaddress());
			jsonObject.put("profile_url", user.getProfile_url());
			
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
