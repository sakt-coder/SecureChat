package test;

import MyTor.TorServer;
import MyTor.TorSocket;

public class TestServer {
    public static void main(String[] args)throws Exception {
        TorServer server = new TorServer(3100);
        TorSocket socket = server.accept();
        Object obj = socket.readObject();
        System.out.println(obj);
        socket.writeObject("Hello to you too");
    }
}
