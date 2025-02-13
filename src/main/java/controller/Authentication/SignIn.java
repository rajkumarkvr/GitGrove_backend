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

public class SignIn extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private static final String COOKIE_KEY = "gitgrove_";

    public SignIn() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject userObj = JSONHandler.parse(request.getReader());
		
		String usernameOrEmail =  userObj.getString("identifier").trim().toLowerCase();
		String password = userObj.getString("password").trim();
		String userAgent =request.getHeader("User-Agent");

		System.out.println(usernameOrEmail+" pas"+password);
		System.out.println(UserDAO.getInstance().emailExists(usernameOrEmail));
		System.out.println(UserDAO.getInstance().userNameExists(usernameOrEmail));
		if(!UserDAO.getInstance().userNameOrEmailExists(usernameOrEmail)) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Username or Email address doesn't exist\"}");
			return;
		}
		
		User user = UserDAO.getInstance().signIn(usernameOrEmail, password);
			
		JSONObject jsonObject = new JSONObject();


		if(user!=null) {

			response.setStatus(200);
			jsonObject.put("username", user.getUsername());
			jsonObject.put("email", user.getEmailaddress());
			jsonObject.put("profile_url", user.getProfile_url());

			String token = Jwts.builder()
	                .setSubject(user.getUsername())
	                .setIssuedAt(new Date())
	                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
	                .signWith(SECRET_KEY)
	                .compact();

			Cookie cookie = new Cookie(COOKIE_KEY + user.getUsername(), token);
			cookie.setPath("/");  // Make it accessible everywhere in your domain
			cookie.setMaxAge((60 * 60 * 24) *2); // Set expiration (1 day)
			cookie.setHttpOnly(false);  // Try setting to false for testing
			cookie.setSecure(false);  // If you're testing on HTTP, must be false
			response.addCookie(cookie);

			System.out.println(token);
			

			SessionDAO.getInstance().storeSession(user.getId(), token, userAgent);
			response.addCookie(cookie);
			response.setHeader("authorization", "Bearer " + token);
			
			JSONObject wrappedJsonObject = new JSONObject();
			wrappedJsonObject.put("user", jsonObject);

			System.out.println("login");
			response.getWriter().write(wrappedJsonObject.toString());

		}

		else {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Invalid input\"}");
		}

		
	}

}
