package pt.ulisboa.tecnico.classes.namingserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.Server;

public class ServerEntry {
    private String host;
    private int port;
    private String qualifiers;

    public ServerEntry(String host, int port, String qualifiers) {
        this.host = host;
        this.port = port;
        this.qualifiers = qualifiers;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getQualifiers() {
        return qualifiers;
    }

    public Server proto() {
        return Server.newBuilder().setHost(host).setPort(port).setQualifier(qualifiers).build();
    }
}
