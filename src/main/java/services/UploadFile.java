package services;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import javax.servlet.http.Part;

import org.eclipse.jgit.api.Git;

import models.User;

public class UploadFile {

	private static final String WORKING_DIR = "/opt/temp_repos/user_repo";
	private static UploadFile uploadFile = null;

	public static UploadFile getInstance() {

		if (uploadFile == null) {
			uploadFile = new UploadFile();
		}

		return uploadFile;
	}

	public boolean addFile(String repoPath, Part filePart, String commitMessage, String branch, User author) {

		try {

			File workingDir = new File(WORKING_DIR);

			if (workingDir.exists()) {
				deleteDirectory(workingDir);
			}

			workingDir.mkdirs();

			Git.cloneRepository().setURI(repoPath).setDirectory(workingDir).setBranch(branch).call();

			Git git = Git.open(workingDir);

			String filename = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			File targetFile = new File(workingDir, filename);
			Files.copy(filePart.getInputStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			git.add().addFilepattern(filename).call();

			git.commit().setMessage(commitMessage).setAuthor(author.getUsername(), author.getEmailaddress()).call();

			git.push().call();

			return true;

		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Adding file error : " + e.getMessage());
			return false;
		}

	}

	public boolean uploadFilesToGit(Collection<Part> parts, String repoPath, String branch,
			String commitMessage) {
		File tempDir = new File("/tmp/git-working-dir"); // Temporary working directory

		if (!tempDir.exists())
			tempDir.mkdirs();

		try {
			// Clone the bare repository into a working directory
			Git git = Git.cloneRepository().setURI("file://" + repoPath).setDirectory(tempDir).setBranch(branch).call();

			for (Part part : parts) {
				if (part.getName().equals("files")) { // Process only file parts
					String fileName = part.getSubmittedFileName();
					String contentDisposition = part.getHeader("content-disposition");

					// Extract relative path
					String relativePath = extractRelativePath(contentDisposition);
					if (relativePath == null || relativePath.isEmpty()) {
						relativePath = fileName;
					}

					// Create full path inside working directory
					File targetFile = new File(tempDir, relativePath);
					targetFile.getParentFile().mkdirs(); // Create necessary folders

					try (InputStream input = part.getInputStream()) {
						Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}

					// Add file to Git
					git.add().addFilepattern(relativePath).call();
				}
			}

			// Commit changes
			git.commit().setMessage(commitMessage).setAuthor("User", "user@example.com").call();

			// Push changes
			git.push().call();

			git.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Extract relative path from Content-Disposition header
	private static String extractRelativePath(String contentDisposition) {
		if (contentDisposition != null) {
			for (String part : contentDisposition.split(";")) {
				if (part.trim().startsWith("filename")) {
					String fileName = part.split("=")[1].trim().replace("\"", "");
					if (fileName.contains("/")) { // Check for relative path
						return fileName;
					}
				}
			}
		}
		return null; // No relative path found
	}

	private void deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteDirectory(file);
					} else {
						file.delete();
					}
				}
			}
		}
		directory.delete();
	}

}
