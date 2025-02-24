package controller.pullrequests;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import enums.PullRequestStatus;
import models.dao.PullRequestDAO;


public class ClosePullRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ClosePullRequest() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String idStr = request.getParameter("id");
		
		
		if(idStr == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid input\"}");
			return;
		}
		
		int id = Integer.parseInt(idStr);
		
		if(id < 0 || PullRequestDAO.getInstance().isIdExists(id)) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" :\"Invalid pull request\"}");
			return;
		}
		
		PullRequestDAO.getInstance().changeStatus(id, PullRequestStatus.CLOSED);
		
	}

}
