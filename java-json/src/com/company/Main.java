package com.company;

import javax.json.*;
import javax.json.stream.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Main {

    public static int maxLengthOfLabels(HashMap<Integer, String> labels){
        int max = 0;
        for (String value : labels.values()) {
            if(value.length() > max){
                max = value.length();
            }
        }
        return max;
    }

    public static int maxLengthOfStrings(JsonObject array, int length) {
        int result = 0;
        for (int i = 1; i < length; i++) {
            if (array.get(i + "").toString().length() > result) {
                result = array.get(i + "").toString().length();
            }
        }
        return result;
    }

    public static void formatedPrintOfLabel(String string, int length) {
        int spaces = length - string.length();
        for (int i = 0; i < spaces; i++) {
            System.out.print(" ");
        }
        System.out.print(string + ": ");
    }

    public static int getMin(JsonArray array) {
        int min = array.getInt(0);
        for (int i = 0; i < array.size(); i++) {
            if (array.getInt(i) < min) {
                min = array.getInt(i);
            }
        }
        return min;
    }

    public static int getMax(JsonArray array) {
        int max = array.getInt(0);
        for (int i = 0; i < array.size(); i++) {
            if (array.getInt(i) > max) {
                max = array.getInt(i);
            }
        }
        return max;
    }

    public static int getNumberOfStars(int value, int min, int step) {
        return (int) Math.ceil((value - min) / (double) step);
    }

    public static void printStars(int numberOfStars) {
        for (int j = 0; j < numberOfStars; j++) {
            System.out.print("*");
        }
        System.out.println();
    }

    public static void formatedPrintOfResult(int number){
        if(number < 1000){
            System.out.print(" ");
        }
        System.out.print(number + " ");
    }


    public static void printData(JsonArray results, JsonObject labels) {
        int formatNumber = maxLengthOfStrings(labels, results.size());
        int min = getMin(results) - 1;
        int max = getMax(results) + 1;
        int step = (max - min) / 10;
        for (int i = 0; i < results.size(); i++) {
            int numberOfStars = getNumberOfStars(results.getInt(i), min, step);
            formatedPrintOfLabel(labels.get((i + 1) + "").toString(), formatNumber);
            formatedPrintOfResult(results.getInt(i));
            printStars(numberOfStars);
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println("PRIEMERNÁ MZDA");
        URL url = new URL("https://data.statistics.sk/api/v2/dataset/np3101rr/SK04/2020/E_PRIEM_HR_MZDA/0?lang=sk");
        try (InputStream is = url.openStream(); JsonReader rdr = Json.createReader(is)) {
            JsonObject obj = rdr.readObject();
            JsonArray results = obj.getJsonArray("value");
            JsonObject labels = obj.getJsonObject("dimension").getJsonObject("np3101rr_dim1").getJsonObject("category").getJsonObject("label");
            printData(results, labels);
        }
        System.out.println();
        streamingApi();
    }

    public static void streamingApi() throws IOException {

        HashMap<Integer, String> labels = new HashMap<Integer, String>();
        ArrayList<Integer> results = new ArrayList<Integer>();

        URL url = new URL("https://data.statistics.sk/api/v2/dataset/np3101rr/SK04/2020/E_PRIEM_HR_MZDA/0?lang=sk");
        try (InputStream is = url.openStream();
             JsonParser parser = Json.createParser(is)) {
            while (parser.hasNext()) {
                JsonParser.Event e = parser.next();
                if (e == JsonParser.Event.KEY_NAME) {
                    switch (parser.getString()) {
                        case "value":
                            e = parser.next();
                            e = parser.next();
                            while (e == JsonParser.Event.VALUE_NUMBER) {
                                results.add(parser.getInt());
                                e = parser.next();
                            }
                            break;

                        case "dimension":
                            boolean findDim = true;
                            while (findDim) {
                                e = parser.next();
                                if (e == JsonParser.Event.KEY_NAME && parser.getString().equals("np3101rr_dim1")) {
                                    findDim = false;

                                    boolean findCategory = true;
                                    while (findCategory) {
                                        e = parser.next();
                                        if (e == JsonParser.Event.KEY_NAME && parser.getString().equals("category")) {
                                            findCategory = false;

                                            boolean findLabel = true;
                                            while (findLabel) {
                                                e = parser.next();
                                                if (e == JsonParser.Event.KEY_NAME && parser.getString().equals("label")) {
                                                    findLabel = false;

                                                    e = parser.next();
                                                    e = parser.next();
                                                    while (e != JsonParser.Event.END_OBJECT) {
                                                        int key = Integer.parseInt(parser.getString());
                                                        e = parser.next();
                                                        String value = parser.getString();
                                                        e = parser.next();
                                                        labels.put(key, value);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            break;
                    }
                }
            }
        }

        printResults(results, labels);
    }

    public static void printResults(ArrayList<Integer> results, HashMap<Integer,String> labels){
        int formatNumber = maxLengthOfLabels(labels);
        int min = Collections.min(results) - 1;
        int max = Collections.max(results) + 1;
        int step = (max - min) / 10;
        for(int i = 0; i < results.size(); i++){
            int numberOfStars = getNumberOfStars(results.get(i), min, step);
            formatedPrintOfLabel(labels.get(i+1), formatNumber);
            formatedPrintOfResult(results.get(i));
            printStars(numberOfStars);
        }
    }

}
