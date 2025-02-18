package controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.Git;
import org.json.JSONObject;

import enums.Visibility;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.JSONHandler;
import utils.PermissionManager;

public class CreateRepoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
//    private static final String REPO_PATH = "/home/raj-zstk371/Documents/Rajkumar/FeedbackSystem/src/main/webapp/all-repos";
	private static final String REPO_PATH = "/opt/repo";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//
//    	resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
//    	resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//    	resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		
		JSONObject jsonData = JSONHandler.parse(req.getReader());

		String username = jsonData.getString("username").toLowerCase();
		String repoName = jsonData.getString("repoName").toLowerCase();
		String repoDescription = jsonData.getString("description");
		Visibility visibility = Visibility.valueOf(jsonData.getString("visibility").toUpperCase());
		
		int userId = UserDAO.getInstance().getUserId(username);

		if (username == null || username.trim().isEmpty() || repoName == null || repoName.trim().isEmpty()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and repository name are required.");
			return;
		}
		// Create user folder
		File userFolder = new File(REPO_PATH, username);
		if (!userFolder.exists()) {
			userFolder.mkdirs(); // Create user directory if not exists
		}

		// Define repo path
		File repoDir = new File(userFolder, repoName + ".git");

		if (repoDir.exists()) {
			resp.sendError(HttpServletResponse.SC_CONFLICT, "Repository already exists.");
			return;
		}
		

		try {
			// Initialize a bare repository
			Git.init().setBare(true).setDirectory(repoDir).call();
			PermissionManager.setRecursivePermissions(repoDir.toPath(), "rwxrwxrwx");
			PermissionManager.setOwner(userFolder, "git:git");
			PermissionManager.setOwner(repoDir, "git:git");
			System.out.println("Repository created:http://localhost:8080/FeedbackSystem/CreateRepoServlet" + username
					+ "/" + repoName + ".git");
			RepositoryDAO.getInstance().addRepository(repoName, repoDescription, visibility.toString(), userId);
			resp.getWriter().write("Repository created:" + username + "/" + repoName + ".git");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error creating repository: " + e.getMessage());
		}
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
