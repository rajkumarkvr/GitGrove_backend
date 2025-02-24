package controller.pullrequests;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MergeConflictUsingOurs extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
    public MergeConflictUsingOurs() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String PRId = request.getParameter("PRId");
		
		if(PRId == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid input\"}");
			return;
		}
		
	}

}
