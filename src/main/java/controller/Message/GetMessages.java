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
import models.dao.CommentsDAO;

//@WebServlet("/service/pull-request/comments")

public class GetMessages extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public GetMessages() {
        super();
        // TODO Auto-generated constructor stub
    }
    
//    @ApiParam(name="PR-Id",type="number")
//    @ResponseParam(responseCode ="200",description="get a message")
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String PRIdStr = request.getParameter("PR-Id");		
		
		if(PRIdStr ==null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int pullRequestId = Integer.parseInt(PRIdStr);
		
		if(pullRequestId < 0 ) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request Id\"}");
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
		
		resultJson.put("comments", commentsJSONList);
		
		response.setStatus(200);
		response.getWriter().write(resultJson.toString());
		
	}

}
