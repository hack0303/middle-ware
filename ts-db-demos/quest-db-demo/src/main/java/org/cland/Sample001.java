package org.cland;

import io.questdb.client.Sender;

import java.util.Random;

/**
 * 同步 influxdb protocol
 * */
public class Sample001 {

    static Random random=new Random();

    public static void main(String[] args) {
        try (Sender sender = Sender.builder().address("vm001:9009").build()) {
            for(int x=0;x<1000;x++) {
                sender.table("tx_table_20220815")
                        .symbol("a_customer", "alice")
                        .symbol("b_customer", "bob")
                        .symbol("currency", "CNY")
                        .doubleColumn("value", random.nextInt(10000)/10d)
                        .atNow();
            }
        }
    }
}
