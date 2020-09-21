package xyz.spedcord.server;

import java.io.IOException;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class Launcher {

    /**
     * Program entry method
     *
     * @param args Program arguments
     * @throws IOException when server start fails
     */
    public static void main(String[] args) throws IOException {
        SpedcordServer spedcordServer = new SpedcordServer();
        spedcordServer.start();
    }

}
