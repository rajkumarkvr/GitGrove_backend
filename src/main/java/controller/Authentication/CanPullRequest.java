package controller.Authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.RepositoryDAO;
import models.dao.UserDAO;


public class CanPullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public CanPullRequest() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String currentUserName = request.getParameter("username");
		String ownerName = request.getParameter("ownername");
		String repoName = request.getParameter("reponame");
		
		if(currentUserName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid user data\"}");
			return;
		}
		
		if(ownerName == null || repoName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid Repository data\"}");
			return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, ownerId);
		
		int currentUserId = UserDAO.getInstance().getUserId(currentUserName);
		
		if(currentUserId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid user\"}");
			return;
		}
		
		if(repoId < 0 || ownerId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid repository\"}");
			return;
		}
		
		boolean isCollaborator = RepositoryDAO.getInstance().canCollaborate(repoId, currentUserId);
		
		response.setStatus(200);
		if(isCollaborator) {
			response.getWriter().write("{\"isCollaborator\" :true}");
			return;
		}
		else {
			response.getWriter().write("{\"isCollaborator\" :false}");
			return;
		}
		
	}

}
