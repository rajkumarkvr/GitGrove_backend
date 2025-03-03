package models.dao;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.mysql.cj.xdevapi.JsonArray;

import services.DBconnection;

public class SSH_KeyDAO {
	static private final String SCRIPTFILEPATH = "\"/home/git/my-git-script.sh\"";
	static private final String SCRIPTACTION = ",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty";
	private static final String AUTHORIZED_KEYS_PATH = "/home/git/.ssh/authorized_keys";
	private static final String SUDO_PASSWORD = "Rajkumardev@753";
	private static final String SSH_DIR_PATH = "/home/git/.ssh";
	private static final String GIT_HOME_PATH = "/home/git";
	static SSH_KeyDAO ssh_KeyDAO = null;

	private SSH_KeyDAO() {
	}

	public static SSH_KeyDAO getInstance() {
		if (ssh_KeyDAO == null) {
			ssh_KeyDAO = new SSH_KeyDAO();
		}
		return ssh_KeyDAO;
	}

	public void insertKey(int user_id, String sshKey, String description) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection
					.prepareStatement("insert into sshkeys(user_id,ssh_key,description) values(?,?,?)");
			stmt.setInt(1, user_id);
			stmt.setString(2, sshKey);
			stmt.setString(3, description);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("SSH key insertion error: " + e.getMessage());
		}
	}

	public boolean deleteSshKey(int id) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("delete from sshkeys where id=?");
			stmt.setInt(1, id);
			int affected = stmt.executeUpdate();
			return affected > 0;
		} catch (Exception e) {
			System.out.println("SSH key deletion error: " + e.getMessage());
		}
		return false;
	}

	public ArrayList<JSONObject> getsshKeysByUserID(int userid) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection
					.prepareStatement("select id,ssh_key,description,inserted_at from sshkeys where user_id=?");
			stmt.setInt(1, userid);
			ResultSet rs = stmt.executeQuery();
			ArrayList<JSONObject> userKeys = new ArrayList<JSONObject>();
			while (rs.next()) {
				JSONObject obj = new JSONObject();
				obj.put("id", rs.getInt(1));
				obj.put("publicKey", rs.getString(2));
				obj.put("description", rs.getString(3));
				obj.put("createdAt", rs.getTimestamp(4).toLocalDateTime().toString());
				userKeys.add(obj);
			}

			return userKeys;

		} catch (Exception e) {
			System.out.println("SSH keys retrival by userid: " + e.getMessage());
		}
		return null;
	}

	public String getsshKeyByrowID(int id) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select ssh_key from sshkeys where id=?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getString(1);
			}

		} catch (Exception e) {
			System.out.println("SSH keys retrival by userid: " + e.getMessage());
		}
		return null;
	}

	public boolean isKeyExists(String key) {
		boolean isExists = false;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select id from sshkeys where ssh_key = ?");
			stmt.setString(1, key);
			ResultSet rs = stmt.executeQuery();
			isExists = rs.next();
		} catch (Exception e) {
			System.out.println("SSH key exists checking error: " + e.getMessage());
		}
		return isExists;
	}

	public String generateEntry(String username, String key) {
		return "command=" + SCRIPTFILEPATH + ",environment=\"GIT_USER=" + username + "\"" + SCRIPTACTION + " " + key;
	}
	
//    public void appendToAuthorizedKeys(String entry) {
//        try {
//            Path tempFile = Files.createTempFile("authkeys", ".tmp");
//            System.out.println("Temporary file created: " + tempFile.toString());
//
//            Path authKeysPath = Paths.get(AUTHORIZED_KEYS_PATH);
//            if (Files.exists(authKeysPath)) {
//                Files.copy(authKeysPath, tempFile, StandardCopyOption.REPLACE_EXISTING);
//                System.out.println("Copied existing authorized_keys to temp file.");
//            }
//
//            Files.writeString(tempFile, entry + "\n", StandardOpenOption.APPEND);
//            System.out.println("Appended new SSH key entry to temp file.");
//
//            Files.move(tempFile, authKeysPath, StandardCopyOption.REPLACE_EXISTING);
//            System.out.println("Moved temp file to " + AUTHORIZED_KEYS_PATH + " successfully.");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Error appending to authorized keys: " + e.getMessage());
//        }
//    }

	private void changeOwnerToGit() {
		try {
		ProcessBuilder chownToGitPb = new ProcessBuilder("bash", "-c", "echo \"" + SUDO_PASSWORD
				+ "\" | sudo -S chown git " + GIT_HOME_PATH + " " + SSH_DIR_PATH + " " + AUTHORIZED_KEYS_PATH);
		chownToGitPb.inheritIO();
		Process chownToGitProcess = chownToGitPb.start();
		int chownToGitExitCode = chownToGitProcess.waitFor();
		if (chownToGitExitCode != 0) {
			String errorOutput = new String(chownToGitProcess.getErrorStream().readAllBytes());
			throw new RuntimeException("Failed to change ownership back to git. Exit code: " + chownToGitExitCode
					+ ", Error: " + errorOutput);
		}
		System.out.println("Changed ownership of " + AUTHORIZED_KEYS_PATH + " back to git.");
		}catch(Exception e) {
			System.out.println("Error from change owner to git: " + e.getMessage());
		}
	}
	
	private void changeOwnerToRaj() {
		try {

			ProcessBuilder chownToRajPb = new ProcessBuilder("bash", "-c",
					"echo \"" + SUDO_PASSWORD + "\" | sudo -S chown raj-zstk371 " + GIT_HOME_PATH + " " + SSH_DIR_PATH
							+ " " + AUTHORIZED_KEYS_PATH);
			chownToRajPb.inheritIO();
			Process chownToRajProcess = chownToRajPb.start();
			int chownToRajExitCode = chownToRajProcess.waitFor();
			if (chownToRajExitCode != 0) {
				String errorOutput = new String(chownToRajProcess.getErrorStream().readAllBytes());
				throw new RuntimeException("Failed to change ownership to raj-zstk371. Exit code: " + chownToRajExitCode
						+ ", Error: " + errorOutput);
			}
			

		} catch (Exception e) {
			System.out.println("Error from change owner to raj: " + e.getMessage());
		}
	

}

	public void deleteKeyFromFile(String keyFormat) {
		
		try {
			changeOwnerToRaj();
		Path tempFile = Files.createTempFile("authkeys", ".tmp");
		
		
        System.out.println("Temporary file created: " + tempFile.toString());

    
        Path authKeysPath = Paths.get(AUTHORIZED_KEYS_PATH);
        if (Files.exists(authKeysPath)) {
            List<String> lines = Files.readAllLines(authKeysPath);
            
            String entryToDelete =keyFormat;
            
            List<String> filteredLines = lines.stream()
                .filter(line -> !line.trim().equals(entryToDelete.trim())) 
                .collect(Collectors.toList());
            
            
            if (lines.size() == filteredLines.size()) {
                System.out.println("No matching entry found for "+keyFormat);
            } else {
                Files.write(tempFile, filteredLines, StandardOpenOption.WRITE);
                System.out.println("Filtered out entry for "+keyFormat);
            }
        } else {
            System.out.println("No authorized_keys file exists; nothing to delete.");
            return; 
        }

 
        Files.move(tempFile, authKeysPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Moved temp file to " + AUTHORIZED_KEYS_PATH + " successfully.");
		}catch (Exception e) {
			System.out.println("Error from delete key file"+e.getMessage());
			
		}finally {
			changeOwnerToGit();
		}
	
	}

	public void appendToAuthorizedKeys(String entry) {
		try {

			
			changeOwnerToRaj();
			System.out.println("Changed ownership of " + AUTHORIZED_KEYS_PATH + " to raj-zstk371 temporarily.");

			Path tempFile = Files.createTempFile("authkeys", ".tmp");

			ProcessBuilder copyPb = new ProcessBuilder("bash", "-c", "echo \"" + SUDO_PASSWORD
					+ "\" | sudo -S -u raj-zstk371 cp " + AUTHORIZED_KEYS_PATH + " " + tempFile.toString());
			copyPb.inheritIO();
			Process copyProcess = copyPb.start();
			int copyExitCode = copyProcess.waitFor();
			if (copyExitCode != 0) {
				String errorOutput = new String(copyProcess.getErrorStream().readAllBytes());
				throw new RuntimeException("Failed to copy authorized_keys to temp file. Exit code: " + copyExitCode
						+ ", Error: " + errorOutput);
			}
			System.out.println("Copied existing authorized_keys to temp file.");

			Files.writeString(tempFile, entry + "\n", StandardOpenOption.APPEND);
			System.out.println("Appended new SSH key entry to temp file.");
			ProcessBuilder movePb = new ProcessBuilder("bash", "-c", "echo \"" + SUDO_PASSWORD
					+ "\" | sudo -S -u raj-zstk371 cp " + tempFile.toString() + " " + AUTHORIZED_KEYS_PATH);
			movePb.inheritIO();
			Process moveProcess = movePb.start();
			int moveExitCode = moveProcess.waitFor();
			if (moveExitCode != 0) {
				String errorOutput = new String(moveProcess.getErrorStream().readAllBytes());
				throw new RuntimeException("Failed to copy temp file to authorized_keys. Exit code: " + moveExitCode
						+ ", Error: " + errorOutput);
			}
			System.out.println("Copied temp file to " + AUTHORIZED_KEYS_PATH + " successfully.");

			changeOwnerToGit();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error appending to authorized keys: " + e.getMessage());
		}
	}

}