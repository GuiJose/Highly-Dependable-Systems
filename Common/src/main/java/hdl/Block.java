package hdl;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import hdl.messages.TRANSFER_MESSAGE;

public class Block implements Serializable{
    private List<List<Object>> operations = new ArrayList<>();
    // [[TRANFER_MESSAGE, Signature]]
    private int serverId;

    private boolean isSpecial;
    private ConcurrentHashMap<PublicKey, int[]> accounts;
    private ConcurrentHashMap<Integer, byte[]> signatures = new ConcurrentHashMap<>(); 


    public Block(int serverId, Boolean isSpecial){
        this.serverId = serverId;
        this.isSpecial = isSpecial;
    }

    public Block(int serverId, Boolean isSpecial, ConcurrentHashMap<PublicKey, int[]> accounts){
        this.serverId = serverId;
        this.isSpecial = isSpecial;
        this.accounts = accounts;
    }

    public void appendOperation(TRANSFER_MESSAGE msg, byte[] signature){
        List<Object> newList = new ArrayList<>();
        newList.add(msg);
        newList.add(signature);
        operations.add(newList);
    }
    public int getServerId(){
        return this.serverId;
    }
    public void clearBlock(){
        operations.clear();
    }
    public int getSize(){
        return operations.size();
    }
    public List<List<Object>> getOperations(){
        return operations;
    }
    public Boolean getIsSpecial(){
        return this.isSpecial;
    }
    public ConcurrentHashMap<PublicKey, int[]> getAccounts(){
        return this.accounts;
    }
    public ConcurrentHashMap<Integer, byte[]> getSignatures(){
        return this.signatures;
    }
    public void printBlock(Block b){
        if (b.getIsSpecial()){
            System.out.print("SPECIAL");
            return; 
        }
        for (List<Object> o : operations){
            TRANSFER_MESSAGE msg = (TRANSFER_MESSAGE) o.get(0);
            System.out.print("(T:");
            System.out.print(msg.getUserId() + ":" + msg.getDestUserId() + ":" + msg.getAmount());
            System.out.print(") ");
        }
    } 
}
