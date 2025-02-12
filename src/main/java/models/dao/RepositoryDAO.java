package models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import enums.Visibility;
import models.Repository;
import services.DBconnection;

public class RepositoryDAO {

	static RepositoryDAO repositoryDAO = null;

	private RepositoryDAO() {

	}

	public static RepositoryDAO getInstance() {
		if(repositoryDAO == null) {
			repositoryDAO = new RepositoryDAO();
		}
		return repositoryDAO;
	}

	public boolean addRepository(String name, String description, String visibility, int owner_id){
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("insert into repositories(name,visibility,owner_id,description) values(?,?,?,?)");
			stmt.setString(1, name);
			stmt.setString(2, Visibility.valueOf(visibility).toString());
			stmt.setInt(3, owner_id);
			stmt.setString(4, description);
			int affected = stmt.executeUpdate();
			return affected>0;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

//	public ArrayList<Repository> repositoriesByOwnerId(int ownerId){
//		ArrayList<Repository> repositorie = new ArrayList<Repository>();
//		try {
//			Connection connection = DBconnection.getConnection();
//			PreparedStatement stmt = connection.prepareStatement("select * from repositories where owner_id ");
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}

	public Repository getRepository(int id) {
		Repository repository = null;
		try {
			Connection connection = DBconnection.getConnection();
			PreparedStatement stmt = connection.prepareStatement("select * from repositories where id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
//				repository = new Repository(rs.getInt(1),rs.getString(2),rs.getString(3),)
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return repository;
	}

}
