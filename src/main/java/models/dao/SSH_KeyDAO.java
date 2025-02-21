package models.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
			stmt.executeQuery();
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
	
	public void appendToAuthorizedKeys(String entry) {
        try {
            Path authKeysPath = Paths.get(AUTHORIZED_KEYS_PATH);
            File authKeysFile = authKeysPath.toFile();

            // Ensure authorized_keys file exists
            if (!authKeysFile.exists()) {
                authKeysFile.createNewFile();
                setFilePermissions(authKeysPath, "rw-------"); // 600 for ~/.ssh/authorized_keys
            }

            // Append key with a newline
            Files.write(authKeysPath, (entry + "\n").getBytes(), StandardOpenOption.APPEND);

            // Ensure file has correct permissions
            setFilePermissions(authKeysPath, "rw-------");

            System.out.println("SSH key added successfully.");
        } catch (Exception e) {
            System.err.println("Error appending to authorized_keys: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setFilePermissions(Path path, String perms) {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(perms));
        } catch (Exception e) {
            // Windows does not support POSIX permissions
            System.out.println("Skipping permission setting, as POSIX permissions are not supported on this OS.");
        } 
    }

	
}
