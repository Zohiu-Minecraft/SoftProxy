package de.zohiu.softproxy.proxy;

import de.zohiu.softproxy.Softproxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * adapted from <a href="https://github.com/oksuz/tcp-proxy/tree/master">oksuz/tcp-proxy</a>
 */
public class Proxy implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private final String remoteIp;
    private final int remotePort;
    private final int port;

    public Proxy(String remoteIp, int remotePort, int port) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.port = port;
    }

    private void startThread(Connection connection) {
        Thread t = new Thread(connection);
        t.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(1000);

            LOGGER.info("SoftProxy started with target " + remoteIp + ":" + remotePort);
            while (Softproxy.server != null) {
                try {
                    Socket socket = serverSocket.accept();
                    startThread(new Connection(socket, remoteIp, remotePort));
                } catch (IOException e) {
                    if (!(e instanceof java.net.SocketTimeoutException)) {
                        throw new IOException(e);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
