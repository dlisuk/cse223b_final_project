package edu.ucsd.map_fold.worker.data;

import Jama.Matrix;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import Jama.Matrix;

public class FileReading{
    /*public static void test() {
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
    }*/
=======
    public static void main(String[] argv)
    {
        System.out.println("Hello");
        String path = "/Users/max/Dropbox/UCSD/Sp14/cse223b/Final Project/cse223b_final_project/data/test.small.noheader.tsv";
        fromFile(path, 2000000,10);
    }
>>>>>>> Stashed changes


    public static Double[][] fromFile(String path, int offset, int bytes) throws IOException {
        String sCurrentLine;
        RandomAccessFile file = new RandomAccessFile(path,"r");
        //get the col size
        sCurrentLine = file.readLine();
        String[] values = sCurrentLine.split("\\t", -1);
        ArrayList<Double[]> result = new ArrayList<Double[]>();
        file.seek(offset);
        //read a certain bytes
        byte[] chars = new byte[bytes];
        int numOfBytes = file.read(chars, 0, bytes);

        String read = new String(chars);
        //System.out.println(read);
        //chop into rows first
        String[] rows = read.split("\\n",-1);
        for(int i = 0;i<rows.length;i++)
        {
            String[] chop = rows[i].split("\\t", -1);
            //handle the last line
            if(chop.length != values.length)
            {
                rows[i] += file.readLine();
                chop = rows[i].split("\\t", -1);
            }

            //if statement to handle cases where eof is reached
            if(chop.length == values.length)
            {
                Double[] newRow = new Double[values.length];
                for(int j = 0;j<values.length;j++)
                {
                    newRow[j] = Double.parseDouble(chop[j]);
                }
                result.add(newRow);
            }
        }

        Double[][] array = new Double[result.size()][];
        for(int i = 0;i<result.size();i++)
        {
            array[i] = result.get(i);
        }

        //for printing only
        /*for(int i = 0;i<array.length;i++)
        {
            for(int j = 0;j<array[0].length;j++)
            {
                System.out.print(array[i][j]+" ");
            }
            System.out.println();
        }*/

        return array;

        //System.out.println("array size : rows = "+array.length +" col = "+array[0].length);
        //create the new matrix object
        //return new Matrix(array, array.length, array[0].length);
    }
}
