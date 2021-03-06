package org.cland.origin.sence;

import lombok.extern.log4j.Log4j2;
import org.cland.origin.BaseSupport;

import java.util.concurrent.TimeUnit;

@Log4j2
public class Publisher extends BaseSupport {

	public static void main(String[] args) throws InterruptedException {
		while (true) {
			String channel="a";
			String msg="a";
			log.info("publish chanel[{}],msg: {}",channel,msg);
			jd.publish(channel,msg);
			channel="b";
			msg="b";
			log.info("publish chanel[{}],msg: {}",channel,msg);
			jd.publish(channel,msg);
			channel="hello.ni.haha";
			msg="hello.ni.haha";
			log.info("publish chanel[{}],msg: {}",channel,msg);
			jd.publish("hello.ni.haha", "hello.ni.haha");
			jd.publish(channel,msg);
			channel="hello.wo";
			msg="hello.wo";
			log.info("publish chanel[{}],msg: {}",channel,msg);
			jd.publish(channel,msg);
			TimeUnit.SECONDS.sleep(1);
		}
	}

}
