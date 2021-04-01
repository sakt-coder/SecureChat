package test;

import MyTor.TorObjectStream;

public class TestClient {
    public static void main(String[] args)throws Exception {
        TorObjectStream tos = new TorObjectStream("localhost", 3100);
        tos.writeObject("Hello");
        System.out.println(tos.readObject());
    }
}
