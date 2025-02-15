package controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.SessionDAO;
import models.dao.UserDAO;


public class GetSessionsDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userdata=request.getParameter("userid");

		if(userdata==null||userdata.trim().isEmpty()) {
			response.setStatus(400);
			JSONObject error = new JSONObject();
			error.put("error", "Invalid user identity");
			response.getWriter().write(error.toString());
			return;
		}

	 int userid =UserDAO.getInstance().getUserByUserName(userdata).getId();
	 
	 System.out.println("userid"+userid);
	 ArrayList<JSONObject>	sessions = SessionDAO.getInstance().getActiveSessions(userid);
	 System.out.println(sessions);
	 JSONObject sessionjson =new JSONObject();
	 sessionjson.put("sessions", sessions);
	 response.setStatus(200);
	
		response.getWriter().write(sessionjson.toString());
		return;
	}

	

}
