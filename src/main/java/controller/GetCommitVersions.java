package controller;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.json.JSONObject;

import services.FileStructureHelper;
import utils.JSONHandler;


public class GetCommitVersions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public GetCommitVersions() {
        super();
        // TODO Auto-generated constructor stub
    }

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		String commitHash = jsonObject.optString("commitHash");
		String repoName = jsonObject.optString("reponame");
		String username = jsonObject.optString("username");
		
		
		String repoPath = "/opt/repo/"+username+"/"+repoName+".git";
		
		File file = new File(repoPath);
		
		if(!file.exists()) {
			response.setStatus(400);
			return;
		}
		
		Git git = Git.open(file);
		
		Repository repository = git.getRepository();
		
		JSONObject responseObject = new JSONObject();
		responseObject.put("message", FileStructureHelper.getInstance().getCommitChanges(repository, commitHash));
		
		response.setStatus(200);
		response.getWriter().write(responseObject.toString());
	}


}
