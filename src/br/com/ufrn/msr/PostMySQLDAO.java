package br.com.ufrn.msr;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostMySQLDAO {
	
	public static void savePost(Post post, int id) {
		PreparedStatement statement;
		try {
			statement = MySQLConnector.getConnection().prepareStatement("INSERT into Post (id, title, body, tags, creation_date, view_count, answer_count, accepted_answer) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, id);
			statement.setString(2, post.getTitle());
			statement.setString(3, post.getBody());
			statement.setString(4, post.getTags());
			statement.setDate(5, post.getCreationDate());
			statement.setInt(6, post.getViewCount());
			statement.setInt(7, post.getAnswerCount());
			statement.setInt(8, post.getAcceptedAnswer());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

	}
	
	public static void saveTags(String tags, int id) {
		PreparedStatement statement;
		try {
			statement = MySQLConnector.getConnection().prepareStatement("UPDATE Post SET tags=? WHERE id=? ");
			statement.setString(1, tags);
			statement.setInt(2, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

	}


}
