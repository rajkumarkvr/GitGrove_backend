package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.SSH_KeyDAO;

public class DeleteSshKey extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			String idStr = request.getParameter("id");
			String username = request.getParameter("username");
			if(idStr==null||idStr.isEmpty()) {
				response.setStatus(400);
				JSONObject out = new JSONObject();
				out.put("message", "Invalid ssh id input.");
				
				response.getWriter().write(out.toString());
				return;
			}
			int id = Integer.parseInt(idStr);
			
			String token = SSH_KeyDAO.getInstance().getsshKeyByrowID(id);
			String format =SSH_KeyDAO.getInstance().generateEntry(username, token);
			SSH_KeyDAO.getInstance().deleteKeyFromFile(format);
			
			if(SSH_KeyDAO.getInstance().deleteSshKey(id)) {
				response.setStatus(200);
				JSONObject out = new JSONObject();
				out.put("message", "Key deleted successfully.");
				
				response.getWriter().write(out.toString());
				return;
			}else {
				response.setStatus(500);
				JSONObject out = new JSONObject();
				out.put("message", "Error to delete ssh key");
				
				response.getWriter().write(out.toString());
				return;
			}
	}

	

}
