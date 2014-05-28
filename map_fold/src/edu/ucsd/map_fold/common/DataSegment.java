package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class DataSegment {
    public DataSegment(int start, int length){
        this.start = start;
        this.length = length;
    }

    public long getLength() {
        return length;
    }

    public long getStart() {
        return start;
    }

    public int start;
    public int length;
}

