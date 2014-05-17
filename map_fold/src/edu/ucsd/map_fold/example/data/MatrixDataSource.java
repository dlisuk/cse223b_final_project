package edu.ucsd.map_fold.example.data;

import Jama.Matrix;
import edu.ucsd.map_fold.common.DataSet;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MatrixDataSource implements DataSet<Matrix>{
    public MatrixDataSource(int _rows, int _cols){
        rows   = _rows;
        cols   = _cols;
        matrix = new Matrix(rows,cols);
    }


    public Iterator<Matrix> iterator() {
        return new Iterator<Matrix>(){
            private int row = -1;
            public boolean hasNext() {
                return row < rows;
            }

            public Matrix next() {
                row += 1;
                if( !hasNext() ) throw new NoSuchElementException();
                return matrix.getMatrix(row,row,0,cols);
            }
        };
    }

    private int    rows;
    private int    cols;
    private Matrix matrix;
}