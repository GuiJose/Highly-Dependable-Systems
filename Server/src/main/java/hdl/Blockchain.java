package hdl;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<String> wordsList;
    public Blockchain(){
        wordsList = new ArrayList<>();
    }

    public void appendString(String word){
        wordsList.add(word);
    } 

    public List<String> getBlockchain(){
        return wordsList;
    } 
}
