package node;

import java.util.Map;
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
}
