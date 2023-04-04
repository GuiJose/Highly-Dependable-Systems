package hdl.messages;

import java.io.Serializable;

public class RESPONSE_CHECK implements Serializable{
    private int serverId;
    private int messageId;
    private int balance;
    private int timestamp;

    public RESPONSE_CHECK(int serverId, int messageId, int balance, int timestamp){
        this.serverId = serverId;
        this.messageId = messageId;
        this.balance = balance;
        this.timestamp = timestamp;
    }
    public int getServerId(){
        return this.serverId;
    } 
    public int getMessageId(int message_id){
        return this.messageId;
    }
    public int getBalance(){
        return this.balance;
    }
    public int getTimestamp(){
        return timestamp;
    }
}
