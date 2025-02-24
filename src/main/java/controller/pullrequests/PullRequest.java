package controller.pullrequests;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.BranchDAO;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.JSONHandler;


public class PullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String ownerName = jsonObject.optString("ownerName");		
		String repoName = jsonObject.optString("reponame");	
		String requestCreaterName = jsonObject.optString("requesterName");
		String sourceBranch = jsonObject.optString("sourceBranch");
		String targetBranch = jsonObject.optString("targetBranch");
		String description = jsonObject.optString("description");
		String title = jsonObject.optString("title");
		
		if(ownerName == null || repoName == null || requestCreaterName == null || sourceBranch == null || targetBranch ==null || description == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		if(title == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"No Title\"}");
			return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, ownerId);
		int requestCreaterId = UserDAO.getInstance().getUserId(requestCreaterName);
		int sourceBranchId = BranchDAO.getInstance().getBranchId(repoId, sourceBranch);
		int targetBranchId = BranchDAO.getInstance().getBranchId(repoId, targetBranch);
		
		if(ownerId < 0 || requestCreaterId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid user\"}");
			return;
		}
		
		if(repoId<0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid Repository\"}");
			return;
		}
		
		if(sourceBranchId < 0 || targetBranchId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid branch\"}");
			return;
		}
		
		PullRequestDAO.getInstance().createPullRequest(sourceBranchId, targetBranchId, requestCreaterId, description, title);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}

