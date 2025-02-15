package controller.Authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import models.dao.SessionDAO;

/**
 * Servlet implementation class ClearSession
 */
public class ClearSession extends HttpServlet {

	private static final long serialVersionUID = 1L;

    public ClearSession() {
        super();
        // TODO Auto-generated constructor stub
    }


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String token = request.getParameter("expiredToken");
		System.out.println("calling");
		boolean isCleared =  SessionDAO.getInstance().clearSession(token);
		JSONObject jsonObject = new JSONObject();
		if(isCleared) {
			jsonObject.put("Is_Cleared", true);
			response.setStatus(200);
		}
		else {
			jsonObject.put("Is_Cleared", false);
			response.setStatus(400);
		}
		response.getWriter().write(jsonObject.toString());
	}

}
