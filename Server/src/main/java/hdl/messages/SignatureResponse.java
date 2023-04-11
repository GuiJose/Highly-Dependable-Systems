package hdl.messages;

import java.io.Serializable;

public class SignatureResponse implements Serializable{
    private int serverId;
    private int messageId;
    private byte[] signature;
    private int blockNumber;

    public SignatureResponse(int serverId, int messageId, byte[] signature, int blockNumber) {
        this.serverId = serverId;
        this.messageId = messageId;
        this.signature = signature;
        this.blockNumber = blockNumber;
    }
    public int getServerId(){
        return this.serverId;
    }
    public int getMessageId(){
        return this.messageId;
    }
    public byte[] getSignature(){
        return this.signature;
    } 
    public int getBlockNumber(){
        return this.blockNumber;
    }
}
