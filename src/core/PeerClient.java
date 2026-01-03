package core;

import model.SharedFile;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PeerClient {

    public boolean connectAndHello(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), SharedConstants.CONNECT_TIMEOUT);
            socket.setSoTimeout(SharedConstants.READ_TIMEOUT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(SharedConstants.CMD_HELLO);
            String response = in.readLine();
            return SharedConstants.RESP_OK.equals(response);
        } catch (IOException e) {
            System.err.println("Connection failed to " + host + ":" + port + " - " + e.getMessage());
            return false;
        }
    }

    public List<SharedFile> fetchFileList(String host, int port) {
        List<SharedFile> files = new ArrayList<>();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), SharedConstants.CONNECT_TIMEOUT);
            socket.setSoTimeout(SharedConstants.READ_TIMEOUT);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(SharedConstants.CMD_LIST);
            String response = in.readLine(); // OK
            if (!SharedConstants.RESP_OK.equals(response)) {
                return files;
            }

            String countStr = in.readLine();
            int count = Integer.parseInt(countStr);

            for (int i = 0; i < count; i++) {
                String line = in.readLine();
                if (line != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        files.add(new SharedFile(parts[0], Long.parseLong(parts[1])));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public boolean downloadFile(String host, int port, String fileName, File destination) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), SharedConstants.CONNECT_TIMEOUT);
            // Longer timeout for download? Or handle it with loop checks.
            socket.setSoTimeout(SharedConstants.READ_TIMEOUT * 2);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            InputStream dataIn = socket.getInputStream();

            out.println(SharedConstants.CMD_GET + " " + fileName);

            // We need to read the initial text response line carefully b/c data follows
            // immediately
            // But BufferedReader buffers! Check ClientHandler implementation.
            // ClientHandler sends: OUT.println(OK + size); OUT.flush(); Then raw bytes.
            // If we use BufferedReader to read the line, it might eat into the byte stream.
            // Solution: Read the status line byte-by-byte or assume standard encoding until
            // newline.
            // Or use a strict protocol separator.

            // To be safe with mixed text/binary, let's read the first line manually from
            // InputStream
            String statusLine = readLineFromStream(dataIn);

            if (statusLine == null || !statusLine.startsWith(SharedConstants.RESP_OK)) {
                return false;
            }

            long fileSize = Long.parseLong(statusLine.split(" ")[1]);

            try (FileOutputStream fos = new FileOutputStream(destination)) {
                byte[] buffer = new byte[SharedConstants.FILE_BUFFER_SIZE];
                int bytesRead;
                long totalRead = 0;
                while (totalRead < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Download failed
        }
    }

    // Helper to read a line from InputStream without buffering too much (like
    // BufferedReader would)
    private String readLineFromStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n')
                break;
            if (c != '\r')
                sb.append((char) c);
        }
        if (sb.length() == 0 && c == -1)
            return null;
        return sb.toString();
    }
}
