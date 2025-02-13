package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import services.RepositoryManager;


public class RepositoryStructure extends HttpServlet {
	private static final long serialVersionUID = 1L;
      
    public RepositoryStructure() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String reponame = request.getParameter("reponame");
		
		RepositoryManager.getAllFiles(reponame, username);
		
	}

	

}
