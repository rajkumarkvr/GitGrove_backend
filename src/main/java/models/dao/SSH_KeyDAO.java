package models.dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import services.DBconnection;

public class SSH_KeyDAO {
	
	static private final String SCRIPTFILEPATH = "\"/home/git/my-git-script.sh\"";
	static private final String SCRIPTACTION = ",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty";
	private static final String AUTHORIZED_KEYS_PATH = "/home/git/.ssh/authorized_keys";
	
	static SSH_KeyDAO ssh_KeyDAO = null;
	
	private SSH_KeyDAO() {
		
	}
	
	public static SSH_KeyDAO getInstance() {
		if(ssh_KeyDAO == null){
			ssh_KeyDAO = new SSH_KeyDAO();
		}
		
		return ssh_KeyDAO;
	}
	
	public void insertKey(int user_id, String sshKey, String description) {
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into sshkeys(user_id,ssh_key,description) values(?,?,?)");
			stmt.setInt(1, user_id);
			stmt.setString(2, sshKey);
			stmt.setString(3, description);
			stmt.executeUpdate();
		} catch (Exception e) {
			System.out.println("SSH key insertion error : "+e.getMessage());
		}
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
			System.out.println("SSH key exists checking error : "+e.getMessage());
		}
		return isExists;
	}
	
	public String generateEntry(String username, String key) {	
		return "command="+SCRIPTFILEPATH+",environment=\"GIT_USER="+username+"\""+SCRIPTACTION+" "+key;
	}
	
	
	public void appendToAuthorizedKeys(String entry){
		try {
			 Path tempFile = Files.createTempFile("authkeys", ".tmp");
			    if (Files.exists(Paths.get(AUTHORIZED_KEYS_PATH))) {
			        Files.copy(Paths.get(AUTHORIZED_KEYS_PATH), tempFile, StandardCopyOption.REPLACE_EXISTING);
			    }
			    Files.write(tempFile, entry.getBytes(), StandardOpenOption.APPEND);
			    Files.move(tempFile, Paths.get(AUTHORIZED_KEYS_PATH), StandardCopyOption.REPLACE_EXISTING);
			    Files.setPosixFilePermissions(Paths.get(AUTHORIZED_KEYS_PATH), PosixFilePermissions.fromString("rw-------"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Appending to auhtorized keys : "+e.getMessage());
		}
	}
	
}
