package listeners;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

public class SshGitListener implements ServletContextListener {
    private SshServer sshd;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2222); // Change this if necessary
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));

//        // Use database-based authentication
//        sshd.setPasswordAuthenticator((username, password, session) -> 
////            DatabaseHelper.authenticateUser(username, password)
//        );

        // Public Key Authentication (Optional, if you want to support SSH Keys)
        sshd.setPublickeyAuthenticator(new DefaultAuthorizedKeysAuthenticator(false));

        // Git command handling
//        sshd.setCommandFactory(command -> {
//            System.out.println("Received SSH command: " + command);
//            return new GitCommandHandler(command);
//        });

        // Enable SFTP (Optional: For file transfers)
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));

        try {
            sshd.start();
            System.out.println("✅ SSH Server started on port 22...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (sshd != null) {
                sshd.stop();
                System.out.println("❌ SSH Server stopped.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
