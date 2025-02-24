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
		
		String PRIdStr = jsonObject.optString("PRId").trim();
		String content = jsonObject.optString("content").trim();
		String currentUserName = jsonObject.optString("currentUsername").trim();
		
		if(content.isEmpty() || currentUserName.isEmpty() || PRIdStr.isEmpty()) {
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
