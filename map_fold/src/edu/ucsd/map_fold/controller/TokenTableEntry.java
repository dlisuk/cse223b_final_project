package edu.ucsd.map_fold.controller;

import java.util.HashSet;
import java.util.Set;

public class TokenTableEntry {
    public TokenTableEntry(Integer dataSegements, Integer _tokenId){
        tokenId = _tokenId;
        tokenVersion = 0;
        notSeen  = new HashSet<>();
        for( Integer i = 0; i < dataSegements; i++ ){
            notSeen.add(i);
        }
    }
    private TokenTableEntry(Integer _tokenId, Integer _tokenVersion, Set<Integer> _notSeen){
        tokenId = _tokenId;
        tokenVersion = _tokenVersion;
        notSeen = new HashSet<>(_notSeen);
    }

    public Integer getTokenVersion(){ return tokenVersion; }
    public Integer getTokenId(){ return tokenId; }
    public void addHost(Integer hostid){
        hosts.add(hostid);
    }
    public void removeHost(Integer hostid){
        hosts.remove(hostid);
    }
    public Integer getHost(){

        if(hosts.size() > 0 )
            return hosts.iterator().next();
        else
            return -1;
    }
    public Integer nHosts(){
        return hosts.size();
    }
    public Set<Integer> getNotSeen(){ return new HashSet<>(notSeen); }

    TokenTableEntry seenDataSegment(Integer i){
        Set<Integer> newNotSeen = new HashSet<>(notSeen);
        newNotSeen.remove(i);
        return new TokenTableEntry(tokenId, tokenVersion + 1, newNotSeen);
    }

    private Integer tokenId;
    private Integer tokenVersion;
    private Set<Integer> hosts = new HashSet<>();
    private Set<Integer> notSeen;
}
