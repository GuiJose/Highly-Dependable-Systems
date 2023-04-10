package hdl;
import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<Block> blocks;
    public Blockchain(){
        blocks = new ArrayList<>();
    }

    public synchronized void appendBlock(Block block){
        blocks.add(block);
    }

    public synchronized void printBlockchain(){
        for(Block b : blocks){
            System.out.print("[");
            b.printBlock(b);
            System.out.print("] =====> ");
        }
        System.out.print("\n");
    }
}