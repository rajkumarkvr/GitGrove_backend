package controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.RepositoryDAO;
import services.FileStructureHelper;

public class DeleteRepository extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public DeleteRepository() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {


		String repoIdStr = request.getParameter("repoId");

		if (repoIdStr == null || repoIdStr.isEmpty()) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Missing input\"}");
			return;
		}

		int repoId = Integer.parseInt(repoIdStr);
		
		if(repoId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid repository\"}");
			return;
		}
		
		String repoPath = RepositoryDAO.getInstance().getRepoPath(repoId);
		
		File repository = new File(repoPath);

		if (!repository.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found.");
			return;
		}
		
		try {
			
			FileStructureHelper.getInstance().deleteDirectory(repository);
			RepositoryDAO.getInstance().deleteRepository(repoId);
			response.setStatus(200);
			response.getWriter().write("Repository deleted successfully");
			
		} catch (Exception e) {
			
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error updating username: " + e.getMessage());
		}
	}

}
