package pt.ulisboa.tecnico.classes.namingserver;

import java.util.HashSet;
import java.util.Set;

public class ServiceEntry {
    private String nameService;
    private Set<ServerEntry> serverEntries = new HashSet<>();
    
    public ServiceEntry(String name, String host, int port, String qualifiers) {
        this.nameService = name;
        this.serverEntries.add(new ServerEntry(host, port, qualifiers));
    }

    public Set<ServerEntry> getServerEntries() {
        return serverEntries;
    }

    public void add(String host, int port, String qualifiers) {
        this.serverEntries.add(new ServerEntry(host, port, qualifiers));
    }

    public String getNameService(){
        return nameService;
    }
    
    public Set<ServerEntry> lookup(String qualifiers){
        if(qualifiers.isBlank()) 
            return new HashSet<>(serverEntries);
        Set<ServerEntry> servers = new HashSet<>();
        for(ServerEntry server : serverEntries){
            if(server.getQualifiers().equals(qualifiers)){
                    servers.add(server);
            }
        }
        return servers;
    }

    public void remove(String host, int port) {
       for (ServerEntry serverEntry : serverEntries) {
           if(serverEntry.getHost().equals(host) && serverEntry.getPort() == port){
               serverEntries.remove(serverEntry);
               return;
           }
       }
    }
}
