package ru.av.test.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

import ru.av.test.activities.ScannerActivity;


public class CSVReadingHelper {
    private ArrayList<String> texts = new ArrayList<>();
    private ArrayList<String> ean13 = new ArrayList<>();
    private ArrayList<String> marks = new ArrayList<>();
    private ArrayList<String> ltInMarks = new ArrayList<>();
    private ArrayList<String> stInMarks = new ArrayList<>();
    private ArrayList<ArrayList<String>> list = new ArrayList<>();
    private ArrayList<String> plods = new ArrayList<>();
    private String[] s;
    public int pointer;
    private ArrayList<String> lists = new ArrayList<>();
    private ArrayList<String> multiplicity = new ArrayList<>();

    public CSVReadingHelper(String fileName) {
        BufferedReader reader;
        texts = new ArrayList<>();
        ean13 = new ArrayList<>();
        marks = new ArrayList<>();
        ltInMarks = new ArrayList<>();
        stInMarks = new ArrayList<>();
        list = new ArrayList<>();
        plods = new ArrayList<>();
        lists = new ArrayList<>();
        plods = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
            String line;
            ArrayList<String> listl;
            while ((line = reader.readLine()) != null) {
                if (line.contains("плода"))
                    continue;
                s = line.split("\\t");
                if (s.length > 8) {
                    plods.add(s[0]);
                    ean13.add(s[4]);
                    texts.add(s[5]);
                    marks.add(s[6]);
                    stInMarks.add(s[7]);
                    ltInMarks.add(s[8]);
                    multiplicity.add(s[9]);
                    listl = new ArrayList<>();
                    Collections.addAll(listl, s);
                    list.add(listl);
                }
            }
            reader.close();
        } catch (UnsupportedEncodingException e) {
            Log.d("reader", "Кодировка");
        } catch (FileNotFoundException e) {
            Log.d("reader", "Не найден файл");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("reader", "Неизвестная ошибка");
            e.printStackTrace();
        }

    }

    private boolean existsList(String plod) {
        for (String pl : lists) {
            if (pl.equals(plod))
                return true;
        }
        return false;
    }

    public ArrayList<String> getList() {
        lists = new ArrayList<>();
        if (plods.size() > 0) {
            lists.add(plods.get(0));
            for (String plod : plods) {
                if (!existsList(plod)) {
                    lists.add(plod);
                }
            }
        }
        return lists;
    }

    public void setString(String plod) {
        pointer = lists.indexOf(plod);
        ScannerActivity.plodFileName = pointer;
        setPlod();
    }

    public void setPlod() {
        String s = lists.get(pointer);
        ArrayList<ArrayList<String>> list1 = new ArrayList<>();
        ArrayList<String> texts1 = new ArrayList<>();
        ArrayList<String> ean131 = new ArrayList<>();
        ArrayList<String> marks1 = new ArrayList<>();
        ArrayList<String> ltInMarks1 = new ArrayList<>();
        ArrayList<String> stInMarks1 = new ArrayList<>();
        ArrayList<String> multiplicity1 = new ArrayList<>();
        for (int i = 0; i < plods.size(); i++) {
            if (plods.get(i).equals(s)) {
                ean131.add(ean13.get(i));
                texts1.add(texts.get(i));
                marks1.add(marks.get(i));
                stInMarks1.add(stInMarks.get(i));
                ltInMarks1.add(ltInMarks.get(i));
                multiplicity1.add(multiplicity.get(i));
                list1.add(list.get(i));
            }

        }
        multiplicity = multiplicity1;
        list = list1;
        ltInMarks = ltInMarks1;
        stInMarks = stInMarks1;
        marks = marks1;
        texts = texts1;
        ean13 = ean131;
    }

    public String getText(int index) {
        return texts.get(index);
    }

    public ArrayList<Integer> getIndex(String ean) {
        ArrayList<Integer> indexes = new ArrayList<>();
        if (ean13 != null)
            for (int i = 0; i < ean13.size(); i++) {
                if (ean13.get(i).equals(ean)) {
                    indexes.add(i);
                }
            }
        else return null;
        return indexes;
    }

    public String getColumn(int index, int ind) {
        return list.get(index).get(ind);
    }

    public String getMart(int index) {
        return marks.get(index);
    }

    public String getLtMark(int index) {
        return ltInMarks.get(index);
    }

    public String getStMark(int index) {
        return stInMarks.get(index);
    }

    public String getMultiplicity(int index) {
        return multiplicity.get(index);
    }



}
