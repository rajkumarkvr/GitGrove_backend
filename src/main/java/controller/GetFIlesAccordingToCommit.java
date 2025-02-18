package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class GetFIlesAccordingToCommit extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
    public GetFIlesAccordingToCommit() {
        super();
        // TODO Auto-generated constructor stub
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String repoPath = request.getParameter("repopath");
		String commit = request.getParameter("commit");
		
		if(repoPath == null || commit == null) {
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"Missing input\"}");
            return;
		}
		
		
	}

}
