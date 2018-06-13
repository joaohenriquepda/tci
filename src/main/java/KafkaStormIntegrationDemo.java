
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import bolt.LoggerBolt;

/**
 * @author Amit Kumar
 */
public class KafkaStormIntegrationDemo {
	
	private static final Logger LOG = Logger.getLogger(KafkaStormIntegrationDemo.class);

	public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		// Log program usages and exit if there are less than 4 command line arguments
//		if(args.length < 4) {
//			LOG.fatal("Incorrect number of arguments. Required arguments: <zk-hosts> <kafka-topic> <zk-path> <clientid>");
//			System.exit(1);
//		}
			
		// Build Spout configuration using input command line parameters
		final BrokerHosts zkrHosts = new ZkHosts("localhost:2181");
		final String kafkaTopic = "storm-test-topic";
		final String zkRoot = "/brokers";
		final String clientId = "storm-consumer";
		final SpoutConfig kafkaConf = new SpoutConfig(zkrHosts, kafkaTopic, zkRoot, clientId);
		kafkaConf.scheme = new SchemeAsMultiScheme(new StringScheme());

		// Build topology to consume message from kafka and print them on console
		final TopologyBuilder topologyBuilder = new TopologyBuilder();
		// Create KafkaSpout instance using Kafka configuration and add it to topology
		topologyBuilder.setSpout("kafka-spout", new KafkaSpout(kafkaConf), 1);
		//Route the output of Kafka Spout to Logger bolt to log messages consumed from Kafka
		topologyBuilder.setBolt("print-messages", new LoggerBolt()).globalGrouping("kafka-spout");
		
		// Submit topology to local cluster i.e. embedded storm instance in eclipse
		final LocalCluster localCluster = new LocalCluster();
		localCluster.submitTopology("kafka-topology", new HashMap<Object, Object>(), topologyBuilder.createTopology());
	}
}