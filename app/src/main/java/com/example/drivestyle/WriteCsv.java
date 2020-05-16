package com.example.drivestyle;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class WriteCsv {
    private static String fileName = "data.csv";
    private static String baseDir = Environment.getExternalStorageDirectory() +
            File.separator + "DriveStyle";
    private static String path = baseDir + File.separator + fileName;
    private static File file = new File(path);

    static void writeDataByLine(String time,String speed, String lat,
                                String longitude,
                                String ax, String ay,
                                String az)
    {
        if (checkFile(path)) {
            fileWrite(time, speed, lat, longitude, ax, ay, az);
        }
        else {
            createFile(time, speed, lat, longitude, ax, ay, az);
        }
    }

    private static boolean checkFile(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    private static void createFile(String time,String speed, String lat,
                                   String longitude,
                                   String ax, String ay,
                                   String az) {
        try {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile);

            String[] header = { "Time", "Speed", "Lat", "Long", "AX", "AY", "AZ" };
            writer.writeNext(header);
            writer.close();

            fileWrite(time, speed, lat, longitude, ax, ay, az);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void fileWrite(String time,String speed, String lat,
                                  String longitude,
                                  String ax, String ay,
                                  String az){
        try {
            FileWriter outputfile = new FileWriter(file, true);
            CSVWriter writer = new CSVWriter(outputfile);
            String[] data = { time, speed, lat, longitude, ax, ay, az };
            writer.writeNext(data);
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
