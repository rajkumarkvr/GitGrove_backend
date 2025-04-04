package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;


import models.Repository;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import services.RepositoryManager;

/**
 * Servlet implementation class RepositoryDetails
 */

public class RepositoryDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public RepositoryDetails() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = request.getParameter("username");
	
		if(username == null) {
			response.setStatus(400);
			response.getWriter().write("{\"error\"}");
		}
		
		int userId = UserDAO.getInstance().getUserId(username);
		
		if(userId<0) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"User doesn't exist\"}");
			return;
		}
		
		ArrayList<Repository> repositoryList = RepositoryDAO.getInstance().getAllRepositoryOfUser(userId);

		if(repositoryList == null || repositoryList.size()==0 ) {
			response.setStatus(204);
			response.getWriter().write("{\"message\" : \"No repository exists\"}");
			return;
		}
		
		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
		
		for(Repository repository : repositoryList) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", repository.getId());
			jsonObject.put("name", repository.getName());
			jsonObject.put("description", repository.getDescription());
			LocalDateTime lastCommitDate = RepositoryManager.getLastCommitedTime(username, repository.getName());	
	
			if(lastCommitDate == null) {
				lastCommitDate = repository.getCreatedAt();
			}
		
			jsonObject.put("updated",lastCommitDate.toString());
			jsonObject.put("stars", repository.getStars_count());
			jsonObject.put("created_at",repository.getCreatedAt().toString() );
			jsonObject.put("url", "git@172.17.23.190:/opt/repo/"+username+"/"+repository.getName()+".git");
			jsonObject.put("isStarred", RepositoryDAO.getInstance().isRepositoryLikedByUser(repository.getId(), userId));
			jsonObject.put("visibility", repository.getVisibility().name());
			jsonObject.put("role", repository.getRole().name());
			jsonObject.put("ownername", repository.getOwnerName());
			jsonList.add(jsonObject);
		}
		
		JSONArray jsonArray = new JSONArray(jsonList);
		
		JSONObject jsonResponse = new JSONObject();
		
		jsonResponse.put("repositories", jsonArray);
		
		response.setStatus(200);
		response.getWriter().write(jsonResponse.toString());
		
	}

}
