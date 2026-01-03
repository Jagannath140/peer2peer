package model;

import java.io.Serializable;
import java.util.Objects;

public class Peer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String host;
    private int port;
    private boolean isOnline;
    private long lastSeen;

    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
        this.isOnline = false;
        this.lastSeen = 0;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        this.isOnline = online;
        if (online) {
            this.lastSeen = System.currentTimeMillis();
        }
    }

    public long getLastSeen() {
        return lastSeen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return port == peer.port && Objects.equals(host, peer.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
