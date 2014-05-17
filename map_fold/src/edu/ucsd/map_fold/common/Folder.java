package edu.ucsd.map_fold.common;

public interface Folder<S,X> {
    public S fold(S state, X x);
}
