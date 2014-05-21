package edu.ucsd.map_fold.common;

public interface StateFactory<A> {
    public A fromString(String params);
}
