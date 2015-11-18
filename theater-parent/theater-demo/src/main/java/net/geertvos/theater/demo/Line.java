package net.geertvos.theater.demo;

import java.util.UUID;

import net.geertvos.theater.api.actors.ActorHandle;

public class Line {

	private String line;
	private String id;
	
	public Line() {
		
	}
	
	public Line(String id, String line) {
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
