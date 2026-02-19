package chapter6;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class ArrayVsArrayListLab {

    static final int K = 200_000; // random access count
    static final int A = 200_000; // append count
    static final int F = 20_000;  // insert-at-front count
    static final int R = 20_000;  // remove-from-front count
    static final int TRIALS = 5;

    public static void main(String[] args) throws IOException {

        // ── Part A: Load Data ────────────────────────────────────────────────
        int[] array = DataLoader.loadArray("numbers.txt");
        ArrayList<Integer> list = DataLoader.loadArrayList("numbers.txt");
        System.out.println("Loaded " + array.length + " numbers.");

        // ── Part B & C: Benchmark + write CSV ───────────────────────────────
        Random rng = new Random(42);

        // Storage for averages
        double[][] avgMs = new double[4][2]; // [operation][0=array, 1=list]

        try (PrintWriter csv = new PrintWriter(new FileWriter("results.csv"))) {
            csv.println("structure,operation,trial,time_ms,checksum");

            // ── Operation 1: Random Access Sum ───────────────────────────────────
            {
                int n = array.length;
                // Pre-generate random indices so index generation isn't timed
                int[] indices = new int[K];
                for (int i = 0; i < K; i++) indices[i] = rng.nextInt(n);

                // array
                double totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    long sum = 0;
                    long start = System.nanoTime();
                    for (int idx : indices) sum += array[idx];
                    long end = System.nanoTime();
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    csv.printf("array,random_access,%d,%.2f,%d%n", t, ms, sum);
                }
                avgMs[0][0] = totalMs / TRIALS;

                // arraylist
                totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    long sum = 0;
                    long start = System.nanoTime();
                    for (int idx : indices) sum += list.get(idx);
                    long end = System.nanoTime();
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    csv.printf("arraylist,random_access,%d,%.2f,%d%n", t, ms, sum);
                }
                avgMs[0][1] = totalMs / TRIALS;
            }

            // ── Operation 2: Append Elements ─────────────────────────────────────
            {
                // array
                double totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    int[] src = array.clone();
                    long start = System.nanoTime();
                    int[] enlarged = new int[src.length + A];
                    System.arraycopy(src, 0, enlarged, 0, src.length);
                    for (int i = 0; i < A; i++) enlarged[src.length + i] = i;
                    long end = System.nanoTime();
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    // checksum: sum last A elements to prevent dead-code elimination
                    long chk = 0;
                    for (int i = src.length; i < enlarged.length; i++) chk += enlarged[i];
                    csv.printf("array,append,%d,%.2f,%d%n", t, ms, chk);
                }
                avgMs[1][0] = totalMs / TRIALS;

                // arraylist
                totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    ArrayList<Integer> copy = new ArrayList<>(list);
                    long start = System.nanoTime();
                    for (int i = 0; i < A; i++) copy.add(i);
                    long end = System.nanoTime();
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    long chk = 0;
                    for (int i = list.size(); i < copy.size(); i++) chk += copy.get(i);
                    csv.printf("arraylist,append,%d,%.2f,%d%n", t, ms, chk);
                }
                avgMs[1][1] = totalMs / TRIALS;
            }

            // ── Operation 3: Insert at Front ──────────────────────────────────────
            {
                // array
                double totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    int[] src = array.clone();
                    long checksum = 0;
                    long start = System.nanoTime();
                    for (int i = 0; i < F; i++) {
                        int[] newArr = new int[src.length + 1];
                        System.arraycopy(src, 0, newArr, 1, src.length);
                        newArr[0] = i;
                        src = newArr;
                    }
                    long end = System.nanoTime();
                    checksum = src[0]; // prevent dead-code elimination
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    csv.printf("array,insert_front,%d,%.2f,%d%n", t, ms, checksum);
                }
                avgMs[2][0] = totalMs / TRIALS;

                // arraylist
                totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    ArrayList<Integer> copy = new ArrayList<>(list);
                    long start = System.nanoTime();
                    for (int i = 0; i < F; i++) copy.add(0, i);
                    long end = System.nanoTime();
                    long checksum = copy.get(0); // prevent dead-code elimination
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    csv.printf("arraylist,insert_front,%d,%.2f,%d%n", t, ms, checksum);
                }
                avgMs[2][1] = totalMs / TRIALS;
            }

            // ── Operation 4: Remove from Front ───────────────────────────────────
            {
                // array
                double totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    int[] src = array.clone();
                    long checksum = 0;
                    long start = System.nanoTime();
                    for (int i = 0; i < R; i++) {
                        int[] newArr = new int[src.length - 1];
                        System.arraycopy(src, 1, newArr, 0, newArr.length);
                        src = newArr;
                    }
                    long end = System.nanoTime();
                    checksum = src.length > 0 ? src[0] : 0;
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    csv.printf("array,remove_front,%d,%.2f,%d%n", t, ms, checksum);
                }
                avgMs[3][0] = totalMs / TRIALS;

                // arraylist
                totalMs = 0;
                for (int t = 1; t <= TRIALS; t++) {
                    ArrayList<Integer> copy = new ArrayList<>(list);
                    long start = System.nanoTime();
                    for (int i = 0; i < R; i++) copy.remove(0);
                    long end = System.nanoTime();
                    long checksum = copy.isEmpty() ? 0 : copy.get(0);
                    double ms = (end - start) / 1_000_000.0;
                    totalMs += ms;
                    csv.printf("arraylist,remove_front,%d,%.2f,%d%n", t, ms, checksum);
                }
                avgMs[3][1] = totalMs / TRIALS;
            }

        } // end try-with-resources (csv auto-closed)

        // ── Part C: Console Summary ───────────────────────────────────────────
        String[] opNames = {"random_access", "append", "insert_front", "remove_front"};
        for (int op = 0; op < opNames.length; op++) {
            double arrAvg  = avgMs[op][0];
            double listAvg = avgMs[op][1];
            String winner  = arrAvg <= listAvg ? "array" : "arraylist";
            System.out.printf(
                    "Operation: %-15s  array avg: %8.2f ms  arraylist avg: %8.2f ms  winner: %s%n",
                    opNames[op], arrAvg, listAvg, winner
            );
        }

        System.out.println("Results written to results.csv");
    }
}