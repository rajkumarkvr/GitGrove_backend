package util.repocreate;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.AdvertiseRefsHook;
import org.eclipse.jgit.transport.PacketLineOut;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;

public class GitHttpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String REPO_BASE = "/opt/repo/"; 
    private static final String GIT_PATH = "/usr/bin/git";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        System.out.println("GET request: " + pathInfo);

        if (pathInfo == null || !pathInfo.endsWith("/info/refs")) {
            resp.sendError(400, "Invalid Git request");
            return;
        }

        String repoPath = pathInfo.replace("/info/refs", "").replaceFirst("^/", "");
        File repoDir = new File(REPO_BASE, repoPath);
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
            packetOut.writeString("# service=" + service + "\n");
            packetOut.end();

            if ("git-upload-pack".equals(service)) {
                UploadPack uploadPack = new UploadPack(repo);
                uploadPack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
            } else if ("git-receive-pack".equals(service)) {
                ReceivePack receivePack = new ReceivePack(repo);
                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                receivePack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
            }

            System.out.println("Refs advertised successfully for: " + repoDir);
        } catch (Exception e) {
            resp.sendError(500, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        System.out.println("POST request: " + pathInfo);

        if (pathInfo == null || (!pathInfo.contains("/git-upload-pack") && !pathInfo.contains("/git-receive-pack"))) {
            resp.sendError(400, "Invalid Git request");
            return;
        }

        String repoPath = pathInfo.replace("/git-upload-pack", "").replace("/git-receive-pack", "").replaceFirst("^/", "");
        File repoDir = new File(REPO_BASE, repoPath);

        if (!repoDir.exists()) {
            resp.sendError(404, "Repository not found");
            return;
        }

        if (pathInfo.contains("/git-upload-pack")) {
            handleGitCommand(req, resp, repoDir, "upload-pack");
        } else if (pathInfo.contains("/git-receive-pack")) {
            handleJGitPush(req, resp, repoDir);
        }
    }

    private void handleGitCommand(HttpServletRequest req, HttpServletResponse resp, File repoDir, String command) throws IOException {
        try {
            String[] cmd = {GIT_PATH, command, "--stateless-rpc", repoDir.getAbsolutePath()};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (OutputStream gitIn = process.getOutputStream(); InputStream httpIn = req.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = httpIn.read(buffer)) != -1) {
                    gitIn.write(buffer, 0, bytesRead);
                }
            }

            resp.setContentType("application/x-git-" + command + "-result");
            try (InputStream gitOut = process.getInputStream(); OutputStream httpOut = resp.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = gitOut.read(buffer)) != -1) {
                    httpOut.write(buffer, 0, bytesRead);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Git command failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            resp.sendError(500, "Server error: " + e.getMessage());
        }
    }

//    private void handleJGitPush(HttpServletRequest req, HttpServletResponse resp, File repoDir) throws IOException {
//        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
//            System.out.println("Handling JGit push for repo: " + repoDir);
//            resp.setContentType("application/x-git-receive-pack-result");
//
//            InputStream input = req.getInputStream();
//            OutputStream output = resp.getOutputStream();
//
//            ReceivePack receivePack = new ReceivePack(repo);
//            receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
//            receivePack.setAtomic(true);
//            receivePack.receive(input, output, null);
//            output.flush();
//
//            System.out.println("Push successful for: " + repoDir);
//        } catch (Exception e) {
//            resp.sendError(500, "Push error: " + e.getMessage());
//        }
//    }
    
//    private void handleJGitPush(HttpServletRequest req, HttpServletResponse resp, File repoDir) throws IOException {
//        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
//            System.out.println("Handling JGit push for repo: " + repoDir);
//            resp.setContentType("application/x-git-receive-pack-result");
//
//            InputStream input = req.getInputStream();
//            OutputStream output = resp.getOutputStream();
//
//            ReceivePack receivePack = new ReceivePack(repo);
//            receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
//            receivePack.setAtomic(true);
//
//            try {
//                receivePack.receive(input, output, null);
//                output.flush();
//                System.out.println("Push successful for: " + repoDir);
//            } catch (Exception e) {
//                System.err.println("Push failed: " + e.getMessage());
//                e.printStackTrace();
//                if (!resp.isCommitted()) {
//                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Push error: " + e.getMessage());
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Repository initialization failed: " + e.getMessage());
//            e.printStackTrace();
//            if (!resp.isCommitted()) {
//                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Repository error: " + e.getMessage());
//            }
//        }
//    }
//    private void handleJGitPush(HttpServletRequest req, HttpServletResponse resp, File repoDir) throws IOException {
//        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
//            System.out.println("Handling JGit push for repo: " + repoDir);
//            resp.setContentType("application/x-git-receive-pack-result");
//
//            try (InputStream input = req.getInputStream();
//                 OutputStream output = resp.getOutputStream()) {
//
//                ReceivePack receivePack = new ReceivePack(repo);
//                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
//                receivePack.setAtomic(true);
//                receivePack.receive(input, output, null);
//
//                output.flush(); // Ensure all data is flushed
//
//                System.out.println("Push successful for: " + repoDir);
//            }
//        } catch (Exception e) {
//            System.err.println("Push failed: " + e.getMessage());
//            if (!resp.isCommitted()) {  // Ensure the response has not been committed yet
//                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Push error: " + e.getMessage());
//            }
//        }
//    }

//    private void handleJGitPush(HttpServletRequest req, HttpServletResponse resp, File repoDir) throws IOException {
//        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
//            System.out.println("Handling JGit push for repo: " + repoDir);
//            
//            // Set correct content type
//            resp.setContentType("application/x-git-receive-pack-result");
//            resp.setHeader("Cache-Control", "no-cache");
//
//            try (InputStream input = req.getInputStream();
//                 OutputStream output = resp.getOutputStream()) {
//
//                ReceivePack receivePack = new ReceivePack(repo);
//                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
//                receivePack.setAtomic(true);
//
//                // Debugging: Print request details
//                System.out.println("Processing push request from client...");
//
//                receivePack.receive(input, output, null);
//
//                // Ensure the output stream is properly flushed before closing
//                output.flush();
//
//                System.out.println("Push completed successfully for: " + repoDir);
//            }
//        } catch (IOException ioEx) {
//            System.err.println("IO Error: " + ioEx.getMessage());
//            if (!resp.isCommitted()) {
//                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O Error: " + ioEx.getMessage());
//            }
//        } catch (Exception e) {
//            System.err.println("Push failed: " + e.getMessage());
//            e.printStackTrace();
//            if (!resp.isCommitted()) {
//                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Push error: " + e.getMessage());
//            }
//        }
//    }
    private void handleJGitPush(HttpServletRequest req, HttpServletResponse resp, File repoDir) throws IOException {
        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
            System.out.println("Handling JGit push for repo: " + repoDir);
            
            resp.setContentType("application/x-git-receive-pack-result");
            resp.setHeader("Cache-Control", "no-cache");

            try (InputStream input = req.getInputStream();
                 OutputStream output = resp.getOutputStream()) {

                ReceivePack receivePack = new ReceivePack(repo);
                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                receivePack.setAtomic(true);
                receivePack.setTimeout(3600); // Avoid timeout issues

                System.out.println("Processing push request from client...");

                receivePack.receive(input, output, null);

                // Remove the explicit flush as receivePack.receive() handles it
                System.out.println("Push completed successfully for: " + repoDir);
            }
        } catch (SocketException se) {
            System.err.println("Socket error (client closed connection): " + se.getMessage());
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            System.err.println("IO Error: " + ioEx.getMessage());
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "I/O Error: " + ioEx.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Push failed: " + e.getMessage());
            e.printStackTrace();
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Push error: " + e.getMessage());
            }
        }
    }
}
