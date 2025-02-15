package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.SessionDAO;


public class LogoutSession extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String token = request.getParameter("sessionid");
		if(token==null||token.trim().isEmpty()) {
			response.setStatus(401);
			JSONObject error = new JSONObject();
			error.put("message", "Unauthorized access");
			response.getWriter().write(error.toString());
			return;
		}
		SessionDAO.getInstance().clearSession(token);
		response.setStatus(200);
		JSONObject success = new JSONObject();
		success.put("message", "session cleared properly");
		response.getWriter().write(success.toString());
	}

}
