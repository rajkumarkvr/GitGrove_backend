package controller.pullrequests;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import enums.PullRequestStatus;
import models.dao.BranchDAO;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import services.MergeHandler;
import utils.JSONHandler;


public class MergePullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public MergePullRequest() {
        super();
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String ownerName = jsonObject.optString("ownername");
		String repoName = jsonObject.optString("reponame");
		String requestCreaterName = jsonObject.optString("requesterName");
		String sourceBranch = jsonObject.optString("sourceBranch");
		String targetBranch = jsonObject.optString("targetBranch");
		
		if(repoName == null || requestCreaterName == null || sourceBranch == null || targetBranch == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, ownerId);
		int requesterId = UserDAO.getInstance().getUserId(requestCreaterName);
		int sourceBranchId = BranchDAO.getInstance().getBranchId(repoId, sourceBranch);
		int targetBranchId = BranchDAO.getInstance().getBranchId(repoId, targetBranch);
		
		if(ownerId<0 || repoId < 0 || requesterId < 0 || sourceBranchId < 0 || targetBranchId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		String repoPath = "/opt/repo/"+ownerName+"/"+repoName+".git";
		Map<String, int[][]> conflicts =  MergeHandler.getInstance().mergeBranches(repoPath, targetBranch, sourceBranch);
		
		if(conflicts.isEmpty()) {
			PullRequestDAO.getInstance().changeStatus(repoId, sourceBranchId, targetBranchId, requesterId, PullRequestStatus.MERGED);
			response.setStatus(200);
			response.getWriter().write("{\"message\" :\"Merge successful\"}");
			return;
		}
		
		else {
	        ObjectMapper objectMapper = new ObjectMapper();
	        String jsonResponse = objectMapper.writeValueAsString(conflicts);

	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.setStatus(200);
	        response.getWriter().write(jsonResponse);
		}
		
	}

}
