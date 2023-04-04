package hdl.messages;

import java.io.Serializable;
import java.security.PublicKey;

public class CREATE_MESSAGE implements Serializable{
    private int user_id;
    private int message_id;
    private String ip;
    private int port;
    private byte[] signature = null;
    private PublicKey publicKey;

    public CREATE_MESSAGE(int user_id, int message_id, String ip, int port, PublicKey publicKey) {
        this.user_id = user_id;
        this.message_id = message_id;
        this.ip = ip;
        this.port = port;
        this.publicKey = publicKey;
    }
    public int getId(){
        return this.user_id;
    }
    public int getMessageId(){
        return this.message_id;
    }
    public String getIp(){
        return this.ip;
    }
    public int getPort(){
        return this.port;
    }
    public byte[] getSignature(){
        return this.signature;
    }
    public PublicKey getKey(){
        return this.publicKey;
    }
}
