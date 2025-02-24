package controller.Message;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.CommentsDAO;
import models.dao.UserDAO;
import utils.JSONHandler;

public class PostMessage extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public PostMessage() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String PRIdStr = jsonObject.optString("PRId");
		String content = jsonObject.optString("content");
		String currentUserName = jsonObject.optString("currentUsername");
		
		if(content == null || currentUserName == null || PRIdStr == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int currentUserId = UserDAO.getInstance().getUserId(currentUserName);
		
		int pullRequestId = Integer.parseInt(PRIdStr);
		
		if(pullRequestId < 0 ) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request Id\"}");
			return;
		}
		
		CommentsDAO.getInstance().addComment(currentUserId, pullRequestId, content);
		
		response.setStatus(200);
		response.getWriter().write("{\"message\" :\"Comment added\"}");
		
	}

}
