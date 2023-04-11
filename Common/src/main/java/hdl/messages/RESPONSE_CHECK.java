package hdl.messages;

import java.io.Serializable;

import hdl.Block;

public class RESPONSE_CHECK implements Serializable{
    private int serverId;
    private int messageId;
    
    private int checkId;
    private int balance;
    private int timestamp;
    
    private boolean isStrong;  
    private Block block;
    private boolean isFirstValue;
    

    public RESPONSE_CHECK(int serverId, int messageId, int balance, int timestamp, int checkId, boolean isStrong){
        this.serverId = serverId;
        this.messageId = messageId;
        this.balance = balance;
        this.timestamp = timestamp;
        this.checkId = checkId;
        this.isStrong = isStrong;
    }

    public RESPONSE_CHECK(int serverId, int messageId, Block block, boolean isStrong, boolean isFirstValue){
        this.serverId = serverId;
        this.messageId = messageId;
        this.block = block;
        this.isStrong = isStrong;
        this.isFirstValue = isFirstValue;
    }
    
    public boolean getIsStrong(){
        return this.isStrong;
    }
    public int getServerId(){
        return this.serverId;
    } 
    public int getMessageId(){
        return this.messageId;
    }
    public int getBalance(){
        return this.balance;
    }
    public int getTimestamp(){
        return this.timestamp;
    }
    public int getCheckId(){
        return this.checkId;
    }
    public Block getBlock(){
        return this.block;
    }
    public boolean getIsFirstValue(){
        return this.isFirstValue;
    }
}
