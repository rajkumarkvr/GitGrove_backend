package controller.UploadFiles;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import models.User;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;


public class UploadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public UploadFile() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String repoName = request.getParameter("reponame");
		String ownerName = request.getParameter("ownerName");
		String commitMsg = request.getParameter("commitMessage");
		String branchName = request.getParameter("branch");
		Part filePart = request.getPart("files");
		String currentUser = request.getParameter("currentuser");
		

		if(commitMsg == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"There is no commit message\"}");
			return;
		}
		
		if(repoName == null || ownerName == null || branchName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid repository\"}");
			return;
		}
		
		if(currentUser == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid user\"}");
			return;
		}
		
		if(filePart == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"No file\"}");
			return;
		}
		System.out.println(filePart.toString());
		int userId =  UserDAO.getInstance().getUserId(currentUser);
		
		if(userId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid user\"}");
			return;
		}
		
		User author = UserDAO.getInstance().getUserById(userId);
		
		int repoOwnerId = UserDAO.getInstance().getUserId(ownerName);
		
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, repoOwnerId);
		
		if(repoId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid repository\"}");
			return;
		}
	
		boolean canUpload = RepositoryDAO.getInstance().canCollaborate(repoId, userId);
		
		if(!canUpload) {
			response.setStatus(403);
			response.getWriter().write("{\"message\" :\"You not have access to upload file\"}");
			return;
		}
		
		String repoPath = "/opt/repo/"+ownerName+"/"+repoName+".git";
		
//		services.UploadFile.getInstance().addFile(repoPath, filePart, commitMsg, branchName, author);
		
		response.setStatus(200);
		response.getWriter().write("{\"message\" :\"File added\"}");
		
	}

}
