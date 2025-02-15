package controller.Authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.User;
import models.dao.SessionDAO;
import models.dao.UserDAO;
import utils.IPLocationInfo;
import utils.JSONHandler;
import utils.JwtUtil;


//@WebServlet("/SignUp")
public class SignUp extends HttpServlet {
	private static final long serialVersionUID = 1L;

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

			String token = JwtUtil.getInstance().generateToken(user.getUsername());
			
			Cookie cookie = new Cookie(COOKIE_KEY + user.getUsername(), token);
			String ipaddress =IPLocationInfo.getIPAddress(request);
			String location = IPLocationInfo.getLocationInfo(ipaddress);
			SessionDAO.getInstance().storeSession(user.getId(), token, userAgent,ipaddress,location);
			response.setStatus(200);
			response.addCookie(cookie);
//			response.setHeader("Authorization", "Bearer " + token);

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
