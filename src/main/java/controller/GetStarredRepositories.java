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


public class GetStarredRepositories extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       

    public GetStarredRepositories() {
        super();
    }
    
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String userName = request.getParameter("username");
		int userId = UserDAO.getInstance().getUserId(userName);
		
		ArrayList<Repository> repositoryList = RepositoryDAO.getInstance().getStarredRepositoriesByUser(userId);
		
		if(repositoryList == null || repositoryList.size() == 0) {
			response.getWriter().write("{\"error\" : \"No repository exists\"}");
			return;
		}
		
		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
		
		for(Repository repository : repositoryList) {
			String repoOwnerName = RepositoryDAO.getInstance().getOwnerName(repository.getId());
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", repository.getId());
			jsonObject.put("name", repository.getName());
			jsonObject.put("description", repository.getDescription());
			LocalDateTime lastCommitDate = RepositoryManager.getLastCommitedTime(repoOwnerName, repository.getName());
			
	
			if(lastCommitDate == null) {
				lastCommitDate = repository.getCreatedAt();
			}
			
		
			jsonObject.put("updated",lastCommitDate.toString());
			jsonObject.put("ownername", repoOwnerName);
			jsonObject.put("stars", repository.getStars_count());
			jsonObject.put("created_at",repository.getCreatedAt().toString() );
			jsonObject.put("url", "git@172.17.23.190:/opt/repo/"+repoOwnerName+"/"+repository.getName()+".git");
			jsonList.add(jsonObject);
		}
		
		JSONArray jsonArray = new JSONArray(jsonList);
		
		JSONObject jsonResponse = new JSONObject();
		
		jsonResponse.put("repositories", jsonArray);
		
		response.setStatus(200);
		response.getWriter().write(jsonResponse.toString());
		
	}

}
