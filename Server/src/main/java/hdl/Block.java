package hdl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import hdl.messages.TRANSFER_MESSAGE;

public class Block implements Serializable{
    private List<List<Object>> operations = new ArrayList<>();
    // [TRANFER_MESSAGE, Signature]
    private int serverId;

    public Block(int serverId){
        this.serverId = serverId;
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

    public void printBlock(Block b){
        for (List<Object> o : operations){
            TRANSFER_MESSAGE msg = (TRANSFER_MESSAGE) o.get(0);
            System.out.print("(T:");
            System.out.print(msg.getUserId() + ":" + msg.getDestUserId() + ":" + msg.getAmount());
            System.out.print(") ");
        }
    } 
}
