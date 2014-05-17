package edu.ucsd.map_fold.common;

public interface Mapper<A,B> {
    public Iterable<B> map(A x);
}
