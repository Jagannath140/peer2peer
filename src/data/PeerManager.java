package data;

import model.Peer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeerManager {
    // Thread-safe list for concurrent access (UI reading vs Network writing)
    private final List<Peer> peers = new CopyOnWriteArrayList<>();

    public void addPeer(String host, int port) {
        Peer newPeer = new Peer(host, port);
        if (!peers.contains(newPeer)) {
            peers.add(newPeer);
        }
    }

    public List<Peer> getPeers() {
        return Collections.unmodifiableList(peers);
    }

    public Peer getPeer(String host, int port) {
        for (Peer p : peers) {
            if (p.getHost().equals(host) && p.getPort() == port) {
                return p;
            }
        }
        return null;
    }

    public void updatePeerStatus(String host, int port, boolean isOnline) {
        Peer p = getPeer(host, port);
        if (p != null) {
            p.setOnline(isOnline);
        } else {
            // Optionally auto-add if we receive a connection from unknown peer
            addPeer(host, port);
            getPeer(host, port).setOnline(isOnline);
        }
    }
}
