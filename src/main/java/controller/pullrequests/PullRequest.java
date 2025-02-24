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
   
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String ownerName = jsonObject.getString("ownerName").trim();		
		String repoName = jsonObject.getString("reponame").trim();	
		String requestCreaterName = jsonObject.getString("requesterName").trim();
		String sourceBranch = jsonObject.getString("sourceBranch").trim();
		String targetBranch = jsonObject.getString("targetBranch").trim();
		String description = jsonObject.getString("description").trim();
		String title = jsonObject.getString("title").trim();
		
		
		
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
		
		int requestId = PullRequestDAO.getInstance().createPullRequest(sourceBranchId, targetBranchId, requestCreaterId, description, title);
		
		if(requestId < 0) {
			response.setStatus(200);
			response.getWriter().write("{\"message\" :\"Sorry ,error in pull request creation\"}");
			return;
		}
		
		response.setStatus(200);
		JSONObject resultJson = new JSONObject();
		resultJson.put("id", requestId);
		response.getWriter().write(resultJson.toString());
		
	}
}

