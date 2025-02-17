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


public class ExploreRepositories extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
   
    public ExploreRepositories() {
        super();
        // TODO Auto-generated constructor stub
    }
  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String lengthStr = request.getParameter("page");
		String query = request.getParameter("search");
		String perPage = request.getParameter("per_page");
		if(query == null) {
			query = "";
		}
		String username = request.getParameter("username");
		
		
		if(username == null || perPage == null || lengthStr == null) {
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"Missing input\"}");
            return;
		}
		
		int startPoint = (Integer.parseInt(lengthStr)-1)*Integer.parseInt(perPage);
		System.out.println("Start point"+startPoint);
		int userId = UserDAO.getInstance().getUserId(username);
		int limit = Integer.parseInt(perPage);
		
		ArrayList<Repository> repositoryList = RepositoryDAO.getInstance().getAllRepositoryExceptCurrentUser(userId,limit,startPoint,query);

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
			
			JSONObject ownerJson = new JSONObject();
			ownerJson.put("username", repository.getOwnerName());
			ownerJson.put("avatar", UserDAO.getInstance().getAvatar(repository.getOwnerName()));
			jsonObject.put("owner", ownerJson);
			LocalDateTime dt = RepositoryManager.getLastCommitedTime(username, repository.getName());
			if(dt ==null) {
				dt =repository.getCreatedAt();
			}
			jsonObject.put("updated_at",dt.toString());
			jsonObject.put("stars", repository.getStars_count());
			jsonObject.put("isStarred", RepositoryDAO.getInstance().isRepositoryLikedByUser(repository.getId(), userId));
			jsonList.add(jsonObject);
		}
		
		JSONArray jsonArray = new JSONArray(jsonList);
		
		JSONObject jsonResponse = new JSONObject();
		
		jsonResponse.put("repositories", jsonArray);
		
		response.setStatus(200);
		response.getWriter().write(jsonResponse.toString());
	
	}	
}
