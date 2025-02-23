package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CookieTesting extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		System.out.println("called cookie testing");
		    response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
		    response.setHeader("Access-Control-Allow-Credentials", "true");
		    Cookie cookie = new Cookie("testcookie", "testvalue");
		    cookie.setPath("/");
		    cookie.setSecure(false);
		    cookie.setHttpOnly(false);
		    response.addCookie(cookie);
		    response.getWriter().write("Cookie set");
		    return;
		
	}

	

}
