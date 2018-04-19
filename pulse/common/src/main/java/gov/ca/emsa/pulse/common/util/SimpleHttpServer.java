package gov.ca.emsa.pulse.common.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class SimpleHttpServer {

    private static ServerSocket server;

    public static void start(final Integer port) throws IOException {
        stop();

        server = new ServerSocket(port);
        System.out.println("Listening for connection on port 8080 ....");
        while (true) {
            try (Socket socket = server.accept()) {
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            }
        }
    }

    public static void stop() {
        if (server != null && !server.isClosed()) {
            try {
                server.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void spawnServer(final Integer port) {
        new Thread() {
            @Override
            public void run() {
                System.out.println("Does it work?");

                try {
                    SimpleHttpServer.start(port);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                System.out.println("Nope, it doesnt...again.");
            }
        }.start();
    }

}
