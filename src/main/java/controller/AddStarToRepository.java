package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.RepositoryDAO;


public class AddStarToRepository extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public AddStarToRepository() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int userId = Integer.parseInt(request.getParameter("userid"));
		int repoId = Integer.parseInt(request.getParameter("repoid"));
		RepositoryDAO.getInstance().addStar(userId, repoId);
		
	}

}
