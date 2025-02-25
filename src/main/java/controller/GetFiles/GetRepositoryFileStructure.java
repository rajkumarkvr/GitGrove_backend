package controller.GetFiles;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.Git;
import org.json.JSONObject;

import services.FileStructureHelper;

public class GetRepositoryFileStructure extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String BASE_REPO_PATH = "/opt/repo/";
     
    public GetRepositoryFileStructure() {
        super();
        // TODO Auto-generated constructor stub
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		    String username = request.getParameter("username");
	        String repoName = request.getParameter("reponame");
	        String branchName = request.getParameter("branchname");
	    	
	        if (username == null || repoName == null || branchName == null) {
	        
	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            response.getWriter().write("{\"error\": \"Missing username or reponame\"}");
	            return;
	        }
	        
	        System.out.println("username"+username+" repo"+repoName);

	        File repoPath = new File(BASE_REPO_PATH + username + "/" + repoName + ".git");
	        
	        if (!repoPath.exists()) {
	        	System.out.println("Not found repo------------------------");
	            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	            response.getWriter().write("{\"error\": \"Repository not found\"}");
	            return;
	        }
	        
	        JSONObject resultJson = new JSONObject();
	        
	        try (Git git = Git.open(repoPath)) {
	        	
	        	 resultJson.put("files",FileStructureHelper.getInstance().getFileStructure(repoPath,branchName));
	        	 response.setStatus(200);
	        	 response.getWriter().write(resultJson.toString());

	        } catch (Exception e) {
	        	System.out.println(e.getMessage());
	        	e.printStackTrace();
	            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
	        }
	    
	    }
}
