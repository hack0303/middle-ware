package org.cland;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    static final String key="hello";

    public static void main(String[] args) throws IOException {
        MemcachedClient client=new MemcachedClient(new InetSocketAddress("node01",11211));
        System.out.println(client.set(key,10,"hello-v").getStatus().isSuccess());
        client.flush().getStatus().isSuccess();
    }
}
