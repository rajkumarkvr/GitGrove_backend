package services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            filePart.getInputStream().transferTo(Files.newOutputStream(targetFile.toPath())); 

            git.add()
               .addFilepattern(filename)
               .call();

            git.commit()
               .setMessage(commitMessage)
               .setAuthor(author.getUsername(), author.getEmailaddress())
               .call();
            
            
            git.push().call();
            
            return true;
	
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("Adding file error : "+e.getMessage());
			return false;
		}

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
