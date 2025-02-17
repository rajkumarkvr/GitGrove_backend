package controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import enums.Collaborator_Request;
import models.User;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;


public class FindPeople extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
    public FindPeople() {
        super();
        // TODO Auto-generated constructor stub
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String query = request.getParameter("searchterm");
		String repoName = request.getParameter("reponame");
		
		if(username == null || query == null) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Invalid input\"}");
			return;
		}
		
		int owner_id = UserDAO.getInstance().getUserId(username);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName);

		if(owner_id<0) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Invalid user\"}");
			return;
		}
		

		ArrayList<User> usersList = UserDAO.getInstance().getAllUserBySearch(owner_id, query);
		
		if(usersList == null || usersList.size() == 0 ) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"There is no users\"}");
		}
		
		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
		
	
		for(User user : usersList) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", user.getId());
			jsonObject.put("username", user.getUsername());
			jsonObject.put("email", user.getEmailaddress());
			jsonObject.put("avatar", user.getProfile_url());
			System.out.println("usrid"+user.getId());
		
			Collaborator_Request cr = RepositoryDAO.getInstance().isAlreadyCollaboratorOrRequested(owner_id,user.getId(),repoId);
			if(cr==null) {
			jsonObject.put("isRequested", false);
			}else {
				jsonObject.put("isRequested", true);
				jsonObject.put("status",cr.name());
			}
			jsonList.add(jsonObject);
		}
		
		JSONArray jsonArray = new JSONArray(jsonList);
		
		JSONObject jsonResponse = new JSONObject();
		
		jsonResponse.put("users", jsonArray);

		response.setStatus(200);
		response.getWriter().write(jsonResponse.toString());
		
		
	}

}
