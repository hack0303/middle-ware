package org.cland.origin;

import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.JedisPubSub;

@Log4j2
public class Subcriber extends BaseSupport {

	public static void main(String[] args) throws InterruptedException {
		JedisPubSub jps = new JedisPubSub() {
			public void onMessage(String channel, String message) {
				log.info("received from {},{}", channel, message);
			}

			public void onPMessage(String pattern, String channel, String message) {
				log.info("pattern [{}],received from {},{}", pattern, channel, message);
			}
		};
		new Thread(() -> {
			jd.subscribe(jps, "a", "b");
		}).start();
		/**
		new Thread(() -> {
			jd.psubscribe(jps, "hello.*");
		}).start();
		*/
		synchronized(BaseSupport.class) {
		BaseSupport.class.wait();
		}
	}

}
