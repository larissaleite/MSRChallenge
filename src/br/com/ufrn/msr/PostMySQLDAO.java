package br.com.ufrn.msr;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostMySQLDAO {

	public static void savePost(Post post, int id) {
		PreparedStatement statement;
		try {
			statement = MySQLConnector
					.getConnection()
					.prepareStatement(
							"INSERT into Post (id, title, body, tags, creation_date, view_count, answer_count, accepted_answer) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
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

	public static void savePostNE(Post post, int id) {
		PreparedStatement statement;
		try {
			statement = MySQLConnector
					.getConnection()
					.prepareStatement(
							"INSERT into PostNE (id, title, body, tags, creation_date, view_count, answer_count, accepted_answer) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
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

	public static String getBodyFromPost(int id) {
		String query = "SELECT body FROM Post WHERE id = " + id;
		String body = null;
		try {
			PreparedStatement statement = MySQLConnector.getConnection()
					.prepareStatement(query);
			ResultSet rs;
			rs = statement.executeQuery(query);
			if (rs != null && rs.next()) {
				body = rs.getString("body");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return body;
	}

	public static String getBodyFromPostNe(int id) {
		String query = "SELECT body FROM PostNE WHERE id = " + id;
		String body = null;
		try {
			PreparedStatement statement = MySQLConnector.getConnection()
					.prepareStatement(query);
			ResultSet rs;
			rs = statement.executeQuery(query);
			if (rs != null && rs.next()) {
				body = rs.getString("body");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return body;
	}

	public static List<String> getAllTags() {
		String query = "SELECT tags FROM Post;";
		List<String> allTags = new ArrayList<String>();
		try {
			PreparedStatement statement = MySQLConnector.getConnection().prepareStatement(query);
	     	ResultSet rs;
			rs = statement.executeQuery(query);
			while (rs.next()) {
				String tags = rs.getString("tags");
				allTags.add(tags);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	     
	    return allTags;
	}
	
	public static List<Integer> getAllAcceptedAnswersId() {
		String query = "SELECT accepted_answer FROM Post;";
		List<Integer> allAnswersId = new ArrayList<Integer>();
		try {
			PreparedStatement statement = MySQLConnector.getConnection().prepareStatement(query);
	     	ResultSet rs;
			rs = statement.executeQuery(query);
			while (rs.next()) {
				int acc = rs.getInt("accepted_answer");
				allAnswersId.add(acc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	     
	    return allAnswersId;
	}
}
