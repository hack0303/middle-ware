package org.cland;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

@Slf4j
public class Producer {
	
	public static void main(String[] args) throws UnknownHostException {
    Properties props=new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BaseSupport.host+":"+BaseSupport.port);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, InetAddress.getLocalHost().getHostName());
    //props.put("acks","all");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    KafkaProducer<String,String> producer=new KafkaProducer<String,String>(props);
    ProducerRecord<String,String> record=new ProducerRecord<String, String>("test", "a", "a-v");
    producer.beginTransaction();
    producer.send(record, new Callback() {
		
		public void onCompletion(RecordMetadata metadata, Exception exception) {
			if(exception!=null) {
			   log.info("",exception);
			}
			
		}
	});
    producer.commitTransaction();
    producer.close();
	}

}
