package hdl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable{
    private List<byte[]> operations = new ArrayList<>();

    public Block(){}

    public void appendOperation(byte[] o){
        operations.add(o);
    }

    public void clearBlock(){
        operations.clear();
    }

    public int getSize(){
        return operations.size();
    }

    public List<byte[]> getOperations(){
        return operations;
    }
}
