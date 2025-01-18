package org.cland.example01;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;

public class Case02 {

    public static void main(String[] args){
        JedisPooled jedis = new JedisPooled("master", 6379,null,"123456");
        jedis.set("clientName", "Jedis");
    }
}
