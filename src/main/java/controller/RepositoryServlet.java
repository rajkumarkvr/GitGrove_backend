package controller;

import java.io.ByteArrayOutputStream;
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
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
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
    private JSONArray getMainFiles(File file) {
        JSONArray mainFilesArray = new JSONArray();

        if (file != null) {
            try (Git git = Git.open(file)) {
                Repository repository = git.getRepository();
                ObjectId head = repository.resolve("HEAD^{tree}"); // Get the latest commit tree

                if (head == null) {
                    // No commits in the repository
                    return mainFilesArray;
                }

                try (RevWalk revWalk = new RevWalk(repository);
                     TreeWalk treeWalk = new TreeWalk(repository)) {
                    
                    RevCommit commit = revWalk.parseCommit(repository.resolve("HEAD")); // Get latest commit
                    treeWalk.addTree(commit.getTree()); 
                    treeWalk.setRecursive(false); // Only top-level files/folders
                    
                    while (treeWalk.next()) {
                        JSONObject fileJson = new JSONObject();
                        String fileName = treeWalk.getNameString();
                        boolean isFolder = treeWalk.isSubtree();
                        
                        fileJson.put("name", fileName);
                        fileJson.put("type", isFolder ? "folder" : "file");

                        // Get last commit message and time for the file
                        String commitMessage = commit.getShortMessage();
                        String commitTime = commit.getAuthorIdent().getWhen().toInstant().toString();

                        fileJson.put("commitMessage", commitMessage);
                        fileJson.put("commitTime", commitTime);

                        mainFilesArray.put(fileJson);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mainFilesArray;
    }


    private JSONArray getFileStructure(File repoPath) {
        JSONArray rootArray = new JSONArray();
        
        if (repoPath != null) {
            try (Git git = Git.open(repoPath)) {
                Repository repository = git.getRepository();
                ObjectId head = repository.resolve("HEAD^{tree}"); // Get the latest commit tree
                
                if (head == null) {
                    return rootArray; // Return empty array if no commits
                }

                try (RevWalk revWalk = new RevWalk(repository);
                     TreeWalk treeWalk = new TreeWalk(repository)) {

                    RevCommit commit = revWalk.parseCommit(repository.resolve("HEAD")); // Get latest commit
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(false); // Process parent-child hierarchy manually

                    // Use a map to store folder structures
                    Map<String, JSONObject> fileMap = new HashMap<>();

                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();

                        // Ignore Git internals
                        if (path.startsWith(".git") || path.startsWith("hooks") || path.startsWith("branches") || path.startsWith("refs")) {
                            continue;
                        }

                        JSONObject fileJson = new JSONObject();
                        fileJson.put("name", treeWalk.getNameString());

                        boolean isFolder = treeWalk.isSubtree(); // True if it's a folder

                        if (isFolder) {
                            fileJson.put("type", "folder");
                            fileJson.put("children", new JSONArray()); // Initialize empty children
                            treeWalk.enterSubtree(); // Dive into folder
                        } else {
                            fileJson.put("type", "file");
                            fileJson.put("content", readFileContent(repoPath, path)); // Read file content
                        }

                        // Handle hierarchy
                        String parentPath = getParentPath(path);
                        if (parentPath.isEmpty()) {
                            rootArray.put(fileJson);
                        } else {
                            // Ensure parent exists
                            JSONObject parentJson = fileMap.computeIfAbsent(parentPath, k -> new JSONObject());
                            JSONArray childrenArray = parentJson.optJSONArray("children");

                            // Initialize children array if not present
                            if (childrenArray == null) {
                                childrenArray = new JSONArray();
                                parentJson.put("children", childrenArray);
                            }

                            childrenArray.put(fileJson);
                        }

                        // Store in map
                        fileMap.put(path, fileJson);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rootArray;
    }

//    // Utility method to read file content
//    private String readFileContent(File file) {
//        try {
//            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            return "Error reading file";
//        }
//    }

    private String readFileContent(File repoPath, String filePath) {
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoPath) // This is a bare repo
                .build()) {

            ObjectId lastCommitId = repository.resolve(Constants.HEAD);
            if (lastCommitId == null) {
                return "No commits found in the repository.";
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(lastCommitId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(filePath));

                    if (!treeWalk.next()) {
                        return "File not found in repository: " + filePath;
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    loader.copyTo(outputStream);
                    return outputStream.toString(); // Convert content to String and return
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading file: " + e.getMessage();
        }
    }

    // Utility method to get the parent path
    private String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash == -1 ? "" : path.substring(0, lastSlash);
    }

}
