package ui;

import core.PeerClient;
import core.PeerServer;
import core.SharedConstants;
import data.FileManager;
import data.PeerManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {
    private PeerManager peerManager;
    private FileManager fileManager;
    private PeerServer peerServer;
    private PeerClient peerClient;

    public MainFrame() {
        super("P2P Student Resource Sharing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // 1. Startup Configuration (Port & Folder)
        JPanel configPanel = new JPanel(new GridLayout(3, 2));
        JTextField portField = new JTextField(String.valueOf(SharedConstants.DEFAULT_PORT));
        JTextField folderField = new JTextField("shared_files_" + SharedConstants.DEFAULT_PORT);

        configPanel.add(new JLabel("Local Port:"));
        configPanel.add(portField);
        configPanel.add(new JLabel("Shared Folder Path:"));
        configPanel.add(folderField);

        int result = JOptionPane.showConfirmDialog(null, configPanel,
                "P2P Configuration", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        int port = 6000;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Port. Using default.");
        }

        String folderPath = folderField.getText();

        // 2. Initialize Components
        peerManager = new PeerManager();
        fileManager = new FileManager(folderPath); // Creates folder if missing
        peerClient = new PeerClient();

        // 3. Start Server
        try {
            peerServer = new PeerServer(port, fileManager);
            new Thread(peerServer).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to start server: " + e.getMessage());
            System.exit(1);
        }

        // 4. Build UI
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Peers", new PeerPanel(peerManager, peerClient));
        tabbedPane.addTab("Files", new FilePanel(fileManager, peerClient));

        add(tabbedPane);

        // Status Bar
        JLabel statusLabel = new JLabel(
                " Listening on port: " + port + " | Shared Folder: " + new File(folderPath).getAbsolutePath());
        add(statusLabel, BorderLayout.SOUTH);

        // Cleanup on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (peerServer != null) {
                    peerServer.stop();
                }
            }
        });
    }
}
