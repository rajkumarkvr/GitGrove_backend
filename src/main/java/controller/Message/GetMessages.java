package controller.Message;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import models.Comment;
import models.dao.BranchDAO;
import models.dao.CommentsDAO;
import models.dao.PullRequestDAO;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;
import utils.JSONHandler;


public class GetMessages extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public GetMessages() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String ownerName = jsonObject.optString("ownerName");
		String repoName = jsonObject.optString("reponame");
		String requestCreaterName = jsonObject.optString("requesterName");
		String sourceBranch = jsonObject.optString("sourceBranch");
		String targetBranch = jsonObject.optString("targetBranch");
		
		if(ownerName == null || repoName == null || requestCreaterName == null || sourceBranch == null || targetBranch ==null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int ownerId = UserDAO.getInstance().getUserId(ownerName);
		int repoId = RepositoryDAO.getInstance().getRepositoryId(repoName, ownerId);
		int requestCreaterId = UserDAO.getInstance().getUserId(requestCreaterName);
		int sourceBranchId = BranchDAO.getInstance().getBranchId(repoId, sourceBranch);
		int targetBranchId = BranchDAO.getInstance().getBranchId(repoId, targetBranch);
		
		int pullRequestId = PullRequestDAO.getInstance().getPullRequestId(sourceBranchId, targetBranchId, requestCreaterId);
		
		
		if(pullRequestId < 0 ) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid credentials\"}");
			return;
		}
		
		ArrayList<Comment> comments = CommentsDAO.getInstance().getComments(pullRequestId);
		
		JSONArray commentsJSONList = new JSONArray();
		
		for(Comment comment : comments) {
			JSONObject commentJson = new JSONObject();
			commentJson.put("username", comment.getUsername());
			commentJson.put("userAvatar", comment.getPostedByAvatar());
			commentJson.put("postedAt", comment.getPostedAt());
			commentJson.put("content", comment.getContent());
			commentsJSONList.put(commentJson);
		}
		
		JSONObject resultJson = new JSONObject();
		
		resultJson.put("data", commentsJSONList);
		
		response.setStatus(200);
		response.getWriter().write(resultJson.toString());
		
	}

}
