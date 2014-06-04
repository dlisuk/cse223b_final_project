package edu.ucsd.map_fold.worker.data;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class FileReading{
    public static void test() {
        RandomAccessFile file = new RandomAccessFile(path,"r");
        file.seek(100);
        char[] buffer = new char[](20);
        file.read(buffer,0,10);
        j = 0;
        i = 0;
        for c <- buffer {
            if(c !=',') {
                i++;
            }else {
                matrix[x][y] =double(c[j:i])
                j = i;
            }
        }
        for x = j:i {
            c[x - j] = c[x]
        }
        file.read(buffer[x],0,10);

    }
    public static Matrix fromFile(String path, int offset, int byteLimit) throws IOException {
        String sCurrentLine;
        RandomAccessFile file = new RandomAccessFile(path,"r");
        file.seek(offset);
        file.readLine();

        sCurrentLine = file.readLine();
        String[] values = sCurrentLine.split("\\t", -1);
        //System.out.println("values length = "+values.length);
        double[] firstRow = new double[values.length];
        ArrayList<double[]> result = new ArrayList<double[]>();
        for(int i = 0;i<values.length;i++)
        {
            System.out.println("i = "+i);
            firstRow[i] = Double.parseDouble(values[i]);
        }
        result.add(firstRow);

        while (( sCurrentLine = file.readLine()) != null) {
            double[] newRow = new double[values.length];
            //parse the tab separated line
            values = sCurrentLine.split("\\t", -1);
            for(int i = 0;i<values.length;i++)
            {
                newRow[i] = Double.parseDouble(values[i]);
            }
            result.add(newRow);
            System.out.println();
            // System.out.println("line index = " + line_count);
            // System.out.println(sCurrentLine);
        }
        //System.out.println("rowSize = "+result.size()+" colSize = "+values.length);
        double[][] array = new double[result.size()][];
        for(int i = 0;i<result.size();i++)
        {
            array[i] = result.get(i);
        }
        //System.out.println("array size : rows = "+array.length +" col = "+array[0].length);
        //create the new matrix object
        return new Matrix(array, array.length, array[0].length);
        }
    }

}
