package parsing;

import java.util.*;

public class Record {
    private Map<String, String> tags;
    private Map<Integer, Move[]> moves;

    private String result;

    public Record(Map<String, String> tags, Map<Integer,Move[]> moves, String result){
        this.tags = tags;
        this.moves = moves;
        this.result = result;
    }
    public List<Move> getMoves(){
        List<Move> moves = new ArrayList<>();
        this.moves.forEach((key,value)->{
            moves.add(value[0]);
            moves.add(value[1]);
        });

        return moves;
    }

    public String getResult() {
        return result;
    }

    public Map<Integer,Move[]> getRecord(){
        return this.moves;
    }
    public Map<String,String> getTags(){
        return tags;
    }
}