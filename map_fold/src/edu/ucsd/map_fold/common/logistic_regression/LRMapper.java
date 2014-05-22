package edu.ucsd.map_fold.common.logistic_regression;

import Jama.Matrix;
import edu.ucsd.map_fold.common.Mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LRMapper implements Mapper<Matrix,Matrix> {
    public LRMapper(List<Integer> _fields){
        fields = new int[_fields.size()];
        for(int i = 0; i<_fields.size(); i++)
            fields[i] = _fields.get(i);
    }
    public Matrix map(Matrix x) {

        return x.getMatrix(firstRow, fields);
    }
    private int[] fields;
    private int[] firstRow = {0};
}
