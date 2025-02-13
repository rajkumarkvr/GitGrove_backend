package filters;

import java.io.IOException;
import java.security.Key;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtFilter extends HttpFilter implements Filter {
    private static final long serialVersionUID = 1L;
    public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    

	public JwtFilter() {
        super();
        // TODO Auto-generated constructor stub
    }

	public void destroy() {
		// TODO Auto-generated method stub
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		String jwtToken = httpRequest.getHeader("Authorization");
		
		if(jwtToken == null || !jwtToken.startsWith("Bearer")) {
			 httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	         httpResponse.getWriter().write("{\"error\": \"Missing or invalid token\"}");
	         return;
		}
		
		try {
//			Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken).getBody();
			 chain.doFilter(request, response);
			 
		}catch (Exception e) {
			
		}
		
	}

	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
