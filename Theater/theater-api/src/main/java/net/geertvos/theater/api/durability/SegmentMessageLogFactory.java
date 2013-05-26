package net.geertvos.theater.api.durability;

public interface SegmentMessageLogFactory {

	MessageLog createLog(int segment);
	
}
