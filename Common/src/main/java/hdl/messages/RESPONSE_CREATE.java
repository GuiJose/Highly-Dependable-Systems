package hdl.messages;

import java.io.Serializable;

public class RESPONSE_CREATE implements Serializable{
    private int serverId;
    private int messageId;
    private boolean succeded;

    public RESPONSE_CREATE(int serverId, int messageId, boolean succeded){
        this.serverId = serverId;
        this.messageId = messageId;
        this.succeded = succeded;
    }
    public int getServerId(){
        return this.serverId;
    } 
    public int getMessageId() {
        return messageId;
    }
    public boolean getSucceded(){
        return succeded;
    }
}
