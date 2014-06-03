package edu.ucsd.map_fold.worker.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.util.ArrayList;

public class fileReading{
    public static void main(String[] argv)
    {
        System.out.println("Hello");
        String path = "/Users/max/Dropbox/UCSD/Sp14/cse223b/Final Project/cse223b_final_project/data/test.small.noheader.tsv";
        fromFile(path, 2000000,10);
    }

    public static void fromFile(String path, int offset, int rows){
        System.out.println(path);
        // return new MatrixDataSource(rows, 100);

        BufferedReader br = null;

        try {
            String sCurrentLine;
            RandomAccessFile file = new RandomAccessFile(path,"r");
            file.seek(offset);
            file.readLine();
            int rowSize = 0;
            int colSize = 0;
            int line_count = 0;
            sCurrentLine = file.readLine();
            String[] values = sCurrentLine.split("\\t", -1);
            System.out.println("values length = "+values.length);
            double[] firstRow = new double[values.length];
            ArrayList<double[]> result = new ArrayList<double[]>();
            for(int i = 0;i<values.length;i++)
            {
                System.out.println("i = "+i);
                firstRow[i] = Double.parseDouble(values[i]);
            }
            result.add(firstRow);
            while ((sCurrentLine = file.readLine()) != null) {
                double[] newRow = new double[values.length];
                //parse the tab separated line
                values = sCurrentLine.split("\\t", -1);
                for(int i = 0;i<colSize;i++)
                {
                    newRow[i] = Double.parseDouble(values[i]);
                }
                result.add(newRow);
                System.out.println();
                // System.out.println("line index = " + line_count);
                // System.out.println(sCurrentLine);
            }
            System.out.println("rowSize = "+result.size()+" colSize = "+values.length);
            double[][] array = new double[result.size()][];
            for(int i = 0;i<result.size();i++)
            {
                array[i] = result.get(i);
            }
            System.out.println("array size : rows = "+array.length +" col = "+array[0].length);
            //create the new matrix object
            Matrix matrix = new Matrix(array, array.length, array[0].length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
