package net.geertvos.theater.demo;

public class LineResultMessage {

	private String id;
	private int wordCount;

	public LineResultMessage() {
		
	}
	
	public LineResultMessage(String id, int wordCount) {
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
