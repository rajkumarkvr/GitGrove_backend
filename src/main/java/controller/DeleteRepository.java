package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import services.FileStructureHelper;
import utils.JSONHandler;
import utils.PermissionManager;

public class DeleteRepository extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String REPO_PATH = "/opt/repo/";

	public DeleteRepository() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject jsonData = JSONHandler.parse(request.getReader());

		String ownerName = jsonData.getString("ownername").toLowerCase().trim();
		String repoName = jsonData.getString("reponame").toLowerCase().trim();

		if (ownerName == null || repoName == null || ownerName.isEmpty() || repoName.isEmpty()) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Missing input\"}");
			return;
		}


		File repository = new File(REPO_PATH + ownerName, repoName);

		if (!repository.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found.");
			return;
		}
		
		try {
			FileStructureHelper.getInstance().deleteDirectory(repository);
			response.setStatus(200);
			response.getWriter().write("Repository deleted successfully");
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error updating username: " + e.getMessage());
		}
	}

}
