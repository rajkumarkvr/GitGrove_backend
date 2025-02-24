package controller.pullrequests;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.BranchDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.JSONHandler;


public class GetBranchesForCreatingPullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public GetBranchesForCreatingPullRequest() {
        super();
        // TODO Auto-generated constructor stub
    }
  
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String ownerName = jsonObject.optString("ownername");
		String repoName = jsonObject.optString("reponame");
		
		if(ownerName == null || repoName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, ownerId);
		
		if(repoId < 0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid repository\"}");
			return;
		}
		
		BranchDAO.getInstance().addBranch(ownerName, repoName);
		
		ArrayList<String> branches = BranchDAO.getInstance().branchList(repoId);
		
		response.setStatus(200);
		
		JSONObject resultJson = new JSONObject();
		resultJson.put("data", branches);
		
		response.getWriter().write(resultJson.toString());
			
	}

}
