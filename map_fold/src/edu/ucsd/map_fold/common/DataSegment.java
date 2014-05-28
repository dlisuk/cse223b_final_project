package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class DataSegment {
    public DataSegment(long start, long length){
        this.start = start;
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getStart() {
        return start;
    }

    public long start;
    public long length;
}

