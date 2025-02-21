package models.dao;

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

import services.DBconnection;

public class SSH_KeyDAO {
    static private final String SCRIPTFILEPATH = "\"/home/git/my-git-script.sh\"";
    static private final String SCRIPTACTION = ",no-port-forwarding,no-X11-forwarding,no-agent-forwarding,no-pty";
    private static final String AUTHORIZED_KEYS_PATH = "/home/git/.ssh/authorized_keys";
    private static final String SUDO_PASSWORD = "Rajkumardev@371"; // Replace with actual password
    
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
            PreparedStatement stmt = connection.prepareStatement("insert into sshkeys(user_id,ssh_key,description) values(?,?,?)");
            stmt.setInt(1, user_id);
            stmt.setString(2, sshKey);
            stmt.setString(3, description);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("SSH key insertion error: " + e.getMessage());
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
            System.out.println("SSH key exists checking error: " + e.getMessage());
        }
        return isExists;
    }
    
    public String generateEntry(String username, String key) {    
        return "command=" + SCRIPTFILEPATH + ",environment=\"GIT_USER=" + username + "\"" + SCRIPTACTION + " " + key;
    }
    
    public void appendToAuthorizedKeys(String entry) {
        try {
        	Path tempFile = Files.createTempFile("authkeys", ".tmp");
            System.out.println("Temporary file created: " + tempFile.toString());

            Path authKeysPath = Paths.get(AUTHORIZED_KEYS_PATH);
//            if (Files.exists(authKeysPath)) {
            	System.out.println("Auth contents"+Files.readAllLines(authKeysPath));
                
                Files.copy(authKeysPath, tempFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied existing authorized_keys to temp file.");
//            }

            System.out.println(Files.readAllLines(tempFile));
            Files.writeString(tempFile, entry + "\n", StandardOpenOption.APPEND);
            System.out.println("Appended new SSH key entry to temp file.");
        
            
            System.out.println(Files.readAllLines(tempFile));
            
            ProcessBuilder copyPb = new ProcessBuilder("bash", "-c",
                    "echo \"" + SUDO_PASSWORD + "\" | sudo -S -u raj-zstk371 cp " + tempFile.toString() + " " + AUTHORIZED_KEYS_PATH
                );
                copyPb.inheritIO(); // To see output/error in console
                Process copyProcess = copyPb.start();
                int copyExitCode = copyProcess.waitFor();
                if (copyExitCode != 0) {
                    String errorOutput = new String(copyProcess.getErrorStream().readAllBytes());
                    throw new RuntimeException("Failed to copy authorized_keys to temp file. Exit code: " + copyExitCode + ", Error: " + errorOutput);
                }
 
            
//
            
            
//            Files.copy(authKeysPath, tempFile, StandardCopyOption.REPLACE_EXISTING);
//            ProcessBuilder pb = new ProcessBuilder("bash", "-c",
//            	    "echo \"" + SUDO_PASSWORD + "\" | sudo -S -u git mv " + tempFile.toString() + " " + AUTHORIZED_KEYS_PATH
//            	);
//            	pb.inheritIO();
//            	Process process = pb.start();
//            	int exitCode = process.waitFor();
//            	if (exitCode != 0) {
//            	    String errorOutput = new String(process.getErrorStream().readAllBytes());
//            	    throw new RuntimeException("Failed to move temp file to authorized_keys. Exit code: " + exitCode + ", Error: " + errorOutput);
//            	}
            System.out.println("Moved temp file to " + AUTHORIZED_KEYS_PATH + " successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error appending to authorized keys: " + e.getMessage());
        }
    }
}