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


public class HasMergeConflict extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public HasMergeConflict() {
        super();
        // TODO Auto-generated constructor stub
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String PRIdStr = request.getParameter("PRId");
		
		if(PRIdStr == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int PRid = Integer.parseInt(PRIdStr);
		
		if(PRid < 0 || PullRequestDAO.getInstance().isIdExists(PRid)) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request\"}");
			return;
		}
		
		int repoId = PullRequestDAO.getInstance().getRepoId(PRid);
		
		String repoPath = RepositoryDAO.getInstance().getRepoPath(repoId);
		
		ArrayList<String> branches = PullRequestDAO.getInstance().getTargetAndSourceBranch(PRid);
		
		Map<String, int[][]> conflicts =  MergeHandler.getInstance().mergeBranches(repoPath,branches.get(1),branches.get(0));
		
		if(conflicts.isEmpty()) {
			PullRequestDAO.getInstance().changeStatus(PRid, PullRequestStatus.MERGED);
			response.setStatus(200);
			response.getWriter().write("{\"message\" :\"We can auto merge\"}");
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

