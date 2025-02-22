package controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import models.dao.SSH_KeyDAO;
import models.dao.UserDAO;


public class GetSSHKeys extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");

		int userid = UserDAO.getInstance().getUserId(username);
	 ArrayList<JSONObject> keys =	SSH_KeyDAO.getInstance().getsshKeysByUserID(userid);
	 if(keys==null||keys.size()==0) {
		 
		 JSONObject res = new JSONObject();
		 response.setStatus(HttpServletResponse.SC_OK);
		 res.put("message", "No ssh keys found.");
		 response.getWriter().write(res.toString());
		 return;
	 }
	
	 JSONObject output = new JSONObject();
	JSONArray outArr = new JSONArray(keys);
	 output.put("keys", outArr);
	 response.setStatus(HttpServletResponse.SC_OK);
	 response.getWriter().write(output.toString());
	 return;
	}

	

}
