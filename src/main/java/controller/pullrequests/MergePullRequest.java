package controller.pullrequests;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import enums.PullRequestStatus;
import models.User;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import services.MergeHandler;

public class MergePullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public MergePullRequest() {
        super();
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String idStr = request.getParameter("id");
		String userName = request.getParameter("username");
		
		if(idStr == null || userName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int PRid = Integer.parseInt(idStr);
		
		if(PRid < 0 || !PullRequestDAO.getInstance().isIdExists(PRid)) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request\"}");
			return;
		}
		
		User author = PullRequestDAO.getInstance().getCreater(PRid);
		
		if(author == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request\"}");
			return;
		}
		
		User commiter = UserDAO.getInstance().getUserByUserName(userName);
		
		if(commiter == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid current user\"}");
			return;
		}
		
		int repoId = PullRequestDAO.getInstance().getRepoId(PRid);
	
		String repoPath = RepositoryDAO.getInstance().getRepoPath(repoId);
		
		ArrayList<String> branches = PullRequestDAO.getInstance().getTargetAndSourceBranch(PRid);
		boolean isMerged=false;
			try {
				System.out.println("Checking");
				isMerged =  MergeHandler.getInstance().mergeBranches(repoPath,branches.get(1),branches.get(0),"",author,commiter);
			}catch(Exception e) {
					System.out.println("Merge pull request: "+e.getMessage());
			}
		if(isMerged) {
			PullRequestDAO.getInstance().changeStatus(PRid, PullRequestStatus.MERGED);
			response.setStatus(200);
			response.getWriter().write("{\"message\" :\"Merge successful\"}");
			return;
		}
		
		else {
	  
	        response.setStatus(400);	
	        response.getWriter().write("{\"message\" :\"Can't auto merge due to conflicts\"}");																		
		}																														
		
	}

}
