package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class PermissionManager {
	
	private static String password;
	public static void setPassword(String pass) {
		password =pass;
	}
	public static void setOwner(File repoDir, String userGroup) throws IOException, InterruptedException {
	      if (!repoDir.exists()) {
	          throw new IOException("Directory does not exist: " + repoDir.getAbsolutePath());
	      }
	   
	      // Construct and execute the chown commandString password = "your-sudo-password"; // Replace with actual sudo password
	      ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
	              "echo " + password + " | sudo -S chown -R " + userGroup + " " + repoDir.getAbsolutePath());;
	      processBuilder.inheritIO(); // To see the output in the console

	      Process process = processBuilder.start();
	      int exitCode = process.waitFor(); // Wait for command execution

	      if (exitCode == 0) {
	          System.out.println("Ownership changed successfully to " + userGroup + " for " + repoDir.getAbsolutePath());
	      } else {
	          System.err.println("Failed to change ownership. Exit code: " + exitCode);
	      }
	  }

	  public static void setRecursivePermissions(Path path, String permissionString) throws IOException {
	      Set<PosixFilePermission> perms = PosixFilePermissions.fromString(permissionString);

	      // Walk through all files & directories
	      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	          @Override
	          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
	              Files.setPosixFilePermissions(file, perms);  // Set permissions for file
	              return FileVisitResult.CONTINUE;
	          }

	          @Override
	          public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
	              Files.setPosixFilePermissions(dir, perms);  // Set permissions for directory
	              return FileVisitResult.CONTINUE;
	          }
	      });
	  }
}
