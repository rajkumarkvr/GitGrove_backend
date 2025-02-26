package controller.GetFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import services.FileStructureHelper;


public class GetContent extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    public GetContent() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String username = request.getParameter("username");
        String repoName = request.getParameter("reponame");
        String branchName = request.getParameter("branchname");
        String filePath = request.getParameter("filename");
        
        if(username == null || repoName == null || branchName == null || filePath == null) {
        	  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	          response.getWriter().write("{\"error\": \"Missing Input\"}");
	          return;
        }
        
        String repoPath = "/opt/repo/"+username+"/"+repoName+".git";
        
        File repository = new File(repoPath);
        
        if(!repository.exists()) {
        	 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	         response.getWriter().write("{\"error\": \"Invalid repository\"}");
	         return;
        }
        System.out.println("path"+filePath);
        
        ArrayList<String> output = FileStructureHelper.getInstance().readFileContent(repository, branchName, filePath);
        
        response.setStatus(200);
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("content", output.get(0));
        
        if(output.size()>2) {
        	jsonResult.put("width", output.get(1));
        	jsonResult.put("height", output.get(2));
        }
        
        response.getWriter().write(jsonResult.toString());
	}

	

}
