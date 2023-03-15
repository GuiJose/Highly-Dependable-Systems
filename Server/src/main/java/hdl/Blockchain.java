package hdl;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<String> wordsList;
    public Blockchain(){
        wordsList = new ArrayList<>();
    }

    public synchronized void appendString(String word){
        wordsList.add(word);
    }

    public synchronized void print(){
        System.out.println("Blockchain:");
        for (String word : wordsList){
            System.out.print(word);
            System.out.print("====>");
        }
        System.out.print("\n");
    }
}