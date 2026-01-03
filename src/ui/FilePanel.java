package ui;

import core.PeerClient;
import data.FileManager;
import model.SharedFile;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class FilePanel extends JPanel {
    private final FileManager fileManager;
    private final PeerClient peerClient;
    private JTable localTable;
    private JTable remoteTable;
    private DefaultTableModel localModel;
    private DefaultTableModel remoteModel;
    private JTextField targetIpField;
    private JTextField targetPortField;

    public FilePanel(FileManager fileManager, PeerClient peerClient) {
        this.fileManager = fileManager;
        this.peerClient = peerClient;
        setLayout(new GridLayout(1, 2));

        // Left: Local Files
        JPanel localPanel = new JPanel(new BorderLayout());
        localPanel.setBorder(BorderFactory.createTitledBorder("Local Files"));
        localModel = new DefaultTableModel(new String[] { "File Name", "Size" }, 0);
        localTable = new JTable(localModel);

        JPanel localButtons = new JPanel();
        JButton refreshLocalBtn = new JButton("Refresh");
        JButton uploadBtn = new JButton("Upload File");

        localButtons.add(refreshLocalBtn);
        localButtons.add(uploadBtn);

        refreshLocalBtn.addActionListener(e -> refreshLocalFiles());
        uploadBtn.addActionListener(e -> uploadFile());

        localPanel.add(new JScrollPane(localTable), BorderLayout.CENTER);
        localPanel.add(localButtons, BorderLayout.SOUTH);

        // Right: Remote Files
        JPanel remotePanel = new JPanel(new BorderLayout());
        remotePanel.setBorder(BorderFactory.createTitledBorder("Remote Files"));

        JPanel searchPanel = new JPanel();
        targetIpField = new JTextField("localhost", 8);
        targetPortField = new JTextField(4);
        JButton fetchListBtn = new JButton("Fetch List");
        JButton downloadBtn = new JButton("Download Selected");

        searchPanel.add(new JLabel("Peer:"));
        searchPanel.add(targetIpField);
        searchPanel.add(targetPortField);
        searchPanel.add(fetchListBtn);

        remoteModel = new DefaultTableModel(new String[] { "File Name", "Size" }, 0);
        remoteTable = new JTable(remoteModel);

        remotePanel.add(searchPanel, BorderLayout.NORTH);
        remotePanel.add(new JScrollPane(remoteTable), BorderLayout.CENTER);
        remotePanel.add(downloadBtn, BorderLayout.SOUTH);

        // Actions
        fetchListBtn.addActionListener(e -> fetchRemoteFiles());
        downloadBtn.addActionListener(e -> downloadSelectedFile());

        add(localPanel);
        add(remotePanel);

        refreshLocalFiles();
    }

    private void refreshLocalFiles() {
        fileManager.refreshLocalFiles();
        List<SharedFile> files = fileManager.getLocalFiles();
        localModel.setRowCount(0);
        for (SharedFile f : files) {
            localModel.addRow(new Object[] { f.getName(), f.getSize() });
        }
    }

    private void fetchRemoteFiles() {
        String host = targetIpField.getText();
        try {
            int port = Integer.parseInt(targetPortField.getText());
            new Thread(() -> {
                List<SharedFile> files = peerClient.fetchFileList(host, port);
                SwingUtilities.invokeLater(() -> {
                    remoteModel.setRowCount(0);
                    for (SharedFile f : files) {
                        remoteModel.addRow(new Object[] { f.getName(), f.getSize() });
                    }
                    if (files.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No files found or peer offline.");
                    }
                });
            }).start();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Port");
        }
    }

    private void downloadSelectedFile() {
        int row = remoteTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a file to download");
            return;
        }

        String fileName = (String) remoteModel.getValueAt(row, 0);
        String host = targetIpField.getText();
        int port = Integer.parseInt(targetPortField.getText());

        // Choose save location
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dest = fc.getSelectedFile();
            new Thread(() -> {
                boolean success = peerClient.downloadFile(host, port, fileName, dest);
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Download Complete!");
                        refreshLocalFiles(); // If saved to shared folder
                    } else {
                        JOptionPane.showMessageDialog(this, "Download Failed.");
                    }
                });
            }).start();
        }
    }

    private void uploadFile() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            try {
                fileManager.addFile(selectedFile);
                refreshLocalFiles();
                JOptionPane.showMessageDialog(this, "File uploaded to shared folder!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to upload file: " + e.getMessage());
            }
        }
    }
}
