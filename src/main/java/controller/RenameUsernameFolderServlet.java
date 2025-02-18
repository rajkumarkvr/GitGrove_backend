package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.json.JSONObject;


import utils.JSONHandler;
import utils.PermissionManager;

public class RenameUsernameFolderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String REPO_PATH = "/opt/repo"; // Base path for repositories

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonData = JSONHandler.parse(req.getReader());
        System.out.println("called ---rename username folder");
        // Extract data from JSON request
        String oldUsername = jsonData.getString("oldUsername").toLowerCase();
        String newUsername = jsonData.getString("newUsername").toLowerCase();

        // Validate input
        if (oldUsername.isEmpty() || newUsername.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Both old and new usernames are required.");
            return;
        }

        // Paths
        File oldUserFolder = new File(REPO_PATH, oldUsername);
        File newUserFolder = new File(REPO_PATH, newUsername);

        // Check if the old user folder exists
        if (!oldUserFolder.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User folder not found.");
            return;
        }

        // Check if the new username already exists
        if (newUserFolder.exists()) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "New username already exists.");
            return;
        }

        try {
            // Rename the user folder
            Files.move(oldUserFolder.toPath(), newUserFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
            PermissionManager.setOwner(newUserFolder, "git:git");


            resp.getWriter().write("Username updated successfully. All repositories moved to " + newUsername);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating username: " + e.getMessage());
        }
    }
}
