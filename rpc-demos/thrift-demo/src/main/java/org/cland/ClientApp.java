package org.cland;

import lombok.SneakyThrows;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.cland.rpc.Sayer;

public class ClientApp {

    @SneakyThrows
    public static void main(String[] args){
        final TTransport transport = new TSocket("localhost", 9090);
        transport.open();
        TProtocol protocol = new TBinaryProtocol(transport);
        Sayer.Client client = new Sayer.Client(protocol);
        System.out.println("ready go!");
        for(int x=0;x<100000;x++){
            String msg=x+":"+"哈哈，兴奋";
            client.say(msg);
            Thread.sleep(1000);
        }
        transport.close();
    }
}
