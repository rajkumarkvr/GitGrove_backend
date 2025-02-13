package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;

import org.json.JSONArray;
import org.json.JSONObject;


public class RepositoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private static final String BASE_REPO_PATH = "/opt/repo/";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        

        String username = request.getParameter("username");
        String repoName = request.getParameter("reponame");

        if (username == null || repoName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing username or reponame\"}");
            return;
        }

        File repoPath = new File(BASE_REPO_PATH + username + "/" + repoName + ".git");
        if (!repoPath.exists()) {
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
            repoJson.put("defaultBranch", branches.isEmpty() ? "master" : branches.get(0));

            // Check if the repository has commits
            JSONArray commitsArray;
            try {
                commitsArray = getCommitHistory(git);
            } catch (GitAPIException e) {
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
                repoJson.put("mainFiles", getMainFiles(repoPath));
                repoJson.put("files", getFileStructure(repoPath));
            }

            response.setContentType("application/json");
            response.getWriter().write(repoJson.toString(4));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    
    }

    // Method to fetch commit history
    private JSONArray getCommitHistory(Git git) throws GitAPIException, IOException {
        JSONArray commitsArray = new JSONArray();
        Iterable<RevCommit> commits = git.log().setMaxCount(3).call();

        for (RevCommit commit : commits) {
            JSONObject commitJson = new JSONObject();
            commitJson.put("id", commit.getName());
            commitJson.put("message", commit.getShortMessage());
            commitJson.put("date", commit.getAuthorIdent().getWhen().toInstant().toString());
            commitsArray.put(commitJson);
        }
        return commitsArray;
    }

    // Get top-level files and folders
    private JSONArray getMainFiles(File repoPath) {
        JSONArray mainFilesArray = new JSONArray();
        File[] files = repoPath.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals(".git")) { // Ignore .git directory
                    JSONObject fileJson = new JSONObject();
                    fileJson.put("name", file.getName());
                    fileJson.put("type", file.isDirectory() ? "folder" : "file");
                    fileJson.put("commitMessage", "Recent change"); // Placeholder, fetch commit if needed
                    fileJson.put("commitTime", "2025-02-13 10:30 AM"); // Placeholder, format needed
                    mainFilesArray.put(fileJson);
                }
            }
        }
        return mainFilesArray;
    }

    // Recursive function to get full file structure
    private JSONArray getFileStructure(File dir) throws IOException {
        JSONArray fileArray = new JSONArray();
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals(".git")) { // Ignore .git directory
                    JSONObject fileJson = new JSONObject();
                    fileJson.put("name", file.getName());
                    fileJson.put("type", file.isDirectory() ? "folder" : "file");

                    if (file.isDirectory()) {
                        fileJson.put("children", getFileStructure(file));
                    } else {
                        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                        fileJson.put("content", content);
                    }

                    fileArray.put(fileJson);
                }
            }
        }
        return fileArray;
    }
}
