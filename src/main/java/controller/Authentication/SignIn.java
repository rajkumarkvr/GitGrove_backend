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

public class SignIn extends HttpServlet {

	private static final long serialVersionUID = 1L;
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
			
			String token = JwtUtil.getInstance().generateToken(user.getUsername());

			Cookie cookie = new Cookie(COOKIE_KEY + user.getUsername(), token);
			cookie.setPath("/");  // Make it accessible everywhere in your domain
			cookie.setHttpOnly(false);  // Try setting to false for testing
			cookie.setSecure(false);  // If you're testing on HTTP, must be false
			response.addCookie(cookie);

			System.out.println(token);
				String ipaddress =IPLocationInfo.getIPAddress(request);
				String location = IPLocationInfo.getLocationInfo(ipaddress);

			SessionDAO.getInstance().storeSession(user.getId(), token, userAgent,ipaddress,location);
			response.addCookie(cookie);
//			response.setHeader("authorization", "Bearer " + token);
			
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
