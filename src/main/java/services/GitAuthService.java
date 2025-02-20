package services;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GitAuthService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  		System.out.println("innn");
  		String user = request.getParameter("user");
        String repo = request.getParameter("repo");
        String action = request.getParameter("action");
        System.out.println("Username"+user+" reponame"+repo+" action"+action);
        
        response.setStatus(HttpServletResponse.SC_OK);
  	}

}
