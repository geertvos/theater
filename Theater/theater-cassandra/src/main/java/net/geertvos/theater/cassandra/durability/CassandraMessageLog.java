package net.geertvos.theater.cassandra.durability;

import java.util.Arrays;
import java.util.List;

import me.prettyprint.cassandra.serializers.BytesArraySerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import net.geertvos.theater.api.durability.MessageLog;
import net.geertvos.theater.api.messaging.Message;

public class CassandraMessageLog implements MessageLog {

	public CassandraMessageLog(Keyspace keyspace) {
		Cluster cluster = HFactory.getOrCreateCluster("test-cluster", "localhost:9160");
		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition("MyKeyspace", "ColumnFamilyName", ComparatorType.BYTESTYPE);

		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition("MyKeyspace", ThriftKsDef.DEF_STRATEGY_CLASS, 3, Arrays.asList(cfDef));
		cluster.addKeyspace(newKeyspace, true);

		keyspace = HFactory.createKeyspace("MyKeyspace", cluster);
		BytesArraySerializer messageSerializer = BytesArraySerializer.get();
		ColumnFamilyTemplate<String, String> template = new ThriftColumnFamilyTemplate<String, String>(keyspace, "ColumnFamilyName", StringSerializer.get(),
				StringSerializer.get());
	}

	public void logMessage(Message message) {
		// TODO Auto-generated method stub

	}

	public void ackMessage(Message message) {
		// TODO Auto-generated method stub

	}

	public List<Message> getUnackedMessages() {
		// TODO Auto-generated method stub
		return null;
	}

}
