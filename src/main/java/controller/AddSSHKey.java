package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.SSH_KeyDAO;
import models.dao.UserDAO;
import utils.JSONHandler;


public class AddSSHKey extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public AddSSHKey() {
        super();
    }

    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		JSONObject jsonObject = JSONHandler.parse(request.getReader());
		
		String username = jsonObject.optString("username");
		String publicKey = jsonObject.optString("publicKey");
		String description = jsonObject.optString("description");
		
		if(username == null || publicKey == null || description == null) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid input\"}");
			return;
		}
		
		int userId = UserDAO.getInstance().getUserId(username);
		
		if(userId<0) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Invalid user\"}");
			return;
		}
		
		if(SSH_KeyDAO.getInstance().isKeyExists(publicKey)) {
			response.setStatus(400);
			response.getWriter().write("{\"message\" : \"Existing key\"}");
			return;
		}	
		
		String entry = SSH_KeyDAO.getInstance().generateEntry(username, publicKey);
		
		SSH_KeyDAO.getInstance().appendToAuthorizedKeys(entry);
		
		SSH_KeyDAO.getInstance().insertKey(userId, publicKey, description);
		response.setStatus(200);
		response.getWriter().write("{\"message\" : \"Public key inserted\"}");
	}

}
