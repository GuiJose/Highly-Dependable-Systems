package hdl.messages;

import java.io.Serializable;

import hdl.Block;

public class ibtfMessage implements Serializable{
    private int serverId;
    private int messageId;
    private int lambda;
    private Block block;
    private String type; //("PREPREPARE", "PREPARE", "COMMIT")

    public ibtfMessage(String type, int serverId, int messageId, int lambda, Block block){
        this.serverId = serverId;
        this.messageId = messageId;
        this.lambda = lambda;
        this.block = block;
        this.type = type;
    }
    public int getServerId(){
        return this.serverId;
    }
    public int getMessageId(){
        return this.messageId;
    }
    public int getLambda(){
        return this.lambda;
    }
    public Block getBlock() {
        return block;
    }
    public String getType() {
        return type;
    }
}
