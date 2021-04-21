package org.cland.basic;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Consumer {

    public static void main(String[] args){
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5555");
            while(true){
                System.out.println("recv: "+socket.recvStr(0));
                socket.send("hello",0);
            }
        }
    }
}
