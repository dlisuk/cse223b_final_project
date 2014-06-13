package edu.ucsd.map_fold.worker.data;

import Jama.Matrix;
import edu.ucsd.map_fold.common.DataSet;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MatrixDataSource implements DataSet<Double[]>{
    public static DataSet<Double[]> fromFile(String path, Long offset, Long nbytes) throws IOException {
        Double[][] matrix = FileReading.fromFile(path,offset,nbytes);
        return new MatrixDataSource(matrix);
    }

    public MatrixDataSource(Double[][] _matrix){
        rows   = _matrix.length;
        cols   = _matrix[0].length;
        matrix = _matrix;
    }

    public Iterator<Double[]> iterator() {
        return new Iterator<Double[]>(){
            private int row = 0;
            public boolean hasNext() {
                return row < rows;
            }

            public Double[] next() {
                if( !hasNext() ) throw new NoSuchElementException();
                row += 1;
                return matrix[row-1];
            }
        };
    }

    private int    rows;
    private int    cols;
    private Double[][] matrix;
}
