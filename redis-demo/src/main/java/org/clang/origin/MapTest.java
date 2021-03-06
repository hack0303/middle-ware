package org.clang.origin;

import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.List;

@Log4j2
public class MapTest extends BaseSupport {

	public static void main(String[] args) {
     jd.select(4);
     Transaction trs=jd.multi();
     Response<Boolean> rsp=trs.hexists("hm002", "haha");
     trs.hset("hm002", "hahaa", "hahadezhi3");
     List<Object> objs=trs.exec();
     objs.forEach(e->{
    	 log.info(e);
     });
	}

}
