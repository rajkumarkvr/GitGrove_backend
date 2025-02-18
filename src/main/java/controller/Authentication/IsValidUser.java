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
import utils.CookieUtil;
import utils.IPLocationInfo;
import utils.JSONHandler;
import utils.JwtUtil;


public class IsValidUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			JSONObject userObj = JSONHandler.parse(request.getReader());
		
		String usernameOrEmail =  userObj.getString("identifier").trim().toLowerCase();
		String password = userObj.getString("password").trim();
		
		
		if(!UserDAO.getInstance().userNameOrEmailExists(usernameOrEmail)) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Username or Email address doesn't exist\"}");
			return;
		}
		
		User user = UserDAO.getInstance().signIn(usernameOrEmail, password);
			
		JSONObject jsonObject = new JSONObject();


		if(user!=null) {

			response.setStatus(200);
			JSONObject res = new JSONObject();
			res.put("message", "User exists");
			response.getWriter().write(res.toString());

		}

		else {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid password\"}");
		}

		
	
	}

}
