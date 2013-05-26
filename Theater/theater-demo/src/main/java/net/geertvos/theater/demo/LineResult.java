package net.geertvos.theater.demo;

import java.util.UUID;

public class LineResult {

	private String id;
	private int wordCount;

	public LineResult() {
		
	}
	
	public LineResult(String id, int wordCount) {
		this.id = id;
		this.wordCount = wordCount;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getWordCount() {
		return wordCount;
	}
	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}
	
	
}
