package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.dao.SessionDAO;
import utils.JwtUtil;

public class JwtFilter extends HttpFilter implements Filter {
    private static final long serialVersionUID = 1L;
    private static final String COOKIE_KEY = "gitgrove_";
    

	public JwtFilter() {
        super();
        
    }

	public void destroy() {
		
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String jwtToken = httpRequest.getHeader("Authorization");
		System.out.println(jwtToken);
		if(jwtToken == null || !jwtToken.startsWith("Bearer")) {
			 httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	         httpResponse.getWriter().write("{\"error\": \"Missing or invalid token\"}");
	         return;
		}
		
		jwtToken = jwtToken.substring(7);
		
		System.out.println("jwt"+jwtToken);
		
		try {
			String token =  JwtUtil.getInstance().validateAndExtendToken(jwtToken);
			
			if(token!=jwtToken) {
//				Cookie cookie = Cookie
			}
			
//			String username = JwtUtil.getInstance().getusername(token);
//			Cookie cookie = new Cookie(COOKIE_KEY + username, token);
//			httpResponse.addCookie(cookie);
			
		}catch (Exception e) {
		
			httpResponse.setStatus(400);			
			httpResponse.getWriter().write("{\"error\": \"Token expired\"}");
			return;
		}
		
		chain.doFilter(request,response);
		
	}

	public void init(FilterConfig fConfig) throws ServletException {
	
	}
}
