package node;

import org.json.JSONObject;

/**
 * Message protocol class
 */
public class Message {
	public enum Type {ELECTION, OK, COORDINATOR, CHAT, HEARTBEAT, QUIT, JOIN, NEW_NODE, PEER_DOWN}
	
	private Type type;				// Election or chat
	private int senderId;			
	private int seq;				// Sequence number (for chat ordering - unimplemented)
	private String content;			// Empty for election messages

	public Message(Type type, int senderId, int seq, String content) {
		this.type = type;
		this.senderId = senderId;
		this.seq = seq;
		this.content = content;
	}
	
	/**
	 * Serializes a message into a json
	 * @return
	 */
    public String toJson() {
        JSONObject obj = new JSONObject();
        obj.put("type",      type.name());
        obj.put("senderId",  senderId);
        obj.put("seq",   seq);
        obj.put("content", content);
        return obj.toString();
    }
    
    /**
     * Deserializes a json message into a message class
     * @param json
     * @return
     */
    public static Message fromJson(String json) {
        JSONObject obj = new JSONObject(json);
        Type type   = Type.valueOf(obj.getString("type"));
        int sid = obj.getInt("senderId");
        int seq = obj.optInt("seq", -1);
        String content   = obj.optString("content", "");
        return new Message(type, sid, seq, content);
    }

    // Getters
    public Type getType() { 
    	return type; 
    }
    
    public int getSenderId() {
    	return senderId;
    }
    
    public int getSeq() {
    	return seq;
    }
    
    public String getContent() {
    	return content;
    }

}
