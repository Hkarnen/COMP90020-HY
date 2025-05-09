package node;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Peer configuration file handler
 */
public class PeerConfig {
	
	private final ConcurrentHashMap<Integer, Integer> peerMap;
	
	 /** Construct with an existing map (static file or discovery). */
    public PeerConfig(ConcurrentHashMap<Integer,Integer> peerMap) {
        this.peerMap = new ConcurrentHashMap<>(peerMap);

    }
    
    public int getPort(int peerId) {
        return peerMap.get(peerId);
    }

    public Set<Integer> getPeerIds() {
        return peerMap.keySet();
    }
    
    public synchronized Integer getIdByPort(int port) {
        return peerMap.entrySet().stream().filter(e -> e.getValue() == port).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    public ConcurrentHashMap<Integer, Integer> getPeerMap() {
    	return peerMap;
    }

    public synchronized void addPeer(int id, int port) {
        peerMap.put(id, port);
    }
    public synchronized void removePeer(int id) {
        peerMap.remove(id);
    }
    
    public synchronized void removePeerByPort(int port) {
    	peerMap.entrySet().removeIf(e -> e.getValue() == port);
    }

    /**
     * Parse a Java .properties file of the form:
     *     1=5001
     *     2=5002
     *     ...
     */
    public static PeerConfig loadFromFile(String filename) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(filename));

        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        for (String key : props.stringPropertyNames()) {
            // Keys are node ids and values are port numbers
            int id = Integer.parseInt(key);
            int port = Integer.parseInt(props.getProperty(key));
            map.put(id, port);
        }
        
        return new PeerConfig(map);
    }
    
    /**
     * Convenience: test whether a given id appears in the static bootstrap file.
     * Allows the GUI to decide whether this node starts the cluster.
     */
    public static boolean isBootstrap(String filename,int id) throws IOException{
        Properties props = new Properties();
        props.load(new FileInputStream(filename));
        ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
        for (String key : props.stringPropertyNames()) {
            // Keys are node ids and values are port numbers
            int nid = Integer.parseInt(key);
            int nport = Integer.parseInt(props.getProperty(key));
            map.put(nid, nport);
        }
        return map.containsKey(id);
    }
}
