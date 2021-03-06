package org.cland.origin.hash;

import lombok.extern.log4j.Log4j2;
import org.cland.origin.BaseSupport;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class HashTest extends BaseSupport {

    public static void main(String[] args) {
        runCase01();
        runCase02();
    }


    /**
     *
     * */
    public static void runCase01() {
        jd.select(4);
        Transaction trs = jd.multi();
        Response<Boolean> rsp = trs.hexists("hm002", "haha");
        trs.hset("hm002", "hahaa", "hahadezhi3");
        List<Object> objs = trs.exec();
        objs.forEach(e -> {
            log.info(e);
        });

    }

    public static void runCase02() {
        jd.select(4);
        Map<String, String> map = new HashMap<>();
        map.put("a", "100aa");
        map.put("b", "100bb");
        log.info(jd.hmset("hm001", map));
        jd.hmget("hm001", "a", "b").forEach(item -> {
            log.info("{}", item);
        });
        log.info(jd.hsetnx("hm001", "a", "aaa"));
    }


}
