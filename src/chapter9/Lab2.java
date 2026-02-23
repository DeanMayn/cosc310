package chapter9;

import java.util.Arrays;
import java.util.Random;

public class Lab2 {

    // --- standardized benchmark constants (do not change) ---
    private static final int WARMUP_OPS  = 15_000;
    private static final int MEASURE_OPS = 60_000;

    // number of trials to run per workload - odd so median is clean
    private static final int TRIALS  = 7;

    // how many items to pre-load before W2 steady-state ops
    private static final int PREFILL = 10_000;

    // fixed seed so results are the same every run
    private static final long SEED = 42L;


    // -------------------------------------------------------------------------
    // UTILITY: returns median value from an array of longs
    // -------------------------------------------------------------------------
    private static long median(long[] values) {
        long[] copy = Arrays.copyOf(values, values.length);
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }


    // =========================================================================
    // W1 - BULK FILL THEN DRAIN
    // Push/enqueue MEASURE_OPS items, then pop/dequeue all of them.
    // =========================================================================

    private static long[] benchW1Stack(Stack<Integer> stack) throws Exception {
        long[] times = new long[TRIALS];

        // warmup - runs twice so the JIT compiler has time to optimize
        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < WARMUP_OPS; i++) stack.push(i);
            while (!stack.isEmpty()) stack.pop();
        }

        for (int t = 0; t < TRIALS; t++) {
            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) stack.push(i);
            while (!stack.isEmpty()) checksum += stack.pop();

            long end = System.nanoTime();
            // divide by ops*2 because we timed both push AND pop
            times[t] = (end - start) / (MEASURE_OPS * 2);
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }

    private static long[] benchW1Queue(Queue<Integer> queue) throws Exception {
        long[] times = new long[TRIALS];

        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < WARMUP_OPS; i++) queue.enqueue(i);
            while (!queue.isEmpty()) queue.dequeue();
        }

        for (int t = 0; t < TRIALS; t++) {
            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) queue.enqueue(i);
            while (!queue.isEmpty()) checksum += queue.dequeue();

            long end = System.nanoTime();
            times[t] = (end - start) / (MEASURE_OPS * 2);
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }

    private static long[] benchW1PQ(PriorityQueue<Integer> pq) throws Exception {
        long[] times = new long[TRIALS];
        Random rand = new Random(SEED);

        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < WARMUP_OPS; i++) pq.enqueue(rand.nextInt(100), i);
            while (!pq.isEmpty()) pq.dequeue();
        }

        for (int t = 0; t < TRIALS; t++) {
            // reset seed each trial so every trial gets the same priority sequence
            rand = new Random(SEED);
            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) pq.enqueue(rand.nextInt(100), i);
            while (!pq.isEmpty()) checksum += pq.dequeue();

            long end = System.nanoTime();
            times[t] = (end - start) / (MEASURE_OPS * 2);
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }


    // =========================================================================
    // W2 - MIXED STEADY STATE
    // Prefill 10k items, then do MEASURE_OPS random ops:
    //   60% insert, 35% remove, 5% peek
    // =========================================================================

    private static long[] benchW2Stack(Stack<Integer> stack) throws Exception {
        long[] times = new long[TRIALS];
        Random rand = new Random(SEED);

        // warmup
        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < PREFILL; i++) stack.push(i);
            for (int i = 0; i < WARMUP_OPS; i++) {
                int roll = rand.nextInt(100);
                if      (roll < 60) { stack.push(i); }
                else if (roll < 95) { if (!stack.isEmpty()) stack.pop(); }
                else                { if (!stack.isEmpty()) stack.top(); }
            }
            while (!stack.isEmpty()) stack.pop();
        }

        for (int t = 0; t < TRIALS; t++) {
            rand = new Random(SEED);

            // prefill before measuring
            for (int i = 0; i < PREFILL; i++) stack.push(i);

            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) {
                int roll = rand.nextInt(100);
                if (roll < 60) {
                    stack.push(i);
                } else if (roll < 95) {
                    if (!stack.isEmpty()) checksum += stack.pop();
                } else {
                    if (!stack.isEmpty()) checksum += stack.top();
                }
            }

            long end = System.nanoTime();
            while (!stack.isEmpty()) stack.pop(); // drain so next trial starts clean
            times[t] = (end - start) / MEASURE_OPS;
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }

    private static long[] benchW2Queue(Queue<Integer> queue) throws Exception {
        long[] times = new long[TRIALS];
        Random rand = new Random(SEED);

        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < PREFILL; i++) queue.enqueue(i);
            for (int i = 0; i < WARMUP_OPS; i++) {
                int roll = rand.nextInt(100);
                if      (roll < 60) { queue.enqueue(i); }
                else if (roll < 95) { if (!queue.isEmpty()) queue.dequeue(); }
                else                { if (!queue.isEmpty()) queue.front(); }
            }
            while (!queue.isEmpty()) queue.dequeue();
        }

        for (int t = 0; t < TRIALS; t++) {
            rand = new Random(SEED);

            for (int i = 0; i < PREFILL; i++) queue.enqueue(i);

            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) {
                int roll = rand.nextInt(100);
                if (roll < 60) {
                    queue.enqueue(i);
                } else if (roll < 95) {
                    if (!queue.isEmpty()) checksum += queue.dequeue();
                } else {
                    if (!queue.isEmpty()) checksum += queue.front();
                }
            }

            long end = System.nanoTime();
            while (!queue.isEmpty()) queue.dequeue();
            times[t] = (end - start) / MEASURE_OPS;
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }

    private static long[] benchW2PQ(PriorityQueue<Integer> pq) throws Exception {
        long[] times = new long[TRIALS];
        Random rand = new Random(SEED);

        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < PREFILL; i++) pq.enqueue(rand.nextInt(100), i);
            for (int i = 0; i < WARMUP_OPS; i++) {
                int roll = rand.nextInt(100);
                if      (roll < 60) { pq.enqueue(rand.nextInt(100), i); }
                else if (roll < 95) { if (!pq.isEmpty()) pq.dequeue(); }
                else                { if (!pq.isEmpty()) pq.front(); }
            }
            while (!pq.isEmpty()) pq.dequeue();
        }

        for (int t = 0; t < TRIALS; t++) {
            rand = new Random(SEED);

            for (int i = 0; i < PREFILL; i++) pq.enqueue(rand.nextInt(100), i);

            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) {
                int roll = rand.nextInt(100);
                if (roll < 60) {
                    pq.enqueue(rand.nextInt(100), i);
                } else if (roll < 95) {
                    if (!pq.isEmpty()) checksum += pq.dequeue();
                } else {
                    if (!pq.isEmpty()) checksum += pq.front();
                }
            }

            long end = System.nanoTime();
            while (!pq.isEmpty()) pq.dequeue();
            times[t] = (end - start) / MEASURE_OPS;
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }


    // =========================================================================
    // W3 - SKEWED PRIORITIES (PriorityQueue only)
    // 90% of priorities in [0..10], 10% in [11..100000]
    // This tests how sorted structures handle clustering at the front
    // =========================================================================

    private static long[] benchW3PQ(PriorityQueue<Integer> pq) throws Exception {
        long[] times = new long[TRIALS];
        Random rand = new Random(SEED);

        // warmup
        for (int w = 0; w < 2; w++) {
            for (int i = 0; i < WARMUP_OPS; i++) {
                int pri = (rand.nextInt(100) < 90) ? rand.nextInt(11) : rand.nextInt(100000) + 11;
                pq.enqueue(pri, i);
            }
            while (!pq.isEmpty()) pq.dequeue();
        }

        for (int t = 0; t < TRIALS; t++) {
            rand = new Random(SEED);
            long checksum = 0;
            long start = System.nanoTime();

            for (int i = 0; i < MEASURE_OPS; i++) {
                int pri = (rand.nextInt(100) < 90) ? rand.nextInt(11) : rand.nextInt(100000) + 11;
                pq.enqueue(pri, i);
            }
            while (!pq.isEmpty()) checksum += pq.dequeue();

            long end = System.nanoTime();
            times[t] = (end - start) / (MEASURE_OPS * 2);
            System.out.println("    trial " + t + "  checksum=" + checksum);
        }
        return times;
    }


    // =========================================================================
    // MAIN - runs all benchmarks and prints median ns/op for each
    // =========================================================================
    public static void main(String[] args) throws Exception {

        long med;

        // --- STACKS ---
        System.out.println("\n========== STACK BENCHMARKS ==========");

        System.out.println("\n[W1] ArrayListStack - fill then drain");
        med = median(benchW1Stack(new ArrayListStack<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W1] DLinkedListStack - fill then drain");
        med = median(benchW1Stack(new DLinkedListStack<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] ArrayListStack - mixed steady state");
        med = median(benchW2Stack(new ArrayListStack<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] DLinkedListStack - mixed steady state");
        med = median(benchW2Stack(new DLinkedListStack<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);


        // --- QUEUES ---
        System.out.println("\n========== QUEUE BENCHMARKS ==========");

        System.out.println("\n[W1] ArrayListQueue - fill then drain");
        med = median(benchW1Queue(new ArrayListQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W1] DLinkedListQueue - fill then drain");
        med = median(benchW1Queue(new DLinkedListQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] ArrayListQueue - mixed steady state");
        med = median(benchW2Queue(new ArrayListQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] DLinkedListQueue - mixed steady state");
        med = median(benchW2Queue(new DLinkedListQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);


        // --- PRIORITY QUEUES ---
        System.out.println("\n========== PRIORITY QUEUE BENCHMARKS ==========");

        System.out.println("\n[W1] SortedArrayListPQ - fill then drain");
        med = median(benchW1PQ(new SortedArrayListPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W1] SortedDLinkedListPQ - fill then drain");
        med = median(benchW1PQ(new SortedDLinkedListPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W1] BinaryHeapPQ - fill then drain");
        med = median(benchW1PQ(new BinaryHeapPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] SortedArrayListPQ - mixed steady state");
        med = median(benchW2PQ(new SortedArrayListPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] SortedDLinkedListPQ - mixed steady state");
        med = median(benchW2PQ(new SortedDLinkedListPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W2] BinaryHeapPQ - mixed steady state");
        med = median(benchW2PQ(new BinaryHeapPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W3] SortedArrayListPQ - skewed priorities");
        med = median(benchW3PQ(new SortedArrayListPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W3] SortedDLinkedListPQ - skewed priorities");
        med = median(benchW3PQ(new SortedDLinkedListPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n[W3] BinaryHeapPQ - skewed priorities");
        med = median(benchW3PQ(new BinaryHeapPriorityQueue<>()));
        System.out.println("  >>> MEDIAN ns/op: " + med);

        System.out.println("\n========== DONE ==========");
    }
}