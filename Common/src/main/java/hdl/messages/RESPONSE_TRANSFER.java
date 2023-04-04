package hdl.messages;

import java.io.Serializable;

public class RESPONSE_TRANSFER implements Serializable{
    private int serverId;
    private int amount;
    private int messageId;
    private int tranferId;
    private boolean succeded;
    private int destUserId;

    public RESPONSE_TRANSFER(int serverId, int messageId, int tranferId, int destUserId, boolean succeded, int amount){
        this.serverId = serverId;
        this.messageId = messageId;
        this.tranferId = tranferId;
        this.destUserId = destUserId;
        this.succeded = succeded;
        this.amount = amount;
    }
    public int getAmount(){
        return this.amount;
    } 
    public int getServerId(){
        return this.serverId;
    } 
    public int getDestUserId() {
        return destUserId;
    }
    public int getMessageId() {
        return messageId;
    }
    public int getTranferId() {
        return tranferId;
    }
    public boolean getSucceded(){
        return succeded;
    }
}
