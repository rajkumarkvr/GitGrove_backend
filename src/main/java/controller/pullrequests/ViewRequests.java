package controller.pullrequests;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import models.PullRequest;


public class ViewRequests extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	public ViewRequests() {
        super();
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String ownerName = request.getParameter("ownername");
		String repoName = request.getParameter("reponame");
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, ownerId);
		
		ArrayList<PullRequest> pullRequests =  PullRequestDAO.getInstance().getPullRequest(repoId);
		
		JSONArray pullRequestsJsonArray = new JSONArray();
		
		for(PullRequest pullRequest : pullRequests) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", pullRequest.getId());
			jsonObject.put("description", pullRequest.getDescription());
			jsonObject.put("title", pullRequest.getTitle());
			jsonObject.put("sourceBranch", pullRequest.getSourceBranch().getName());
			jsonObject.put("targetBranch", pullRequest.getTargetBranch().getName());
			jsonObject.put("status", pullRequest.getStatus().toString());
			jsonObject.put("createdAt", pullRequest.getCreatedAt());
			jsonObject.put("updatedAt", pullRequest.getUpdatedAt());
			jsonObject.put("createrName", pullRequest.getCreatedBy().getUsername());
			jsonObject.put("createrEmail", pullRequest.getCreatedBy().getEmailaddress());
			jsonObject.put("createrAvatar", pullRequest.getCreatedBy().getProfile_url());
			pullRequestsJsonArray.put(jsonObject);
		}
		
		JSONObject resultJson = new JSONObject();
		resultJson.put("data", resultJson);
		
		response.getWriter().write(resultJson.toString());
	}

}
