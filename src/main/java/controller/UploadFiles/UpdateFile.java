package controller.UploadFiles;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UpdateFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String BASE_REPO_PATH = "/opt/repo/";
       
    public UpdateFile() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String repoName = request.getParameter("reponame");
		String ownerName = request.getParameter("ownerName");
		String commitMsg = request.getParameter("commitMessage");
		String branchName = request.getParameter("branch");
		String currentUserName = request.getParameter("currentUser");
		
		if(repoName == null || ownerName == null || commitMsg == null || branchName == null || currentUserName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Missing Input\"}");
		}
		
		
			
	}

}
