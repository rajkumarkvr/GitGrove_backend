package controller.GetFiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import services.FileStructureHelper;

public class GetContent extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static ArrayList<String> isImage = new ArrayList<String>(List.of("png", "jpg", "jpeg", "gif", "bmp", "svg", "webp"));
    
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
        
    	String ext = "";
		String[] parts = filePath.split("\\.");
		if (parts.length > 1) {
			ext = parts[parts.length - 1].toLowerCase();
		}
		
        
        JSONObject jsonResult = new JSONObject();
        
        if(isImage.contains(ext)) {
        	 ArrayList<String> contentAndDimensions = FileStructureHelper.getInstance().readFileContentOfImage(new File(repoPath), branchName, filePath);
        	 jsonResult.put("content", contentAndDimensions.get(0));
        	 jsonResult.put("width", contentAndDimensions.get(1));
        	 jsonResult.put("height", contentAndDimensions.get(2));
        }
 
        else {
        	 String output = FileStructureHelper.getInstance().readFileContent(new File(repoPath), branchName, filePath);
        	  jsonResult.put("content", output);
		}
        
        response.setStatus(200);
        response.getWriter().write(jsonResult.toString());

	}

//	protected void doGet(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//
//		String username = request.getParameter("username");
//		String repoName = request.getParameter("reponame");
//		String branchName = request.getParameter("branchname");
//		String filePath = request.getParameter("filename");
//
//		if (username == null || repoName == null || branchName == null || filePath == null) {
//			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//			response.getWriter().write("{\"error\": \"Missing Input\"}");
//			return;
//		}
//
//		String repoPath = "/opt/repo/" + username + "/" + repoName + ".git";
//
//		File repository = new File(repoPath);
//
//		if (!repository.exists()) {
//			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//			response.getWriter().write("{\"error\": \"Invalid repository\"}");
//			return;
//		}
//		System.out.println("path " + filePath);
//
//		System.out.println(Arrays.toString(filePath.split(" .")));
//		String ext = "";
//		String[] parts = filePath.split("\\.");
//		if (parts.length > 1) {
//			ext = parts[parts.length - 1].toLowerCase();
//		}
//		
//
//		String output = null;
//
//		if (isImage.contains(ext)) {
//			output = FileStructureHelper.getInstance().readFileContentOfImage(new File(repoPath), branchName, filePath);
//		} else {
//			output = FileStructureHelper.getInstance().readFileContent(new File(repoPath), branchName, filePath);
//
//		}
//
//		response.setStatus(200);
//		JSONObject jsonResult = new JSONObject();
//		jsonResult.put("content", output);
//		response.getWriter().write(output.toString());
//
//	}

}
