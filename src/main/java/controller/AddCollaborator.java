package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.JSONHandler;


public class AddCollaborator extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public AddCollaborator() {
        super();
        // TODO Auto-generated constructor stub
    }


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject userobj =JSONHandler.parse(request.getReader());
		String ownerName = userobj.optString("inviterUsername");
		String inviteeName = userobj.optString("inviteeUsername");
		String repoName = userobj.optString("repository");
		
		System.out.println("owner name "+ownerName);
		if(ownerName == null || inviteeName == null || repoName == null) {
			System.out.println("owner name "+ownerName);
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
		
		if(RepositoryDAO.getInstance().addCollaborator(inviteeId, repoId, ownerId)){
			response.setStatus(200);
            response.getWriter().write("{\"message\": \"Collaborator added\"}");
		}
		
		else {
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"Invalid actions\"}");
		}
	}

}
