package controller.pullrequests;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import enums.MergeStrategy;
import models.User;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import services.MergeHandler;


public class MergeConflictWithTheirs extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	
    public MergeConflictWithTheirs() {
        super();
        // TODO Auto-generated constructor stub
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
		
		MergeHandler.getInstance().mergeBranches(repoPath, branches.get(1), branches.get(0), MergeStrategy.THEIRS.toString(),author,commiter);
		
		response.setStatus(200);
		response.getWriter().write("{\"message\" : \"Branches merged\"}");
		
	}

}
