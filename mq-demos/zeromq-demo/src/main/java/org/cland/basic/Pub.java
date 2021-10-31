package org.cland.basic;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Pub {

    public static void main(String[] args){
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:5555");
            for(int x=0;x<1000;x++){
                final String msg="哈哈："+x;
                System.out.println("send msg is: "+msg);
                socket.send(msg,0);
                System.out.println(socket.recvStr(0));
                Thread.sleep(1000);
            }
            Pub.class.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
