package org.cland.example01;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Case01 {

    public static void main(String[] args){
        JedisPool pool = new JedisPool("master", 6379,null,"123456");
        try (Jedis jedis = pool.getResource()) {
            jedis.set("clientName", "Jedis");
        }
    }
}
