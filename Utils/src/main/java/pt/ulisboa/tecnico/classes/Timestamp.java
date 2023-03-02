package pt.ulisboa.tecnico.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timestamp {
    private List<Integer> timestamp;
    private int position;
    
    public Timestamp() {
        this.timestamp = new ArrayList<>(Collections.nCopies(2, 0));
    }

    public Timestamp(List<Integer> timestamp) {
        this.timestamp = timestamp;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public List<Integer> getTimestamp() {
        return timestamp;
    }

    public boolean lessOrEqual(List<Integer> other) {  
        for (int i = 0; i < this.timestamp.size(); i++) {
            if(this.timestamp.get(i) > other.get(i))
                return false;
        }
        return true;
    }

    public void updatePosition() {
        this.timestamp.set(position, this.timestamp.get(position) + 1);
    }

    public void updateOtherPosition(int otherPosition) {
        this.timestamp.set(otherPosition, this.timestamp.get(otherPosition) + 1);
    }

    public void updateTimestamp(List<Integer> other) {
        for (int i = 0; i < this.timestamp.size(); i++) {
            if(this.timestamp.get(i) < other.get(i))
                this.timestamp.set(i, other.get(i));
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        timestamp.forEach(b::append);
        return b.toString();
    }
}