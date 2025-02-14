package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.User;
import models.dao.UserDAO;
import utils.JSONHandler;

/**
 * Servlet implementation class UpdateUserProfile
 */

public class UpdateUserProfile extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
 
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject jsonData = JSONHandler.parse(request.getReader());
		String username = jsonData.getString("oldusername");
		String newusername = jsonData.getString("username").toLowerCase();
		String email = jsonData.getString("email").toLowerCase();
		String profile_url = jsonData.getString("profile_url");
		String oldemail =jsonData.getString("oldemail");

		System.out.println(username+" new"+newusername+""+profile_url+""+email);
		if(username.trim().isEmpty()||newusername.isEmpty()||email.isEmpty()||profile_url.isEmpty()) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Invalid user input.\"}");
		}
		
	
		if ((!username.equals(newusername) && UserDAO.getInstance().userNameExists(newusername))) {
		    response.setStatus(400);
		    response.getWriter().write("{\"error\": \"This username has already been taken.\", \"isUsername\": true}");
		    return;
		}
		if (!email.equals(oldemail) && UserDAO.getInstance().emailExists(email)) {
		    response.setStatus(400);
		    response.getWriter().write("{\"error\": \"Email address already exists\", \"isUsername\": false}");
		    return;
		}

		
		User user = new User(newusername,email,profile_url);
		User olduser = new User(username,oldemail,profile_url);
		System.out.println("Updated");
		User usr = UserDAO.getInstance().updateUserProfile(username, user,olduser);
		if(usr!=null) {
			System.out.println("entered");
			JSONObject ouputUserObject = new JSONObject();
			response.setStatus(200);
			ouputUserObject.put("username", user.getUsername());
			ouputUserObject.put("email", user.getEmailaddress());
			ouputUserObject.put("profile_url", user.getProfile_url());
			response.getWriter().write(ouputUserObject.toString());
		}else {
			System.out.println(usr);
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Error to update the user\"}");
		}
	}

}
