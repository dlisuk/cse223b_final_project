package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class DataSegment {
    public DataSegment(int start, int length){
        this.start = start;
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public int getStart() {
        return start;
    }

    public int start;
    public int length;
}

