package controller.UploadFiles;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import models.User;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;

@MultipartConfig(
	    fileSizeThreshold = 1024 * 1024, // 1 MB (threshold for memory vs. disk storage)
	    maxFileSize = 1024 * 1024 * 100, // 10 MB (max size per file)
	    maxRequestSize = 1024 * 1024 * 300 // 50 MB (max total request size)
	)

public class UploadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String BASE_REPO_PATH = "/opt/repo/";
   
    public UploadFile() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String repoName = request.getParameter("reponame");
		String ownerName = request.getParameter("ownerName");
		String commitMsg = request.getParameter("commitMessage");
		String branchName = request.getParameter("branch");
		
		Collection<Part> fileParts = request.getParts().stream()
                .filter(part -> "files".equals(part.getName()))
                .collect(Collectors.toList());

		String currentUser = request.getParameter("currentUser");
		
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
		
		if(fileParts == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"No file\"}");
			return;
		}

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
		
		String repoPath = BASE_REPO_PATH+ownerName+"/"+repoName+".git";
		

		boolean res = services.UploadFile.getInstance().uploadFilesToGit(fileParts, repoPath, branchName, commitMsg, author);

		if(res) {
		response.setStatus(200);
		response.getWriter().write("{\"message\" :\"File(s) added\"}");
		}else {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Failed to add File(s)\"}");
		}
		
	}

}
