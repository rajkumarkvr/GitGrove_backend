package services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileStructureHelper {

	static FileStructureHelper helper = null;

	private static ArrayList<String> isImage = new ArrayList<String>(
			List.of("png", "jpg", "jpeg", "gif", "bmp", "svg", "webp"));
	private static ArrayList<String> isVideo = new ArrayList<String>(
			List.of("mp4", "webm", "ogg", "mov", "avi", "flv", "mkv"));
	private static ArrayList<String> isAudio = new ArrayList<String>(
			List.of("mp3", "wav", "ogg", "flac", "aac", "m4a"));

	private FileStructureHelper() {

	}

	public static FileStructureHelper getInstance() {
		if (helper == null) {
			helper = new FileStructureHelper();
		}
		return helper;
	}

	// Fetches commit history of a specific branch
	public JSONArray getCommitHistory(Git git, String branchName) throws GitAPIException, IOException {
		JSONArray commitsArray = new JSONArray();

		Iterable<RevCommit> commits = git.log().add(git.getRepository().resolve("refs/heads/" + branchName)).call();

		for (RevCommit commit : commits) {
			JSONObject commitJson = new JSONObject();
			commitJson.put("id", commit.getName());
			commitJson.put("message", commit.getShortMessage());
			commitJson.put("date", commit.getAuthorIdent().getWhen().toInstant().toString());
			commitsArray.put(commitJson);
		}
		return commitsArray;
	}

	// Fetches main files from a specific branch
	public JSONArray getMainFiles(File repoPath, String branchName) {
		JSONArray mainFilesArray = new JSONArray();

		if (repoPath != null) {
			try (Git git = Git.open(repoPath)) {
				Repository repository = git.getRepository();

				// Resolving the HEAD commit of the given branch
				ObjectId branchHead = repository.resolve("refs/heads/" + branchName);
				if (branchHead == null) {
					return mainFilesArray; // Empty if no branch found
				}

				try (RevWalk revWalk = new RevWalk(repository); TreeWalk treeWalk = new TreeWalk(repository)) {

					RevCommit commit = revWalk.parseCommit(branchHead);
					RevTree tree = commit.getTree(); // Commit tree for specific branch
					treeWalk.addTree(tree);
					treeWalk.setRecursive(false); // Don't go into subdirectories

					while (treeWalk.next()) {
						JSONObject fileJson = new JSONObject();
						String fileName = treeWalk.getNameString();
						boolean isFolder = treeWalk.isSubtree();

						fileJson.put("name", fileName);
						fileJson.put("type", isFolder ? "folder" : "file");

						// Get the last commit for the file
						RevCommit fileCommit = getLastCommitForFile(repository, branchHead, treeWalk.getPathString());

						if (fileCommit != null) {
							fileJson.put("commitMessage", fileCommit.getShortMessage());
							Date commitDate = fileCommit.getAuthorIdent().getWhen();
							TimeZone authorTimeZone = fileCommit.getAuthorIdent().getTimeZone();
							ZonedDateTime localTime = commitDate.toInstant().atZone(authorTimeZone.toZoneId());
							fileJson.put("commitTime", localTime);
						}

						mainFilesArray.put(fileJson);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mainFilesArray;
	}

	// Get the last commit associated with a specific file in a given branch
	private RevCommit getLastCommitForFile(Repository repository, ObjectId branchHead, String filePath) {
		try (Git git = new Git(repository); RevWalk revWalk = new RevWalk(repository)) {

			revWalk.markStart(revWalk.parseCommit(branchHead));

			for (RevCommit commit : revWalk) {
				try (TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.addTree(commit.getTree());
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));

					if (treeWalk.next()) {
						return commit;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Reads file content from a specific branch
	public String readFileContent(File repoPath, String branchName, String filePath) {

		try (Repository repository = new FileRepositoryBuilder().setGitDir(repoPath).build();) {

			ObjectId branchHead = repository.resolve("refs/heads/" + branchName);
			System.out.println("branch name" + branchName);
			
			if (branchHead == null) {
				return "No commits found in the branch: " + branchName;
			}

			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit commit = revWalk.parseCommit(branchHead);
				RevTree tree = commit.getTree();

				try (TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.addTree(tree);
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));

					if (!treeWalk.next()) {
						
						return "File not found in repository: " + filePath + " (Branch: " + branchName + ")";
	
					}

					ObjectId objectId = treeWalk.getObjectId(0);

					try {
						ObjectLoader loader = repository.open(objectId);
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						loader.copyTo(outputStream);
						return outputStream.toString();
					} catch (Exception e) {
						System.out.println("Read file content error : " + e.getMessage());
						
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public ArrayList<String> readFileContentOfImage(File repoPath, String branchName, String filePath) {

		ArrayList<String> contentAndDimensions = new ArrayList<String>();
		
		try (Repository repository = new FileRepositoryBuilder().setGitDir(repoPath).build();) {

			ObjectId branchHead = repository.resolve("refs/heads/" + branchName);
			System.out.println("branch name" + branchName);
			
			if (branchHead == null) {
				contentAndDimensions.add("No commits found in the branch: " + branchName);
				return contentAndDimensions;
			}

			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit commit = revWalk.parseCommit(branchHead);
				RevTree tree = commit.getTree();

				try (TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.addTree(tree);
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));

					if (!treeWalk.next()) {
						contentAndDimensions.add("File not found in repository: " + filePath + " (Branch: " + branchName + ")");
						return contentAndDimensions;
					}

					ObjectId objectId = treeWalk.getObjectId(0);

					try {
						
						ObjectLoader loader = repository.open(objectId);
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						loader.copyTo(outputStream);
						String base64Str = Base64.getEncoder().encodeToString(outputStream.toByteArray());
						contentAndDimensions.add(base64Str);
						ArrayList<String> widthandHeight = getDimensionWidthandHeight(base64Str);
						contentAndDimensions.add(widthandHeight.get(0));
						contentAndDimensions.add(widthandHeight.get(1));
						
					} catch (Exception e) {
						contentAndDimensions.add("Unable to get file. Please, try again later");
						System.out.println("Read file content error : " + e.getMessage());
						return contentAndDimensions;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return contentAndDimensions;
	}
	
	public ArrayList<String> readFileContentWithDimension(File repoPath, String branchName, String filePath) {

		ArrayList<String> contentAndDimension = new ArrayList<String>();
		
		try (Repository repository = new FileRepositoryBuilder().setGitDir(repoPath).build();) {

			ObjectId branchHead = repository.resolve("refs/heads/" + branchName);
			System.out.println("branch name" + branchName);
			
			if (branchHead == null) {
				contentAndDimension.add("No commits found in the branch: " + branchName);
				return contentAndDimension;
			}

			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit commit = revWalk.parseCommit(branchHead);
				RevTree tree = commit.getTree();

				try (TreeWalk treeWalk = new TreeWalk(repository)) {
					treeWalk.addTree(tree);
					treeWalk.setRecursive(true);
					treeWalk.setFilter(PathFilter.create(filePath));

					if (!treeWalk.next()) {
						
						contentAndDimension.add("File not found in repository: " + filePath + " (Branch: " + branchName + ")");
						return contentAndDimension;
	
					}

					ObjectId objectId = treeWalk.getObjectId(0);

					try {
						ObjectLoader loader = repository.open(objectId);
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						loader.copyTo(outputStream);
						String ext = Arrays.asList(filePath.split(".")).getLast().toLowerCase();
						
						if (isImage.contains(ext)) {
							String base64Content = Base64.getEncoder().encodeToString(outputStream.toByteArray()); 
							contentAndDimension.add(base64Content);
							ArrayList<String> widthAndHeight = getDimensionWidthandHeight(base64Content);
							contentAndDimension.add(widthAndHeight.get(0));
							contentAndDimension.add(widthAndHeight.get(1));
						}
						
						else if(isVideo.contains(ext) || isAudio.contains(ext)) {
							contentAndDimension.add(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
						}
						
						else {
							contentAndDimension.add(outputStream.toString());
						}
					} catch (Exception e) {
						System.out.println("Read file content error : " + e.getMessage());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return contentAndDimension;
	}
	
	

	// Utility method to get the parent path
	private String getParentPath(String path) {
		int lastSlash = path.lastIndexOf('/');
		return lastSlash == -1 ? "" : path.substring(0, lastSlash);
	}

	// Fetches file structure from a specific branch
	public JSONArray getFileStructure(File repoPath, String branchName) {
		JSONArray rootArray = new JSONArray();

		if (repoPath != null) {
			try (Git git = Git.open(repoPath)) {
				Repository repository = git.getRepository();

				// Resolve the HEAD commit for the branch
				ObjectId branchHead = repository.resolve("refs/heads/" + branchName);
				if (branchHead == null) {
					return rootArray; // Empty if no branch found
				}

				try (RevWalk revWalk = new RevWalk(repository); TreeWalk treeWalk = new TreeWalk(repository)) {

					RevCommit commit = revWalk.parseCommit(branchHead);
					RevTree tree = commit.getTree(); // Get the tree of the specific branch commit
					treeWalk.addTree(tree);
					treeWalk.setRecursive(false);

					Map<String, JSONObject> fileMap = new HashMap<>();

					while (treeWalk.next()) {
						String path = treeWalk.getPathString();
						// Skip unwanted paths that are irrelevant to the branch
						if (path.startsWith(".git") || path.startsWith("hooks") || path.startsWith("branches")
								|| path.startsWith("refs")) {
							continue;
						}

						JSONObject fileJson = new JSONObject();
						fileJson.put("name", treeWalk.getNameString());

						boolean isFolder = treeWalk.isSubtree();

						if (isFolder) {
							fileJson.put("type", "folder");
							fileJson.put("children", new JSONArray());
							treeWalk.enterSubtree();
						} else {
							fileJson.put("type", "file");
//                            fileJson.put("content", readFileContent(repoPath, branchName, path));
							fileJson.put("content", "");
							fileJson.put("filepath", path);
						}

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

	public JSONArray getCommitChanges(Repository repository, String commitHash) {
		JSONArray changesArray = new JSONArray();

		try (RevWalk revWalk = new RevWalk(repository)) {
			ObjectId commitId = repository.resolve(commitHash);
			RevCommit commit = revWalk.parseCommit(commitId);

			RevCommit parentCommit = (commit.getParentCount() > 0) ? revWalk.parseCommit(commit.getParent(0)) : null;

			try (DiffFormatter diffFormatter = new DiffFormatter(new ByteArrayOutputStream())) {
				diffFormatter.setRepository(repository);
				diffFormatter.setDetectRenames(true);

				if (parentCommit == null) {
					// FIRST COMMIT: List all files in the commit
					try (TreeWalk treeWalk = new TreeWalk(repository)) {
						treeWalk.addTree(commit.getTree());
						treeWalk.setRecursive(true);

						while (treeWalk.next()) {
							JSONObject fileJson = new JSONObject();
							String filePath = treeWalk.getPathString();
							fileJson.put("path", filePath);
							fileJson.put("changeType", "ADD");

							// Fetch file content from the first commit
							String content = readFileContentFromCommit(repository, commit, filePath);
							fileJson.put("newContent", content != null ? content : "");

							changesArray.put(fileJson);
						}
					}
				} else {
					AbstractTreeIterator oldTree = (parentCommit != null) ? prepareTreeParser(repository, parentCommit)
							: null;
					AbstractTreeIterator newTree = prepareTreeParser(repository, commit);
					List<DiffEntry> diffs = diffFormatter.scan(oldTree, newTree);

					for (DiffEntry entry : diffs) {
						String filePath = entry.getNewPath();
						if (filePath.equals("/dev/null"))
							continue; // Skip non-existent files

						JSONObject fileJson = new JSONObject();
						fileJson.put("path", filePath);
						fileJson.put("changeType", entry.getChangeType().name());

						// Store changes with line numbers
						JSONArray changesArrayForFile = new JSONArray();
						diffFormatter.format(entry);
						EditList editList = diffFormatter.toFileHeader(entry).toEditList();

						for (Edit edit : editList) {
							JSONObject changeJson = new JSONObject();
							changeJson.put("startLineOld", edit.getBeginA() + 1); // Line number in old file
							changeJson.put("endLineOld", edit.getEndA());
							changeJson.put("startLineNew", edit.getBeginB() + 1); // Line number in new file
							changeJson.put("endLineNew", edit.getEndB());

							// Fetch old content
							if (parentCommit != null && entry.getChangeType() != DiffEntry.ChangeType.ADD) {
								String oldContent = readFileContentFromCommit(repository, parentCommit,
										entry.getOldPath());
								changeJson.put("oldContent", oldContent != null ? oldContent : "File not found");
							}

							// Fetch new content
							String newContent = readFileContentFromCommit(repository, commit, filePath);
							changeJson.put("newContent", newContent != null ? newContent : "File not found");

							changesArrayForFile.put(changeJson);
						}

						fileJson.put("changes", changesArrayForFile);
						changesArray.put(fileJson);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return changesArray;
	}

	private AbstractTreeIterator prepareTreeParser(Repository repository, RevCommit commit) throws IOException {
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevTree tree = commit.getTree(); // Get the tree of the commit

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId()); // Parse the tree
			}

			return treeParser;
		}
	}

	private String readFileContentFromCommit(Repository repository, RevCommit commit, String filePath) {
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.addTree(commit.getTree());
			treeWalk.setRecursive(true);
			treeWalk.setFilter(PathFilter.create(filePath));

			if (!treeWalk.next()) {
				return ""; // File not found in this commit
			}

			ObjectId objectId = treeWalk.getObjectId(0);
			ObjectLoader loader = repository.open(objectId);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			loader.copyTo(outputStream);

			return outputStream.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error reading file: " + e.getMessage();
		}
	}

	public File zipRepository(File bareRepoPath, String branch, String zipFileName)
			throws IOException, GitAPIException {

		File tempDir = new File(bareRepoPath.getParent(), "temp_repo");
		if (tempDir.exists()) {
			deleteDirectory(tempDir); // Cleanup if it exists
		}

		// Initialize a new repository in temp directory
		try (Git git = Git.cloneRepository().setURI(bareRepoPath.getAbsolutePath()) // Clone from bare repo
				.setDirectory(tempDir) // Clone into temp directory
				.setBranch(branch) // Checkout specific branch
				.call()) {

			git.checkout().setName(branch).call();

		}

		File zipFile = new File(bareRepoPath.getParent(), zipFileName);
		try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zipOut = new ZipOutputStream(fos)) {
			zipDirectory(tempDir.toPath(), tempDir.toPath(), zipOut);
		}

		// Cleanup: Delete temp worsking directory
		deleteDirectory(tempDir);

		return zipFile;
	}

	private void zipDirectory(Path sourcePath, Path rootPath, ZipOutputStream zipOut) throws IOException {
		Files.walk(sourcePath).forEach(path -> {
			try {
				if (!Files.isDirectory(path)) {
					String zipEntryName = rootPath.relativize(path).toString();
					zipOut.putNextEntry(new ZipEntry(zipEntryName));
					Files.copy(path, zipOut);
					zipOut.closeEntry();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void deleteDirectory(File dir) throws IOException {
		if (!dir.exists()) {
			return; // Avoid errors if directory does not exist
		}

		File[] files = dir.listFiles();
		if (files != null) { // Check if directory is not empty
			for (File file : files) {
				deleteDirectory(file);
			}
		}

		if (!dir.delete()) {
			throw new IOException("Failed to delete: " + dir.getAbsolutePath());
		}
	}
	
	
	public ArrayList<String> getDimensionWidthandHeight(String base64Image){
		
		ArrayList<String> widthAndHeight = new ArrayList<String>();
		
		 try {
	            // Decode Base64 string to byte array
	            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

	            // Convert byte array to BufferedImage
	            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

	            if (image != null) {
	            	widthAndHeight.add(image.getWidth()+"");
	            	widthAndHeight.add(image.getHeight()+"");
	            } else {
	                System.out.println("Invalid image data in dimensions");
	            }
	            
	        } catch (Exception e) {
	        	System.out.println("Error in getting image dimensions");
	        }
		 
		 return widthAndHeight;
		
	}
}

