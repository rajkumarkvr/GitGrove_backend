package filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:5173",
            "http://172.17.23.190:3000",
            "http://172.17.23.190:8080",
            "http://192.168.43.216:8080",
            "http://192.168.43.216:3000",
            "http://gitgrove.com:3000",
            "http://gitgrove.com:8080",
            "http://gitgrove.com:3000"
            
        );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest)request;
        System.out.println("request");
        String origin = req.getHeader("Origin");
        System.out.println(origin);
        if(ALLOWED_ORIGINS.contains(origin)) {
        	   res.setHeader("Access-Control-Allow-Origin", origin);
        }else {
        	System.out.println("not allowed");
        }

        res.setContentType("application/json");
         

        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        res.setHeader("Access-Control-Allow-Credentials","true");

        if("OPTIONS".equalsIgnoreCase(req.getMethod()))
        {
        	res.setStatus(HttpServletResponse.SC_OK);
        	return;
        }
        System.out.println("Welcome");
        chain.doFilter(request, response);
    }


    @Override
    public void destroy() {}
}

