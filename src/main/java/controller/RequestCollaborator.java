package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.RepositoryDAO;
import models.dao.UserDAO;


public class RequestCollaborator extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public RequestCollaborator() {
        super();
        // TODO Auto-generated constructor stub
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ownerName = request.getParameter("ownername");
		String inviteeName = request.getParameter("inviteename");
		String repoName = request.getParameter("reponame");
		
		if(ownerName == null || inviteeName == null || repoName == null) {
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"Missing invittename or reponame\"}");
            return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int inviteeId = UserDAO.getInstance().getUserId(inviteeName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName);
	
		if(ownerId<0 || inviteeId<0 || repoId<0) {
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"Missing Inviter or repository\"}");
            return;
		}
		
		if(RepositoryDAO.getInstance().requestCollaborator(inviteeId, repoId, ownerId)){
			
			response.setStatus(200);
            response.getWriter().write("{\"message\": \"Collaborator requested\"}");
		}
		else {
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"Invalid actions\"}");
		}
		
	}

}
