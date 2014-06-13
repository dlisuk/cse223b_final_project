package edu.ucsd.map_fold.common;

/**
 * Created by thomas on 5/27/14.
 */
public class DataSegment {
    public DataSegment(Long start, Long length){
        this.start = start;
        this.length = length;
    }

    public Long getLength() {
        return length;
    }

    public Long getStart() {
        return start;
    }

    public Long start;
    public Long length;
}

