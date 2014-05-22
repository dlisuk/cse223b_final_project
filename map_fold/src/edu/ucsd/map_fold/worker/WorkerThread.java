package edu.ucsd.map_fold.worker;

import edu.ucsd.map_fold.common.DataSet;
import edu.ucsd.map_fold.common.Folder;
import edu.ucsd.map_fold.common.Mapper;
import edu.ucsd.map_fold.common.logistic_regression.Token;

import java.util.concurrent.locks.Lock;

public class WorkerThread{
    public WorkerThread(WorkerNode parent)
    }

    public Token run(Token inToken){
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
            }catch(Exception e){
                error = e;
            }finally{
                stateLock.unlock();
            }
        }else{
            error = new Exception("WorkerThread is locked");
        }
    }

    public Exception getExceptions(){
        return error;
    }

    private       Lock                          stateLock;
    private       Exception                     error;
    private final DataSet<MemRecord>            data;
    private final Token<State,Record,MemRecord> token;
    private final Folder<State,Record>          folder;
    private final Mapper<MemRecord,Record>      mapper;
}
