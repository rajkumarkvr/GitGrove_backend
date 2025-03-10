package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.PermissionManager;

public class RenameRepository extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String REPO_PATH = "/opt/repo/";

	public RenameRepository() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {;

		String ownerName = request.getParameter("ownername");
		String oldRepoName =  request.getParameter("oldreponame");
		String newRepoName =  request.getParameter("newreponame");

		if(ownerName == null || oldRepoName == null || newRepoName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Missing input\"}");
			return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
	
		int repoId = RepositoryDAO.getInstance().getRepositoryId(oldRepoName, ownerId);
		
		if (oldRepoName.equals(newRepoName)) {
			response.setStatus(200);
			return;
		}
		
		File oldRepo = new File(REPO_PATH+ownerName, oldRepoName+".git");
		File newRepo = new File(REPO_PATH+ownerName, newRepoName+".git");

		
		if (!oldRepo.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found.");
			return;
		}

		if (newRepo.exists()) {
			response.sendError(HttpServletResponse.SC_CONFLICT, "New repository name already exists.");
			return;
		}

		try {
			
			Files.move(oldRepo.toPath(), newRepo.toPath(), StandardCopyOption.REPLACE_EXISTING);
			PermissionManager.setOwner(newRepo, "git:git");
			RepositoryDAO.getInstance().renameRepository(repoId, newRepoName);
			response.setStatus(200);
			response.getWriter().write("Repository name successfully changed");
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating username: " + e.getMessage());
		}
	}

}
