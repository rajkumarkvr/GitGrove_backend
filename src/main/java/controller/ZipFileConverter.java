package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import services.FileStructureHelper;


public class ZipFileConverter extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    public ZipFileConverter() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String repoName = request.getParameter("reponame");
		String userName = request.getParameter("username");
		
		if(repoName == null || userName == null) {
			response.setStatus(400);
			response.getWriter().write("{\"error\" : \"Repository not found\"}");
		}
		
		String repoPath = "/opt/repo/"+userName+"/"+repoName+".git";
		File file = new File(repoPath);
		
		File zipFile = FileStructureHelper.getInstance().zipRepository(file, repoName);

	    response.setContentType("application/zip");
	    response.setHeader("Content-Disposition", "attachment; filename=\"repository.zip\"");
	    
	    try (FileInputStream fis = new FileInputStream(zipFile);
	         OutputStream os = response.getOutputStream()) {
	        byte[] buffer = new byte[1024];
	        int bytesRead;
	        while ((bytesRead = fis.read(buffer)) != -1) {
	            os.write(buffer, 0, bytesRead);
	        }
	    }
		
	}

}
