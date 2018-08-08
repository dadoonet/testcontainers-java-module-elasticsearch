package fr.pilato.elasticsearch.containers;

public class TcpHost {

    private final String hostname;
    private final int port;

    public TcpHost(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
