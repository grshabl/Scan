package com.azbuka.gshabalov.tsd_alcho_app.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Simple Class to help write a csv file
 * Created by Lucas B. Ferreira
 */
public class WriteCSVHelper {

    private FileOutputStream fileOutputStream;
    private OutputStreamWriter outputStreamWriter;

    public static final String SEMICOLON_SEPARATOR = "\t";
    public static final String COMMA_SEPARATOR = "\t";

    public static String folderName;
    public static String fileName;
    private String separator;


    public WriteCSVHelper(String folderName, String fileName) {
        this.folderName = folderName;
        this.fileName = fileName;
        this.separator = SEMICOLON_SEPARATOR;
    }

    public WriteCSVHelper(String folderName, String fileName, String separator) {
        this.folderName = folderName;
        this.fileName = fileName;
        this.separator = separator;
    }


    /**
     * Create a csv file
     * @throws IOException
     */
    private void createCsvFile() throws IOException {
        File createPath = new File("/" + folderName);
        if (!createPath.exists())
            createPath.mkdirs();

        String path = createPath.getPath();

        try {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File csvFile = new File(path + "/" + fileName);
            csvFile.createNewFile();
            fileOutputStream = new FileOutputStream(csvFile, true);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create a csv file if don't exists
     * @throws IOException
     */
    private boolean createCsvFileIfDontExists(){
        if(outputStreamWriter == null){
            try {
                createCsvFile();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else return true;
    }


    /**
     * Write a line of text in the csv file with the informed separator
     * @param csvText String Array with text to be written in one line
     * @return true if the line was successfully written, false if not
     */
    public boolean writeLine(String[] csvText){
        if(!createCsvFileIfDontExists()){
            return false;
        }

        StringBuilder textWithSeparator = new StringBuilder();
        for(String text: csvText){
            textWithSeparator.append(text + separator);
        }

        try {
            outputStreamWriter.append(textWithSeparator.toString());
            outputStreamWriter.append("\n");
            outputStreamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Write a line of text in the csv file with the informed separator
     * @param csvText ArrayList<String> with text to be written in one line
     * @return true if the line was successfully written, false if not
     */
    public boolean writeLine(ArrayList<String> csvText){
        if(!createCsvFileIfDontExists()){
            return false;
        }

        StringBuilder textWithSeparator = new StringBuilder();
        for(String text: csvText){
            textWithSeparator.append(text + separator);
        }

        try {
            outputStreamWriter.append(textWithSeparator.toString());
            outputStreamWriter.append("\n");
            outputStreamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Write a line of text in the csv file with the informed separator
     * @param csvText double Array with values to be written in one line
     * @return true if the line was successfully written, false if not
     */
    public boolean writeLine(double[] csvText){
        if(!createCsvFileIfDontExists()){
            return false;
        }

        StringBuilder textWithSeparator = new StringBuilder();
        for(double text: csvText){
            textWithSeparator.append(text + separator);
        }

        try {
            outputStreamWriter.append(textWithSeparator.toString());
            outputStreamWriter.append("\n");
            outputStreamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Write a line of text in the csv file with the informed separator
     * @param csvText int Array with values to be written in one line
     * @return true if the line was successfully written, false if not
     */
    public boolean writeLine(int[] csvText){
        if(!createCsvFileIfDontExists()){
            return false;
        }

        StringBuilder textWithSeparator = new StringBuilder();
        for(double text: csvText){
            textWithSeparator.append(text + separator);
        }

        try {
            outputStreamWriter.append(textWithSeparator.toString());
            outputStreamWriter.append("\n");
            outputStreamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Close the writer
     * @return true if the writer was successfully closed, false if not
     */
    public boolean close(){
        try {
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }



}
