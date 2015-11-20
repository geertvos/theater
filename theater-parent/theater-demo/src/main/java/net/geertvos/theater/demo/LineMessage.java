package net.geertvos.theater.demo;

public class LineMessage {

	private String line;
	private String id;
	
	public LineMessage() {
	}
	
	public LineMessage(String id, String line) {
		this.id = id;
		this.line = line;
	}
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

}
