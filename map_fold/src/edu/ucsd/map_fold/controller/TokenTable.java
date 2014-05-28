package edu.ucsd.map_fold.controller;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TokenTable {
    public TokenTable(Integer nTokens, Integer segments){
        tokenTable = new ArrayList<>(nTokens);
        for( Integer i = 0; i < nTokens; i ++ ) {
            TokenTableEntry entry = new TokenTableEntry(segments, i);
            Pair data = new Pair(false, entry);
            tokenTable.add(i,data);
        }
    }

    public Boolean isRunning(Integer tokenId){ return tokenTable.get(tokenId).running(); }
    public void startRunning(Integer tokenId){ tokenTable.get(tokenId).setRun(true); }
    public void stopRunning(Integer tokenId){ tokenTable.get(tokenId).setRun(false); }
    public TokenTableEntry getLatestVersion(Integer tokenId){ return tokenTable.get(tokenId).getLatest(); }
    public void rebaseToPrevious(Integer tokenId){ tokenTable.get(tokenId).rebaseToPrevious(); }
    public void newVersion(Integer tokenId, Integer segment, Integer workerId){
    }
    public void lock(){editLock.lock();}
    public void unlock(){editLock.unlock();}


    private List<Pair> tokenTable;
    private Lock editLock = new ReentrantLock();
    private class Pair{
        public Pair(Boolean _t1, TokenTableEntry _t2){
            t1 = _t1;
            t2 = new ArrayList<>();
            t2.add(_t2);
        }

        public TokenTableEntry getLatest(){
            return t2.get(t2.size()-1);
        }
        public TokenTableEntry getVersion(int version){
            return t2.get(version);
        }
        public void newVersion(Integer segment, Integer workerId){
            TokenTableEntry head = getLatest();
            TokenTableEntry newHead = head.seenDataSegment(segment);
            t2.add(newHead.getTokenVersion(), newHead);
        }
        public void rebaseToPrevious(){
            rebase(t2.size()-2);
        }
        public void rebase(int version){
            for( int i = t2.size()-1; version <= i; i--){
                t2.remove(i);
            }
        }

        public Boolean running(){ return t1; }
        public void setRun(Boolean _t1){ t1 = _t1; }

        private Boolean t1;
        private List<TokenTableEntry> t2;
    }
}
