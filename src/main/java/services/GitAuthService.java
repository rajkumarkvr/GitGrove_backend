package services;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.RepositoryDAO;
import models.dao.UserDAO;

public class GitAuthService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  		System.out.println("innn");
  		String user = request.getParameter("user");
        String repo = request.getParameter("repo");
        String action = request.getParameter("action");
        
        System.out.println("Username"+user+" reponame"+repo+" action"+action);
        
        String repoName = repo.replaceAll("/opt/repo/", "").split("/")[1].replaceAll(".git", "");
        String ownerName = repo.replaceAll("/opt/repo/", "").split("/")[0];
        	
        int ownerId = UserDAO.getInstance().getUserId(ownerName);
        
        boolean isValid = false;
        int userId = UserDAO.getInstance().getUserId(user);
        int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName,ownerId);
        
        System.out.println("repoid : "+repoId+" User id : "+userId);
        if(action.equals("fetch")) {
        	if(!RepositoryDAO.getInstance().isPrivate(repoId) || RepositoryDAO.getInstance().canCollaborate(repoId, userId)) {
        		isValid = true;
        	}
        }
        
        if(action.equals("push")) {
        	if(RepositoryDAO.getInstance().canCollaborate(repoId, userId)){
        		isValid = true;
        	}
        }	
        
  
        if(isValid) {
        	response.setStatus(HttpServletResponse.SC_OK);
        }
        
        else {
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        
        
  	}

}
