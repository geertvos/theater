package net.geertvos.theater.core.partitioning;

import net.geertvos.theater.api.partitioning.Partition;

public interface LocalPartitionFactory {

	LocalPartition createPartition(int id);
	
}
