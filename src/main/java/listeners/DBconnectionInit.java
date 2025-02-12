package listeners;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import services.DBconnection;
import utils.PermissionManager;


@WebListener
public class DBconnectionInit implements ServletContextListener, HttpSessionListener, ServletRequestListener {

    /**
     * Default constructor.
     */
    public DBconnectionInit() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    @Override
	public void sessionCreated(HttpSessionEvent se)  {
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestListener#requestDestroyed(ServletRequestEvent)
     */
    @Override
	public void requestDestroyed(ServletRequestEvent sre)  {
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletRequestListener#requestInitialized(ServletRequestEvent)
     */
    @Override
	public void requestInitialized(ServletRequestEvent sre)  {
         // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    @Override
	public void sessionDestroyed(HttpSessionEvent se)  {
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @Override
	public void contextDestroyed(ServletContextEvent sce)  {
    	 Enumeration<Driver> drivers = DriverManager.getDrivers();
         while (drivers.hasMoreElements()) {
             Driver driver = drivers.nextElement();
             try {
                 // Only deregister drivers that were registered by the web application class loader
                 if (driver.getClass().getClassLoader() == this.getClass().getClassLoader()) {
                     DriverManager.deregisterDriver(driver);
//                     System.out.println("Deregistered JDBC driver: " + driver);
                 }
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }

         AbandonedConnectionCleanupThread.checkedShutdown();
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
	public void contextInitialized(ServletContextEvent sce)  {
    	String pass = sce.getServletContext().getInitParameter("USER_PASSWORD");

    PermissionManager.setPassword(pass);//Setting password
    	String conString = sce.getServletContext().getInitParameter("DB_URL");
    	String username = sce.getServletContext().getInitParameter("DB_USERNAME");
    	String password = sce.getServletContext().getInitParameter("DB_PASSWORD");
    	DBconnection.config(conString, username, password);
    	try {
		Connection con=	DBconnection.getConnection();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
		System.out.println(e.getMessage());
		}


    }

}
