package chapter6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataLoader {

    public static int[] loadArray(String filename) throws IOException {
        ArrayList<Integer> temp = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                temp.add(Integer.parseInt(line));
            }
        }

        int[] arr = new int[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            arr[i] = temp.get(i);
        }

        return arr;
    }

    public static ArrayList<Integer> loadArrayList(String filename) throws IOException {
        ArrayList<Integer> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                list.add(Integer.parseInt(line));
            }
        }

        return list;
    }
}