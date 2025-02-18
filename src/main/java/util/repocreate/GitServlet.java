package util.repocreate;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.AdvertiseRefsHook;
import org.eclipse.jgit.transport.PacketLineIn;
import org.eclipse.jgit.transport.PacketLineOut;
import org.eclipse.jgit.transport.ReceivePack;
import org.eclipse.jgit.transport.RefAdvertiser;
import org.eclipse.jgit.transport.UploadPack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;


public class GitServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private static final String REPO_PATH = "/opt/repo/";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    String pathInfo = req.getPathInfo();
	    System.out.println("POST request: " + pathInfo);

	    // Validate the request URL
	    if (pathInfo == null || (!pathInfo.contains("/git-upload-pack") && !pathInfo.contains("/git-receive-pack"))) {
	        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Git request");
	        return;
	    }

	    // Extract repository path (e.g., "Rajan/rajanRepo.git")
	    String repoPath = pathInfo.replace("/git-upload-pack", "")
	                              .replace("/git-receive-pack", "")
	                              .replaceFirst("^/", "");
	    File repoDir = new File(REPO_PATH, repoPath);

	    if (!repoDir.exists()) {
	        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repo not found: " + repoDir);
	        return;
	    }

	    try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {

	        if (pathInfo.contains("/git-upload-pack")) {
	        	
//	        	System.out.println("upload pack");
	        	  resp.setContentType("application/x-git-upload-pack-result");

	        	    // Use raw streams
	        	    InputStream input = req.getInputStream();
	        	    OutputStream output = resp.getOutputStream();

	        	    UploadPack uploadPack = new UploadPack(repo);
	        	   
	        	    uploadPack.upload(input, output, null);
	        	    output.flush();

	        	    System.out.println("Pull successful for: " + repoDir);
//	        	System.out.println("upload-pack");
//	            // Handle pull (fetch) operation
//	            resp.setContentType("application/x-git-upload-pack-result");
//       
//	       
//	            // Wrap streams for reliable binary transfer.
//	            try (BufferedInputStream bin = new BufferedInputStream(req.getInputStream());
//	                 BufferedOutputStream bout = new BufferedOutputStream(resp.getOutputStream())) {
//	                 
//	                // (Optional) Log the content length for debugging
//	                System.out.println("POST content length: " + req.getContentLength());
//	                
//	                UploadPack uploadPack = new UploadPack(repo);
//	                // Process the client's pack request.
//	                uploadPack.upload(bin, bout, System.err);
//	                bout.flush();
//	                System.out.println("Sent packfile for pull: " + repoDir);
//	            }
	        } else if (pathInfo.contains("/git-receive-pack")) {
	        	System.out.println("Handling git-receive-pack (push)");
	        	 resp.setContentType("application/x-git-receive-pack-result");

	             ReceivePack receivePack = new ReceivePack(repo);
	             receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
	             receivePack.setAtomic(true); // Enable atomic ref updates

	             InputStream input = req.getInputStream();
	             OutputStream output = resp.getOutputStream();

	             // Process the push
	             receivePack.receive(input, output, null);
	             output.flush();

	             System.out.println("Push successful for: " + repoDir);
	        	
//	        	System.out.println("git-receive-pack\"");
//	            // Handle push operation
//	        	resp.setContentType("application/x-git-receive-pack-result");
//	        	 ReceivePack receivePack = new ReceivePack(repo);
//	             receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
//	   
//	             InputStream input = req.getInputStream();
//	             OutputStream output = resp.getOutputStream();
//
//	             receivePack.receive(input, output, null);
//	             output.flush();
//
//	  
//
//	            System.out.println("Push successful for: " + repoDir);
//	                System.out.println("Received packfile for push: " + repoDir);
	            
	        }
	    } catch (Exception e) {
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
	    	if (!resp.isCommitted()) {
	    	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
	    	}
	    }
	}

	
	//working code
//	@Override
//	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//	    String pathInfo = req.getPathInfo();
//	    System.out.println("POST request: " + pathInfo);
//
//	    if (pathInfo == null || (!pathInfo.contains("/git-upload-pack") && !pathInfo.contains("/git-receive-pack"))) {
//	        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Git request");
//	        return;
//	    }
//
//	    // Extract repo path (e.g., "/Rajkumar/project2.git")
//	    String repoPath = pathInfo.replace("/git-upload-pack", "").replace("/git-receive-pack", "").replaceFirst("^/", "");
//	    File repoDir = new File(REPO_PATH, repoPath);
//
//	    if (!repoDir.exists()) {
//	        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repo not found: " + repoDir);
//	        return;
//	    }
//
//	    try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
//	    	
//	        if (pathInfo.contains("/git-upload-pack")) {
//	            // Handle pull (fetch) operation
//	            resp.setContentType("application/x-git-upload-pack-result");
//	            UploadPack uploadPack = new UploadPack(repo);
//	            InputStream input = req.getInputStream();
//	            OutputStream output = resp.getOutputStream();
//	            uploadPack.upload(input, output, null);
//	            System.out.println("Sent packfile for pull: " + repoDir);
//	        } else if (pathInfo.contains("/git-receive-pack")) {
//	            // Handle push operation
//	            resp.setContentType("application/x-git-receive-pack-result");
//	            ReceivePack receivePack = new ReceivePack(repo);
//	            InputStream input = req.getInputStream();
//	            OutputStream output = resp.getOutputStream();
//	            receivePack.receive(input, output, null);
//	            System.out.println("Received packfile for push: " + repoDir);
//	        }
//	    } catch (Exception e) {
//	        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
//	    }
//	}
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
//    	System.out.println("POST");
//        // This handles 'git push' requests
//    	System.out.println(req.getPathInfo());
//    	 String pathInfo = req.getPathInfo(); // Example: "/Rajkumar/project1.git/info/refs"
//    	  String repoPath = pathInfo.replaceFirst("/git-upload-pack", "").replaceFirst("^/", "");
//    	  System.out.println(repoPath);
//        File repoDir = new File(REPO_PATH,repoPath);
//        System.out.println(repoDir.getAbsolutePath());
//        if (!repoDir.exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found");
//            return;
//        }
//        
//        System.out.println("before start");
//       
//
//        try (Repository repo = new FileRepositoryBuilder()
//                .setGitDir(repoDir)
//                .readEnvironment()
//                .findGitDir()
//                .build()) {
//
//            // Create an UploadPack instance for the repository
//            UploadPack uploadPack = new UploadPack(repo);
//
//            // Get the input stream from the client's request
//            InputStream input = req.getInputStream();
//
//            // Get the output stream to send the packfile to the client
//            OutputStream output = resp.getOutputStream();
//            System.out.println("inside");
//            // Process the client's request and stream the packfile
//            uploadPack.upload(input, output, null);
//            System.out.println("Does it works");
//        } catch (Exception e) {
//        	System.out.println(e.getMessage());
//        	
//        	System.out.println(e.getMessage());
//            // Handle errors
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing Git request: " + e.getMessage());
//        }
//    
//    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        System.out.println("GET request: " + pathInfo);

        if (pathInfo == null || !pathInfo.endsWith("/info/refs")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Git request");
            return;
        }

        String repoPath = pathInfo.replace("/info/refs", "").replaceFirst("^/", "");
        File repoDir = new File(REPO_PATH, repoPath);

        if (!repoDir.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repo not found: " + repoDir);
            return;
        }

        String service = req.getParameter("service");
        
        System.out.println("Printing service: "+service);
        if (!"git-upload-pack".equals(service) && !"git-receive-pack".equals(service)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported service: " + service);
            return;
        }

        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
            resp.setContentType("application/x-" + service + "-advertisement");
            resp.setHeader("Cache-Control", "no-cache");

            ServletOutputStream out = resp.getOutputStream();
            PacketLineOut packetOut = new PacketLineOut(out);

            // Write service header
            packetOut.writeString("# service=" + service + "\n");
            packetOut.end();

            // Advertise refs
            if ("git-upload-pack".equals(service)) {
                UploadPack uploadPack = new UploadPack(repo);
                uploadPack.setBiDirectionalPipe(false);
                uploadPack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
//                packetOut.end();

            } else if ("git-receive-pack".equals(service)) {
                ReceivePack receivePack = new ReceivePack(repo);
                receivePack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
                receivePack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
                packetOut.end();
                
            }else {
            	System.out.println("Fetch");
            }

            System.out.println("Advertised refs for: " + repoDir);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
    
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        String pathInfo = req.getPathInfo();
//        System.out.println("GET request: " + pathInfo);
//
//        if (pathInfo == null || !pathInfo.endsWith("/info/refs")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Git request");
//            return;
//        }
//
//        String repoPath = pathInfo.replace("/info/refs", "").replaceFirst("^/", "");
//        File repoDir = new File(REPO_PATH, repoPath);
//
//        if (!repoDir.exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repo not found: " + repoDir);
//            return;
//        }
//
//        
//        System.out.println("Hii");
//        String service = req.getParameter("service");
////        if (!"git-upload-pack".equals(service)) {
////            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported service: " + service);
////            return;
////        }
//        System.out.println("sss");
//        try (Repository repo = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
//            resp.setContentType("application/x-git-upload-pack-advertisement");
//            resp.setHeader("Cache-Control", "no-cache");
//
//            ServletOutputStream out = resp.getOutputStream();
//            PacketLineOut packetOut = new PacketLineOut(out);
//
//            // Write service header
//            packetOut.writeString("# service=git-upload-pack\n");
//            packetOut.end();
//
//            // Advertise refs
//            UploadPack uploadPack = new UploadPack(repo);
//            uploadPack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
//            uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
//
//            System.out.println("Advertised refs for: " + repoDir);
//        } catch (Exception e) {
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
//        }
//    }
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Extract repository path
//        String pathInfo = req.getPathInfo(); // Example: "/Rajkumar/project1.git/info/refs"
//System.out.println(pathInfo+"On get");
//        if (pathInfo == null || pathInfo.equals("/")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Repository path required");
//            return;
//        }
//
//        // Extract repo path correctly (removing "/info/refs")
//        String repoPath = pathInfo.replaceFirst("/info/refs$", "").replaceFirst("^/", "");
////        replaceFirst("temp-", "")
//        File repoDir = new File("/opt/all-repos", repoPath);
//
//        // Debugging output
//        System.out.println("Requested repo: " + repoPath);
//        System.out.println("Absolute path: " + repoDir.getAbsolutePath());
//
//        // Validate repo existence
//        if (!repoDir.exists() || !new File(repoDir, "HEAD").exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found");
//            return;
//        }
//
//        // Ensure the client requested a valid Git service
//        String service = req.getParameter("service");
//        if (service == null || !service.equals("git-upload-pack")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid service request");
//            return;
//        }
//
//        try (Repository repository = new FileRepositoryBuilder()
//                .setGitDir(repoDir)
//                .build()) {
//
//            UploadPack uploadPack = new UploadPack(repository);
//            uploadPack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT);
////            uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(new PacketLineOut(resp.getOutputStream())));
//
//
//            // Fix: Add correct content type
//            resp.setContentType("application/x-" + service + "-advertisement");
//
//            try (ServletOutputStream out = resp.getOutputStream()) {
//                PacketLineOut packetOut = new PacketLineOut(out);
//
//                // Fix: Send correct "# service=git-upload-pack" header
//                packetOut.writeString("# service=git-upload-pack\n");
//                packetOut.end();
//
//                // Fix: Use correct advertisement method
//                uploadPack.sendAdvertisedRefs(new RefAdvertiser.PacketLineOutRefAdvertiser(packetOut));
//                System.out.println("Success");
//            }
//
//        } catch (Exception e) {
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fetch failed: " + e.getMessage());
//        }
//    }

//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Extract repository path
//        String pathInfo = req.getPathInfo();  // Example: "/Rajkumar/project1.git/info/refs"
//        
//        if (pathInfo == null || pathInfo.equals("/")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Repository path required");
//            return;
//        }
//
//        // Get actual repository path (removing "/info/refs" or any extra path)
//        String repoPath = pathInfo.replaceFirst("/info/refs$", ""); // Removes "/info/refs" if present
//     
//        System.out.println(repoPath);
//        File repoDir = new File("/opt/all-repos" + repoPath);
//System.out.println(repoDir.getAbsolutePath());
//File gitDir = new File(repoDir, "HEAD");
//        if (!repoDir.exists()&&!gitDir.exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found");
//            return;
//        }
//
//        try (Repository repository = new FileRepositoryBuilder()
//                .setGitDir(repoDir)
//                .build()) {
//
//            UploadPack uploadPack = new UploadPack(repository);
//            uploadPack.setAdvertiseRefsHook(AdvertiseRefsHook.DEFAULT); // ✅ Use JGit’s default ref advertisement
//
//            resp.setContentType("application/x-git-upload-pack-advertisement");
//
//            try (ServletOutputStream out = resp.getOutputStream()) {
//                PrintWriter writer = new PrintWriter(out, true);
//                
//                // Advertise the service (Git expects this)
//                writer.print("001e# service=git-upload-pack\n");
//                writer.print("0000"); // Flush packet
//
//                // Send advertised refs to the client
//                uploadPack.upload(req.getInputStream(), out, System.err);
//            }
//
//        } catch (Exception e) {
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fetch failed: " + e.getMessage());
//        }
//    }
//

//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Extract repository path
//        String pathInfo = req.getPathInfo(); // Example: "/Rajkumar/project1.git/info/refs"
//        
//        // Get the actual repo path
//        String repoPath = pathInfo != null ? pathInfo.split("/info")[0] : ""; // This cuts off anything after "/info"
//        File repoDir = new File("/opt/all-repos" + repoPath);
//
//        if (!repoDir.exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found");
//            return;
//        }
//
//        try (Git git = Git.open(repoDir)) {
//            if (pathInfo.endsWith("/info/refs")) {
//                // Handles the refs request
//                UploadPack uploadPack = new UploadPack(git.getRepository());
////                uploadPack.setAdvertiseRefsHook(UploadPack.DEFAULT_ADVERTISE_REFS); // Advertise refs (branches/tags)
//                uploadPack.upload(req.getInputStream(), resp.getOutputStream(), System.err);
//            } else {
//                // Handle normal clone/pull requests
//                UploadPack uploadPack = new UploadPack(git.getRepository());
//                uploadPack.upload(req.getInputStream(), resp.getOutputStream(), System.err);
//            }
//        } catch (Exception e) {
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fetch failed: " + e.getMessage());
//        }
//    }

//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // Extract repository path from URL
//        String pathInfo = req.getPathInfo();  // Example: "/john/myrepo.git"
//       
//        pathInfo=pathInfo.substring(1);
//        System.out.println(pathInfo);
//        if (pathInfo == null || pathInfo.equals("/")) {
//            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Repository path required");
//            return;
//        }
//
//        // Construct the full repository path
//        File repoDir = new File("/opt/all-repos" + pathInfo);
//        
//        if (!repoDir.exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found");
//            return;
//        }
//
//        try (Git git = Git.open(repoDir)) {
//            UploadPack uploadPack = new UploadPack(git.getRepository());
//            uploadPack.upload(req.getInputStream(), resp.getOutputStream(), System.err);
//        } catch (Exception e) {
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fetch failed: " + e.getMessage());
//        }
//    }

//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // This handles 'git pull' and 'git clone' requests
//        File repoDir = new File(REPO_PATH);
//        if (!repoDir.exists()) {
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Repository not found");
//            return;
//        }
//
//        try (Git git = Git.open(repoDir)) {
//            UploadPack uploadPack = new UploadPack(git.getRepository());
//            uploadPack.upload(req.getInputStream(), resp.getOutputStream(), System.err);
//        } catch (Exception e) {
//            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fetch failed: " + e.getMessage());
//        }
//    }
}