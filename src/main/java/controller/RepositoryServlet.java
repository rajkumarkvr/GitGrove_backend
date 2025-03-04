package controller;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.json.JSONArray;
import org.json.JSONObject;

import services.FileStructureHelper;

public class RepositoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private static final String BASE_REPO_PATH = "/opt/repo/";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String repoName = request.getParameter("reponame");
        String branchName = request.getParameter("branchname");

        System.out.println("Branch name printing -------------------------------------"+branchName);
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
        
        try (Git git = Git.open(repoPath)) {
            JSONObject repoJson = new JSONObject();
            repoJson.put("name", repoName);
            repoJson.put("sshUrl", "git@172.17.23.190:/opt/repo/" + username + "/" + repoName + ".git");

            // Get Branches
            List<String> branches = new ArrayList<>();
            for (Ref ref : git.branchList().call()) {
                branches.add(ref.getName().replace("refs/heads/", ""));
            }
            repoJson.put("branches", new JSONArray(branches));
            
            if(branches.contains("main") && !branches.contains("master")) {
            	 repoJson.put("defaultBranch", "main");
            	 
            	 if(branchName.equals( "master")) {
            		 branchName = "main";
            	 }
            	 
            }
            
            else {
            	repoJson.put("defaultBranch", "master");
            }

            // Check if the repository has commits
            JSONArray commitsArray;
            try {
            	System.out.println("Commit getting branch =--------------------"+branchName);
                commitsArray = FileStructureHelper.getInstance().getCommitHistory(git,branchName) ;
                } 
            
            
            catch (GitAPIException|NullPointerException e) {
            	e.printStackTrace();
            	System.out.println("Gett messsyugdysuigdj --------------------------"+e.getMessage());
                // If an error occurs, assume no commits exist
                commitsArray = new JSONArray();
            }

            if (commitsArray.isEmpty()) {
            
                // Special response for repositories without commits
                repoJson.put("status", "fresh");
                repoJson.put("message", "This repository has no commits yet.");
                repoJson.put("branches", branches);
                repoJson.put("files", new JSONArray()); // Empty file list
                repoJson.put("mainFiles", new JSONArray()); // Empty main files
            } else {
                repoJson.put("commits", commitsArray);
                repoJson.put("mainFiles",FileStructureHelper.getInstance().getMainFiles(repoPath,branchName));
            }


            response.setContentType("application/json");
            response.getWriter().write(repoJson.toString(4));

        } catch (Exception e) {
        	System.out.println(e.getMessage());
        	e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    
    }


}
