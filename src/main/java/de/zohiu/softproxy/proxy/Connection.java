package de.zohiu.softproxy.proxy;

import java.io.IOException;
import java.net.Socket;

/**
 * adapted from <a href="https://github.com/oksuz/tcp-proxy/tree/master">oksuz/tcp-proxy</a>
 */
public class Connection implements Runnable {
    private final Socket clientsocket;
    private final String remoteIp;
    private final int remotePort;
    private Socket serverConnection = null;

    public Connection(Socket clientsocket, String remoteIp, int remotePort) {
        this.clientsocket = clientsocket;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
    }

    @Override
    public void run() {
        try { serverConnection = new Socket(remoteIp, remotePort); } catch (IOException e) { return; }
        new Thread(new SingleConnection(clientsocket, serverConnection)).start();
        new Thread(new SingleConnection(serverConnection, clientsocket)).start();
        new Thread(() -> {
            while (true) {
                if (clientsocket.isClosed()) {
                    closeServerConnection();
                    break;
                }

                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void closeServerConnection() {
        if (serverConnection != null && !serverConnection.isClosed()) {
            try { serverConnection.close(); } catch (IOException ignored) { }
        }
    }
}
