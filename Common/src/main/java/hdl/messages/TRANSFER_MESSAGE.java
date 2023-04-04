package hdl.messages;

import java.io.Serializable;
import java.security.PublicKey;

public class TRANSFER_MESSAGE implements Serializable{
    private int user_id;
    private int message_id;
    private String ip;
    private int port;
    private int amount;
    private PublicKey SPK;
    private PublicKey DPK;
    private int destUserId;

    public TRANSFER_MESSAGE(int user_id, int message_id, String ip, int port, PublicKey SPK, PublicKey DPK, int amount, int destUserId) {
        this.user_id = user_id;
        this.message_id = message_id;
        this.ip = ip;
        this.port = port;
        this.SPK = SPK;
        this.DPK = DPK;
        this.amount = amount;
        this.destUserId = destUserId;
    }
    public int getDestUserId() {
        return destUserId;
    }
    public int getUserId(){
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
    public PublicKey getSPK(){
        return this.SPK;
    }
    public PublicKey getDPK(){
        return this.DPK;
    }
    public int getAmount(){
        return this.amount;
    }
}
