package ru.av.test.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Simple Class to help write a csv file
 * Created by Lucas B. Ferreira
 */
public class WriteCSVHelper {

    private FileOutputStream fileOutputStream;
    private OutputStreamWriter outputStreamWriter;

    public static final String SEMICOLON_SEPARATOR = "\t";

    public static String folderName;
    public static String fileName;
    private String separator;


    public WriteCSVHelper(String folderName, String fileName, String separator) {
        this.folderName = folderName;
        this.fileName = fileName;
        this.separator = separator;
    }


    /**
     * Create a csv file
     *
     * @throws IOException
     */
    private void createCsvFile() throws IOException {
        File file = Environment.getExternalStorageDirectory();
        file = file.getParentFile();
        file = file.getParentFile();
        Log.d("asd", file.listFiles() == null ? "Ok" : "НеОК");
        if (file != null && file.listFiles() != null) {
            for (File tmp : file.listFiles())
                if (tmp.toString().contains("sdcard1")) {
                    File[] files = tmp.listFiles();
                    for (File tmp2 : files)
                        if (tmp2.getName().contains("AvExchange"))
                            folderName = tmp2.toString() + "/Out";
                }
        }
        File createPath = new File("/" + folderName);
        createPath.mkdir();
        String path = createPath.getPath();

        try {
            File csvFile = new File(path + "/" + fileName + "");
            csvFile.createNewFile();
            fileOutputStream = new FileOutputStream(csvFile);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create a csv file if don't exists
     *
     * @throws IOException
     */
    private boolean createCsvFileIfDontExists() {
        if (outputStreamWriter == null) {
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
     *
     * @param csvText String Array with text to be written in one line
     * @return true if the line was successfully written, false if not
     */
    public boolean writeLine(String[] csvText) {
        if (!createCsvFileIfDontExists()) {
            return false;
        }

        StringBuilder textWithSeparator = new StringBuilder();
        for (String text : csvText) {
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
     *
     * @return true if the writer was successfully closed, false if not
     */
    public boolean close() {
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
