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

    /*public synchronized void print(){
        System.out.println("Blockchain:");
        for (String word : wordsList){
            System.out.print(word);
            System.out.print("====>");
        }
        System.out.print("\n");
    }*/
}