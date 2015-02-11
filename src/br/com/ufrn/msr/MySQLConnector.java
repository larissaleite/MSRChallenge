package br.com.ufrn.msr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnector {
	
	private static Connection connection;

	public static Connection getConnection() {
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "stackoverflow";
		String driver = "com.mysql.jdbc.Driver"; 
		String userName = "root"; 
		String password = "root"; 
		
		try { 
			Class.forName(driver).newInstance();
			if (connection == null)
				connection = DriverManager.getConnection(url+dbName,userName,password); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return connection;
	}
	
	public static void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
