package core;

import data.FileManager;
import model.SharedFile;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final FileManager fileManager;

    public ClientHandler(Socket socket, FileManager fileManager) {
        this.socket = socket;
        this.fileManager = fileManager;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                OutputStream dataOut = socket.getOutputStream()) {
            String request = in.readLine();
            if (request == null)
                return;

            System.out.println("Received request: " + request);

            if (request.equals(SharedConstants.CMD_HELLO)) {
                out.println(SharedConstants.RESP_OK);
            } else if (request.equals(SharedConstants.CMD_LIST)) {
                handleListFiles(out);
            } else if (request.startsWith(SharedConstants.CMD_GET)) {
                String filename = request.substring(SharedConstants.CMD_GET.length()).trim();
                handleGetFile(filename, out, dataOut);
            } else {
                out.println(SharedConstants.RESP_ERROR + " Unknown command");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void handleListFiles(PrintWriter out) {
        List<SharedFile> files = fileManager.getLocalFiles();
        out.println(SharedConstants.RESP_OK);
        out.println(files.size());
        for (SharedFile f : files) {
            // Send name:size
            out.println(f.getName() + ":" + f.getSize());
        }
    }

    private void handleGetFile(String filename, PrintWriter out, OutputStream dataOut) throws IOException {
        SharedFile file = fileManager.getLocalFile(filename);
        if (file == null) {
            out.println(SharedConstants.RESP_ERROR + " File not found");
            return;
        }

        out.println(SharedConstants.RESP_OK + " " + file.getSize());
        // Flush the writer before sending raw bytes
        out.flush();

        File diskFile = new File(file.getLocalPath());
        try (FileInputStream fis = new FileInputStream(diskFile)) {
            byte[] buffer = new byte[SharedConstants.FILE_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
        }
    }
}
