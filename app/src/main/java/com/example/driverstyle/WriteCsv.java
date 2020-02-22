package com.example.driverstyle;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class WriteCsv {
    private static String fileName = "data.csv";
    private static String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static String path = baseDir + File.separator + fileName;
    private static File file = new File(path);

    static void writeDataByLine(String time, String speed)
    {
        if (checkFile(path)) {
            fileWrite(time, speed);
        }
        else {
            createFile(time, speed);
        }
    }

    private static boolean checkFile(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    private static void createFile(String time,String speed) {
        try {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "Time", "Speed" };
            writer.writeNext(header);
            writer.close();

            fileWrite(time, speed);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void fileWrite(String time, String speed){
        try {
            FileWriter outputfile = new FileWriter(file, true);
            CSVWriter writer = new CSVWriter(outputfile);
            String[] data = { time , speed };
            writer.writeNext(data);
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
