package edu.ucsd.map_fold.common.logistic_regression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Token implements Serializable{
    public Token(Integer _id, List<Integer> _fields, Double _mu, Double _lambda){
        id      = _id;
        version = 0;
        fields  = new ArrayList<>(_fields);
        state   = new LRState( _mu, _lambda, fields.size());
    }
    private Token(Integer _id, Integer _version, List<Integer> _fields, LRState _state){
        id      = _id;
        version = _version;
        state   = _state;
        fields  = _fields;
    }

    public int     getId()                        { return id; }
    public int     getVersion()                   { return version; }
    public List<Integer> getFields()              { return new ArrayList<>(fields); }
    public LRState getState()                     { return state; }
    public Token   setState( LRState _state )     { return new Token( id, version+1, fields, _state); }

    private final Integer       id;
    private final Integer       version;
    private final List<Integer> fields;
    private LRState             state;
}
