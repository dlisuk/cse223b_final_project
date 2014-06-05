package edu.ucsd.map_fold.common.logistic_regression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Token implements Serializable{
    public Token(Integer _id, Double _mu, Double _lambda, Integer nFields){
        id      = _id;
        version = 0;
        state   = new LRState( _mu, _lambda, nFields);
    }
    private Token(Integer _id, Integer _version, LRState _state){
        id      = _id;
        version = _version;
        state   = _state;
    }

    public String toJson(){
        String out = "{ \"lambda\":"+state.getLambda()+", \"mu\":" + state.getMu() + ", \"offset\":" + state.getOffset() + ", \"w\":[";
        boolean first = true;
        for(double x : state.getWeights()){
            if(!first)
                out += ", ";
            first = false;
            out += Double.toString(x);
        }
        return out + "]}";

    }

    public int     getId()                        { return id; }
    public int     getVersion()                   { return version; }
    public LRState getState()                     { return state; }
    public Token   setState( LRState _state )     { return new Token( id, version+1, _state); }

    private final Integer       id;
    private final Integer       version;
    private LRState             state;
}
