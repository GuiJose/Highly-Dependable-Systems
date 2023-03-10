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

    public synchronized List<String> getBlockchain(){
        return wordsList;
    } 
}