package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.User;
import models.dao.RepositoryDAO;
import models.dao.UserDAO;


public class AddStarToRepository extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public AddStarToRepository() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String repoIdStr = request.getParameter("repoid");
		
		if(username == null  || repoIdStr == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Missing input\"}");
			return;
		}
		
		User user = UserDAO.getInstance().getUserByUserName(username);
		int repoId = Integer.parseInt(repoIdStr);		
		
		RepositoryDAO.getInstance().addStar(user.getId(), repoId);
		
		response.setStatus(200);
		
	}

}
