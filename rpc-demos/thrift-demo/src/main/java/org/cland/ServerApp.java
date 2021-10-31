package org.cland;

import lombok.SneakyThrows;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.cland.rpc.Sayer;

import javax.swing.plaf.synth.SynthTextAreaUI;

public class ServerApp {

    public static Sayer.Processor processor;
    public static class SayerHandler implements Sayer.Iface{

        @Override
        public void say(String msg) throws TException {
            System.out.println("receive is "+msg);
        }
    }
    @SneakyThrows
    public static void main(String[] args){
        TServerTransport serverTransport = new TServerSocket(9090);
        processor= new Sayer.Processor<>(new SayerHandler());
        TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

        // Use this for a multithreaded server
        // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

        System.out.println("Starting the simple server...");
        server.serve();
    }
}
