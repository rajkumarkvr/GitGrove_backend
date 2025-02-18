package util.repocreate;
import org.eclipse.jgit.api.Git;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
public class CreateRepoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
//    private static final String REPO_PATH = "/home/raj-zstk371/Documents/Rajkumar/FeedbackSystem/src/main/webapp/all-repos";
  private static final String REPO_PATH = "/opt/all-repos";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String repoName = req.getParameter("repoName");

        if (username == null || username.trim().isEmpty() || repoName == null || repoName.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and repository name are required.");
            return;
        }

        // Create user folder
        File userFolder = new File(REPO_PATH, username);
        if (!userFolder.exists()) {
            userFolder.mkdirs();  // Create user directory if not exists
        }

        // Define repo path
        File repoDir = new File(userFolder, repoName + ".git");

        if (repoDir.exists()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Repository already exists.");
            return;
        }

        try {
            // Initialize a bare repository
            Git.init().setBare(true).setDirectory(repoDir).call();
            System.out.println("Repository created:http://localhost:8080/FeedbackSystem/CreateRepoServlet" + username + "/" + repoName + ".git");
            resp.getWriter().write("Repository created: http://yourserver/git/" + username + "/" + repoName + ".git");
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating repository: " + e.getMessage());
        }
    }
}
