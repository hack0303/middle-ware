package org.cland.example01;

import redis.clients.jedis.JedisPooled;

public class Case03 {

    public static void main(String[] args){
        JedisPooled jedis = new JedisPooled("redis://:123456@master:6379/2");
        jedis.set("clientName", "Jedis");
    }
}
