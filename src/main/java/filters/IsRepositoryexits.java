package filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Servlet Filter implementation class IsRepositoryexits
 */
public class IsRepositoryexits extends HttpFilter implements Filter {
       
    private static final long serialVersionUID = 1L;
    private static final String REPO_PATH = "/opt/repo";

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		StringBuilder sb=new StringBuilder();
		BufferedReader read=request.getReader();
		String line;
		while((line=read.readLine())!=null) {
			sb.append(line);
		}
		
		JSONObject jsonData=new JSONObject(sb.toString());
    	
        String username = jsonData.getString("username"); 
        String repoName = jsonData.getString("repoName");
        
        if (username == null || username.trim().isEmpty() || repoName == null || repoName.trim().isEmpty()) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Username and repository name are required.");
            return;
        }
        // Create user folder
        File userFolder = new File(REPO_PATH, username);
        
        if (userFolder.exists()) {
        	File repoDir = new File(userFolder, repoName + ".git");
        	if(!repoDir.exists()) {
        		((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Repository name already exists");
                return;
        	}
        }
  
		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
		
	}

}
