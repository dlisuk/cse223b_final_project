package edu.ucsd.map_fold.common.logistic_regression;

import Jama.Matrix;
import edu.ucsd.map_fold.common.Mapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LRMapper implements Mapper<Matrix,Matrix> {
    public LRMapper(List<Integer> _fields){
        System.out.println("Creating mapper " + _fields.toString());
        fields = new int[_fields.size()];
        int i = 0;
        System.out.println("STARTING");
        for (Integer e : _fields){
            System.out.println("ADDING" + i + " TO " + e);
            fields[i++] = e.intValue();
        }
        System.out.println("17");
    }
    public Matrix map(Matrix x) {

        return x.getMatrix(firstRow, fields);
    }
    private int[] fields;
    private int[] firstRow = {0};
}
