package hdl.messages;

import java.io.Serializable;

public class RESPONSE_USER implements Serializable{
    private int serverId;
    private int messageID;
    private String message;

    public RESPONSE_USER(int serverId, int messageID){
        this.serverId = serverId;
        this.messageID = messageID;
    }
    public int getServerId(){
        return this.serverId;
    } 
    public int getMessageId(int message_id){
        return this.messageID;
    }
    public String getMessage(){
        return this.message;
    }
    public void setMessage(String message){
        this.message = message;
    }
}
