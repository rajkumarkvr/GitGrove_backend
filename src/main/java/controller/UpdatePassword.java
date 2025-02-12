package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.UserDAO;

/**
 * Servlet implementation class UpdatePassword
 */
//@WebServlet("/UpdatePassword")
public class UpdatePassword extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public UpdatePassword() {
        super();
        // TODO Auto-generated constructor stub
    }

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String email = request.getParameter("email");
		String password = request.getParameter("username");
		boolean isUpdated = UserDAO.getInstance().updatePassword(email, password);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject jsonResponse = new JSONObject();
		if(isUpdated) {
			jsonResponse.put("responseCode", "200");
		}
		else {
			jsonResponse.put("responseCode", "200");
		}

	    response.getWriter().write(jsonResponse.toString());

	}

}
