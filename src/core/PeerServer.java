package core;

import data.FileManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerServer implements Runnable {
    private final int port;
    private final FileManager fileManager;
    private final ExecutorService threadPool;
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public PeerServer(int port, FileManager fileManager) {
        this.port = port;
        this.fileManager = fileManager;
        this.threadPool = Executors.newCachedThreadPool(); // Or FixedThreadPool
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("PeerServer listening on port " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                    threadPool.execute(new ClientHandler(clientSocket, fileManager));
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (threadPool != null) {
                threadPool.shutdown();
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
