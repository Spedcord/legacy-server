package xyz.spedcord.server;

import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        SpedcordServer spedcordServer = new SpedcordServer();
        spedcordServer.start();
    }

}
