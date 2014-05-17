package edu.ucsd.map_fold.worker;

import edu.ucsd.map_fold.common.DataSet;
import edu.ucsd.map_fold.common.Folder;
import edu.ucsd.map_fold.common.Mapper;
import edu.ucsd.map_fold.common.Token;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Worker <MemRecord,Record,State> {
    public Worker(DataSet<MemRecord> _data, Token<State,Record,MemRecord> _token){
        data   = _data;
        token  = _token;
        folder = token.getFolder();
        mapper = token.getMapper();
        stateLock = new ReentrantLock();
    }

    public boolean doWork(){
        if(stateLock.tryLock()){
            try {
                stateLock.lock();
                State state = token.getState();
                for (MemRecord memRec : data) {
                    Iterable<Record> records = mapper.map(memRec);
                    for (Record rec : records)
                        state = folder.fold(state, rec);
                }
                token.setState(state);
                return true;
            }catch(Exception e){
                System.err.println(e.getMessage());
                return false;
            }finally{
                stateLock.unlock();
            }
        }else{
            return false;
        }
    }

    private       Lock                          stateLock;
    private final DataSet<MemRecord>            data;
    private final Token<State,Record,MemRecord> token;
    private final Folder<State,Record>          folder;
    private final Mapper<MemRecord,Record>      mapper;
}
