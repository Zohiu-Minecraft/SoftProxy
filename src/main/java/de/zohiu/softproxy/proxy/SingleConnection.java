package de.zohiu.softproxy.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * adapted from <a href="https://github.com/oksuz/tcp-proxy/tree/master">oksuz/tcp-proxy</a>
 */
public class SingleConnection implements Runnable {
    private final Socket in;
    private final Socket out;

    public SingleConnection(Socket in, Socket out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = in.getInputStream();
            OutputStream outputStream = out.getOutputStream();

            if (inputStream == null || outputStream == null) { return; }

            byte[] reply = new byte[4096];
            int bytesRead;
            while (-1 != (bytesRead = inputStream.read(reply))) {
                outputStream.write(reply, 0, bytesRead);
            }
        } catch (SocketException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { in.close(); } catch (IOException ignored) { }
        }
    }
}
