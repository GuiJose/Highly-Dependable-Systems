package hdl.messages;

import java.io.Serializable;

public class ACK_MESSAGE implements Serializable{
    private int id;
    private int messageID;
    private int messageACKED;

    public ACK_MESSAGE(int id, int messageID, int messageACKED){
        this.id = id;
        this.messageID = messageID;
        this.messageACKED = messageACKED;
    }
    public int getId(){
        return this.id;
    }
    public int getMessageId(){
        return this.messageID;
    }
    public int getMessageACKED(){
        return this.messageACKED;
    }
}
