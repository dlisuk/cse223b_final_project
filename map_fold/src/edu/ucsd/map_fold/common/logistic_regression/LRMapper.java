package edu.ucsd.map_fold.common.logistic_regression;

import Jama.Matrix;
import edu.ucsd.map_fold.common.Mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LRMapper implements Mapper<Matrix,Matrix> {
    public LRMapper(List<Integer> _fields){
        fields = _fields.toArray(firstRow);
    }
    public Matrix map(Matrix x) {

        return x.getMatrix(firstRow, fields);
    }
    private int[] fields;
    private int[] firstRow = {0};
}
