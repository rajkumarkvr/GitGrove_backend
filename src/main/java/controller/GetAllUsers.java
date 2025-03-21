package controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import models.User;
import models.dao.UserDAO;

public class GetAllUsers extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	
    public GetAllUsers() {
        super();
        // TODO Auto-generated constructor stub
    }

    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userName = request.getParameter("username");
		
		if(userName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Invalid input\"}");
			return;
		}
		
		int userId = UserDAO.getInstance().getUserId(userName);
		
		if(userId<0) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Invalid user\"}");
			return;
		}
		
		ArrayList<User> usersList = UserDAO.getInstance().getAllUserExceptCurrent(userId);
		
		if(usersList == null || usersList.size() == 0 ) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"There is no users\"}");
		}
		
		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
		
		for(User user : usersList) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("username", user.getUsername());
			jsonObject.put("email", user.getEmailaddress());
			jsonObject.put("profile_url", user.getProfile_url());
			jsonObject.put("createdAt", user.  getCreatedAt());
			jsonList.add(jsonObject);
		}
		
		JSONArray jsonArray = new JSONArray(jsonList);
		
		JSONObject jsonResponse = new JSONObject();
		
		jsonResponse.put("users", jsonArray);
		
		response.setStatus(200);
		response.getWriter().write(jsonResponse.toString());
		
	}

}
