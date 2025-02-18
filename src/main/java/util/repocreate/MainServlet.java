
package util.repocreate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ReceivePackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.AdvertiseRefsHook;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.util.IO;


import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private static final String REPO_PATH = "/opt/repo/";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {
        
        String pathInfo = req.getPathInfo();
        System.out.println("GET request: " + pathInfo);
 
        if (pathInfo == null || !pathInfo.endsWith("/info/refs")) {
            resp.sendError(400, "Invalid Git request");
            return;
        }

        // Extract repository path
        String repoPath = pathInfo.replace("/info/refs", "").replaceFirst("^/", "");
        System.out.println(repoPath);
        File repoDir = new File(REPO_PATH, repoPath);

        if (!repoDir.exists()) {
            resp.sendError(404, "Repository not found");
            return;
        }

        String service = req.getParameter("service");
        if (!"git-upload-pack".equals(service) && !"git-receive-pack".equals(service)) {
            resp.sendError(400, "Unsupported service: " + service);
            return;
        }

        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
            resp.setContentType("application/x-" + service + "-advertisement");
            resp.setHeader("Cache-Control", "no-cache");

            ServletOutputStream out = resp.getOutputStream();
            PacketLineOut packetOut = new PacketLineOut(out);

            // Write service header
            packetOut.writeString("# service=" + service + "\n");
            packetOut.end(); // Flush packet

            // Advertise refs
            if ("git-upload-pack".equals(service)) {
                UploadPack uploadPack = new UploadPack(repo);
                uploadPack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
            } else if ("git-receive-pack".equals(service)) {
                ReceivePack receivePack = new ReceivePack(repo);
                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                receivePack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
            }

            // Final flush packet
//            packetOut.end();

            System.out.println("Refs advertised successfully for: " + repoDir);

        } catch (Exception e) {
            resp.sendError(500, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException {
        
        String pathInfo = req.getPathInfo();
        System.out.println("POST request: " + pathInfo);

        if (pathInfo == null || (!pathInfo.contains("/git-upload-pack") && !pathInfo.contains("/git-receive-pack"))) {
            resp.sendError(400, "Invalid Git request");
            return;
        }

        // Extract repository path
        String repoPath = pathInfo.replace("/git-upload-pack", "")
                                  .replace("/git-receive-pack", "")
                                  .replaceFirst("^/", "");
        File repoDir = new File(REPO_PATH, repoPath);

        if (!repoDir.exists()) {
            resp.sendError(404, "Repository not found");
            return;
        }

        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {

            if (pathInfo.contains("/git-upload-pack")) {
                System.out.println("Handling git-upload-pack (pull/fetch)");
                resp.setContentType("application/x-git-upload-pack-result");

                InputStream input = req.getInputStream();
                OutputStream output = resp.getOutputStream();

                UploadPack uploadPack = new UploadPack(repo);
                uploadPack.upload(input, output, null);
                output.flush();

                System.out.println("Pull/fetch successful for: " + repoDir);

            } else if (pathInfo.contains("/git-receive-pack")) {
                System.out.println("Handling git-receive-pack (push)");
                resp.setContentType("application/x-git-receive-pack-result");

                InputStream input = req.getInputStream();
                OutputStream output = resp.getOutputStream();

                ReceivePack receivePack = new ReceivePack(repo);
                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                receivePack.setAtomic(true); // Ensure atomic ref updates

                receivePack.receive(input, output, null);
                output.flush();

                System.out.println("Push successful for: " + repoDir);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (!resp.isCommitted()) {
                resp.sendError(500, "Server error: " + e.getMessage());
            }
        }
    }
}
