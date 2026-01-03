package ui;

import core.PeerClient;
import data.PeerManager;
import model.Peer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PeerPanel extends JPanel {
    private final PeerManager peerManager;
    private final PeerClient peerClient;
    private DefaultTableModel tableModel;
    private JTable peerTable;

    public PeerPanel(PeerManager peerManager, PeerClient peerClient) {
        this.peerManager = peerManager;
        this.peerClient = peerClient;
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel();
        JTextField hostField = new JTextField("localhost", 10);
        JTextField portField = new JTextField(5);
        JButton addButton = new JButton("Add Peer");
        JButton refreshButton = new JButton("Check Status");

        inputPanel.add(new JLabel("Host:"));
        inputPanel.add(hostField);
        inputPanel.add(new JLabel("Port:"));
        inputPanel.add(portField);
        inputPanel.add(addButton);
        inputPanel.add(refreshButton);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "Host", "Port", "Status", "Last Seen" };
        tableModel = new DefaultTableModel(columns, 0);
        peerTable = new JTable(tableModel);
        add(new JScrollPane(peerTable), BorderLayout.CENTER);

        // Actions
        addButton.addActionListener(e -> {
            try {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                peerManager.addPeer(host, port);
                refreshTable();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid port number");
            }
        });

        refreshButton.addActionListener(e -> checkPeerStatus());

        // Initial Refresh
        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Peer> peers = peerManager.getPeers();
        for (Peer p : peers) {
            tableModel.addRow(new Object[] {
                    p.getHost(),
                    p.getPort(),
                    p.isOnline() ? "Online" : "Offline",
                    p.getLastSeen() > 0 ? new java.util.Date(p.getLastSeen()).toString() : "Never"
            });
        }
    }

    private void checkPeerStatus() {
        new Thread(() -> {
            List<Peer> peers = peerManager.getPeers();
            for (Peer p : peers) {
                boolean online = peerClient.connectAndHello(p.getHost(), p.getPort());
                peerManager.updatePeerStatus(p.getHost(), p.getPort(), online);
            }
            SwingUtilities.invokeLater(this::refreshTable);
        }).start();
    }
}
