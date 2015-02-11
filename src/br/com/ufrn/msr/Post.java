package br.com.ufrn.msr;

import java.sql.Date;

public class Post {
	
	private String title;
	private String body;
	private String tags;
	private Date creationDate;
	private int viewCount;
	private int answerCount;
	private int acceptedAnswer;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public int getAcceptedAnswer() {
		return acceptedAnswer;
	}

	public void setAcceptedAnswer(int acceptedAnswer) {
		this.acceptedAnswer = acceptedAnswer;
	}

}
