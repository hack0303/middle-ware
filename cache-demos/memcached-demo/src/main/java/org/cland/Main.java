package org.cland;

import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.checkState;

public class Main {

    static final String key="wish";

    public static void main(String[] args) throws IOException {
        MemcachedClient client=new MemcachedClient(new InetSocketAddress("node01",11211));
        checkState(client.set(key,0,"你,下午路上小心.我去跑步去了...").getStatus().isSuccess(),"设置值失败");
        System.out.printf("key:%s,val:%s%n",key,client.get(key));
    }
}
