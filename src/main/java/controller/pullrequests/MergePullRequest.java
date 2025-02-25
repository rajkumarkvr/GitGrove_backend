package controller.pullrequests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.fasterxml.jackson.databind.ObjectMapper;

import enums.PullRequestStatus;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import services.MergeHandler;

public class MergePullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public MergePullRequest() {
        super();
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String idStr = request.getParameter("id");
		
		if(idStr == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int PRid = Integer.parseInt(idStr);
		
		if(PRid < 0 || !PullRequestDAO.getInstance().isIdExists(PRid)) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request\"}");
			return;
		}
		
		int repoId = PullRequestDAO.getInstance().getRepoId(PRid);
	
		String repoPath = RepositoryDAO.getInstance().getRepoPath(repoId);
		
		ArrayList<String> branches = PullRequestDAO.getInstance().getTargetAndSourceBranch(PRid);
		
		Map<String,String> conflicts =  MergeHandler.getInstance().mergeBranches(repoPath,branches.get(1),branches.get(0));
		
		if(conflicts.isEmpty()) {
			PullRequestDAO.getInstance().changeStatus(PRid, PullRequestStatus.MERGED);
			response.setStatus(200);
			response.getWriter().write("{\"message\" :\"Merge successful\"}");
			return;
		}
		
		else {
	        ObjectMapper objectMapper = new ObjectMapper();
	        String jsonResponse = objectMapper.writeValueAsString(conflicts);

	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.setStatus(200);	
	        response.getWriter().write(jsonResponse);																		
		}																														
		
	}

}
