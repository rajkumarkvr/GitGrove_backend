package services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileStructureHelper {

	static FileStructureHelper helper= null;
	
		private FileStructureHelper() {
			
			
			
			
		}
		
		public static FileStructureHelper getInstance() {
			
			if(helper==null) {
				helper = new FileStructureHelper();
				return helper;
			}
			return helper;
		}
    // Method to fetch commit history
    public  JSONArray getCommitHistory(Git git) throws GitAPIException, IOException {
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
    public  JSONArray getMainFiles(File file) {
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


    public  JSONArray getFileStructure(File repoPath) {
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

    private  String readFileContent(File repoPath, String filePath) {
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
    private  String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash == -1 ? "" : path.substring(0, lastSlash);
    }
    
    
    public JSONArray getFileStructure(File repoPath, String branchName) {
        JSONArray rootArray = new JSONArray();

        if (repoPath != null) {
            try (Git git = Git.open(repoPath)) {
                Repository repository = git.getRepository();

                // Resolve the branch reference
                ObjectId branchHead = repository.resolve("refs/heads/" + branchName);
                if (branchHead == null) {
                    return rootArray; // No commits in the branch
                }

                try (RevWalk revWalk = new RevWalk(repository);
                     TreeWalk treeWalk = new TreeWalk(repository)) {

                    RevCommit commit = revWalk.parseCommit(branchHead);
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(false); // Allow manual folder processing

                    Map<String, JSONObject> fileMap = new HashMap<>();

                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();

                        // Ignore Git metadata files
                        if (path.startsWith(".git") || path.startsWith("hooks") || path.startsWith("branches") || path.startsWith("refs")) {
                            continue;
                        }

                        JSONObject fileJson = new JSONObject();
                        fileJson.put("name", treeWalk.getNameString());

                        boolean isFolder = treeWalk.isSubtree();
                        fileJson.put("type", isFolder ? "folder" : "file");

                        if (isFolder) {
                            fileJson.put("children", new JSONArray()); // Prepare for nesting
                            treeWalk.enterSubtree(); // Process folder contents
                        } else {
                            fileJson.put("content", readFileContent(repoPath, path)); // Read file content
                        }

                        // Handle the hierarchy
                        String parentPath = getParentPath(path);
                        if (parentPath.isEmpty()) {
                            rootArray.put(fileJson);
                        } else {
                            JSONObject parentJson = fileMap.computeIfAbsent(parentPath, k -> new JSONObject());
                            JSONArray childrenArray = parentJson.optJSONArray("children");

                            if (childrenArray == null) {
                                childrenArray = new JSONArray();
                                parentJson.put("children", childrenArray);
                            }

                            childrenArray.put(fileJson);
                        }

                        fileMap.put(path, fileJson);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rootArray;
    }



}
