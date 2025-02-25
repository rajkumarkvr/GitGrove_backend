package controller.pullrequests;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import enums.MergeStrategy;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import services.MergeHandler;


public class MergeConflictWithTheirs extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	
    public MergeConflictWithTheirs() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		String PRIdStr = request.getParameter("PRId");
		
		if(PRIdStr == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid input\"}");
			return;
		}
		
		int PRId = Integer.parseInt(PRIdStr);
		
		if(PRId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid pull request\"}");
			return;
		}
		
		int repoId = PullRequestDAO.getInstance().getRepoId(PRId);
		
		String repoPath = RepositoryDAO.getInstance().getRepoPath(repoId);
		
		ArrayList<String> branches = PullRequestDAO.getInstance().getTargetAndSourceBranch(PRId);
		
		MergeHandler.getInstance().mergeBranchConflict(repoPath, branches.get(1), branches.get(0), MergeStrategy.OURS);
		
		response.setStatus(200);
		response.getWriter().write("{\"message\" : \"Branches merged\"}");
		
	}

}
