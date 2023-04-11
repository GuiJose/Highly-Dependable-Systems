package hdl.messages;

import java.io.Serializable;

public class SignatureRequest implements Serializable{
    private int serverId;
    private int messageId;
    private byte[] accountsHash;
    private int blockNumber;

    public SignatureRequest(int serverId, int messageId, byte[] accountsHash, int blockNumber) {
        this.serverId = serverId;
        this.messageId = messageId;
        this.accountsHash = accountsHash;
        this.blockNumber = blockNumber;
    }
    public int getServerId(){
        return this.serverId;
    }
    public int getMessageId(){
        return this.messageId;
    }
    public byte[] getAccountsHash(){
        return this.accountsHash;
    } 
    public int getBlockNumber(){
        return this.blockNumber;
    }
}
