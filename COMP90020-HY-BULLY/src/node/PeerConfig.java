package node;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PeerConfig {
	private Map<Integer, Integer> peerMap;
	
	public PeerConfig(Map<Integer, Integer> peerMap) {
        this.peerMap = peerMap;
    }

    public int getPort(int peerId) {
        return peerMap.get(peerId);
    }

    public Set<Integer> getPeerIds() {
        return peerMap.keySet();
    }
    
    public Map<Integer, Integer> getPeerMap() {
    	return peerMap;
    }
    
 // Load from a properties file
    public static PeerConfig loadFromFile(String filename) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(filename));
        
        Map<Integer, Integer> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            // Keys are node ids and values are port numbers
            int id = Integer.parseInt(key);
            int port = Integer.parseInt(props.getProperty(key));
            map.put(id, port);
        }
        
        return new PeerConfig(map);
    }
}
