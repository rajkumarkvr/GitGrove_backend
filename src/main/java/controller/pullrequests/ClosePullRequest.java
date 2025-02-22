package controller.pullrequests;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import enums.PullRequestStatus;
import models.dao.BranchDAO;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.JSONHandler;


public class ClosePullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ClosePullRequest() {
        super();
        // TODO Auto-generated constructor stub
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
		
		PullRequestDAO.getInstance().changeStatus(repoId, sourceBranchId, targetBranchId, requesterId, PullRequestStatus.CLOSED);
		
	}

}
