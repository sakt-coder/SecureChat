package test;

import MyTor.Volunteer;

public class TestVolunteer {

    static final int CNT = 3;

    public static void main(String[] args)throws Exception {
        for(int port = 3000;port < 3000+CNT; port++)
            new Volunteer(port).start();
    }
}
