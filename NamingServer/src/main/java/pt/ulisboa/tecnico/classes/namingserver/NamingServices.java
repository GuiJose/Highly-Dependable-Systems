package pt.ulisboa.tecnico.classes.namingserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NamingServices {
    Map<String, ServiceEntry> serviceEntries = new HashMap<>();

    public int register(String name, String host, int port, String qualifiers) {
        if(serviceEntries.containsKey(name)){
            ServiceEntry serviceEntry = serviceEntries.get(name);

            serviceEntry.add(host, port, qualifiers);
            return serviceEntry.getServerEntries().size() - 1;
        } else{
            ServiceEntry serviceEntry = new ServiceEntry(name, host, port, qualifiers);
            serviceEntries.put(name, serviceEntry);
            return 0;
        }
    }

    public Set<ServerEntry> lookup(String name, String qualifiers){
        if(serviceEntries.containsKey(name)){
            ServiceEntry serviceEntry = serviceEntries.get(name);
            return serviceEntry.lookup(qualifiers);
        }
        return new HashSet<>();
    }

    public void remove(String name, String host, int port) {
        if(serviceEntries.containsKey(name)){
            ServiceEntry serviceEntry = serviceEntries.get(name);
            serviceEntry.remove(host, port);
        }
    }
}
