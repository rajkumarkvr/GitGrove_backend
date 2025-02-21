package controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import models.dao.RepositoryDAO;
import models.dao.UserDAO;

public class GetCollaborators extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String reponame =request.getParameter("reponame");
		String ownername = request.getParameter("ownername");
		
		if(ownername == null|| reponame == null) {
	
			response.setStatus(400);
            response.getWriter().write("{\"error\": \"ownername or reponame missing\"}");
            return;
		}
		
		int ownerid =UserDAO.getInstance().getUserId(ownername);
		int repoid = RepositoryDAO.getInstance().getRepositoryId(reponame,ownerid);
		ArrayList<JSONObject> collaborators = RepositoryDAO.getInstance().getCollaborators(repoid, ownerid);
		if(collaborators==null) {
			response.setStatus(203);
            response.getWriter().write("{\"message\": \"No collaborators found.\"}");
			return ;
		}
		
		JSONArray userArray = new JSONArray(collaborators);
		
		JSONObject usersOutput = new JSONObject();
		usersOutput.put("users",userArray);
		response.setStatus(200);
		response.getWriter().write(usersOutput.toString());
	}



}
