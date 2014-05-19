package edu.ucsd.map_fold.common;

public class Token<State,Record,MemRecord>{
    public Token(int _id, int _version, Folder<State,Record> _folder, Mapper<MemRecord,Record> _mapper, State _state){
        id      = _id;
        version = _version;
        folder  = _folder;
        mapper  = _mapper;
        state   = _state;
    }

    public int   getId()                        { return id; }
    public int   getVersion()                   { return version; }
    public State getState()                     { return state; }
    public void  setState( State _state )       { state = _state; }
    public Folder<State,Record> getFolder()     { return folder; }
    public Mapper<MemRecord,Record> getMapper() { return mapper; }

    private final int                      id;
    private final int                      version;
    private final Folder<State, Record>    folder;
    private final Mapper<MemRecord,Record> mapper;
    private       State                    state;
}
