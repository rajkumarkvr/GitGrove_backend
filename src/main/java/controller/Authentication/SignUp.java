package controller.Authentication;

import java.io.IOException;
import java.security.Key;
import java.util.Date;

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
import utils.JSONHandler;

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

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject userObj = JSONHandler.parse(request.getReader());

		String username = userObj.getString("username").trim().toLowerCase();
		String password = userObj.getString("password").trim();
		String email = userObj.getString("email").trim().toLowerCase();

		String userAgent = request.getHeader("User-Agent");
		User user = null;
		boolean isSignedUp = false;
		
		if(UserDAO.getInstance().userNameExists(username)) {
			response.setStatus(400);
			response.getWriter().write("{\"error\": \"Username already exists\"}");
			return;
		}
		
		if(UserDAO.getInstance().emailExists(email)) {
			response.setStatus(400);
			response.getWriter().write("{\"error\": \"Email already exists\"}");
			return;
		}

		if (username != null || password != null || email != null) {
			user = UserDAO.getInstance().signUp(username, email, password);
			
			if (user != null) {
				isSignedUp = true;
			}
		}
		
		


		if (isSignedUp) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", user.getUsername());
			jsonObject.put("email", user.getEmailaddress());
			jsonObject.put("profile_url", user.getProfile_url());

			String token = Jwts.builder().setSubject(username).setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)).signWith(SECRET_KEY)
					.compact();

			Cookie cookie = new Cookie(COOKIE_KEY + username, token);
			System.out.println("Successfull");
			SessionDAO.getInstance().storeSession(user.getId(), token, userAgent);
			response.setStatus(200);
			response.addCookie(cookie);
			response.setHeader("Authorization", "Bearer " + token);

			JSONObject wrappedJsonObject = new JSONObject();
			wrappedJsonObject.put("user", jsonObject);
			response.getWriter().write(wrappedJsonObject.toString());

		}

		else {
			response.setStatus(400);
			response.getWriter().write("{\"error\": \"Invalid inputs\"}");
		}

	}

}
