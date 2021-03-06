package org.clang.origin;

import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;

@Log4j2
public class BaseSupport {
	static String server = "192.168.204.132";
	static int port = 6379;
	static String db = "";
	static String PONG = "PONG";
	public static Jedis jd=null;
	static {
		jd = new Jedis(server, port);
		log.info("connect {}:{}",server,port);
		if (PONG.equals(jd.ping()))
			log.info("receive PONG");
		else {
			log.error("wrong...");
			jd.close();
			System.exit(1);
		}
	}
}
