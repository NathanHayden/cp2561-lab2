import java.io.*;
import java.util.*;

public class PartA {
    private static final int[] SIZES = {1000, 10000, 100000};
    private static final int WARMUP_OPS = 100000;
    private static final int TIMED_OPS = 100000;
    private static Random random = new Random(42);
    private static long sink = 0;
    
    static class BenchmarkResult {
        String structure;
        String operation;
        long[] nanoPerOp;
        double[] ratios;
        String[] bigOs;
        
        BenchmarkResult(String structure, String operation) {
            this.structure = structure;
            this.operation = operation;
            this.nanoPerOp = new long[SIZES.length];
            this.ratios = new double[SIZES.length - 1];
            this.bigOs = new String[SIZES.length - 1];
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<BenchmarkResult> results = new ArrayList<>();
        
        // Benchmark Lists
        benchmarkArrayList(results);
        benchmarkLinkedList(results);
        benchmarkArrayDeque(results);
        
        // Benchmark Sets
        benchmarkHashSet(results);
        benchmarkLinkedHashSet(results);
        benchmarkTreeSet(results);
        
        // Benchmark Maps
        benchmarkHashMap(results);
        benchmarkLinkedHashMap(results);
        benchmarkTreeMap(results);
        
        // Benchmark PriorityQueue
        benchmarkPriorityQueue(results);
        
        // Print results
        printTable(results);
        writeCSV(results);
        
        // Print sink to prevent JIT optimization
        System.out.println("Sink value (to prevent optimization): " + sink);
    }
    
    private static void benchmarkArrayList(List<BenchmarkResult> results) {
        BenchmarkResult getResult = new BenchmarkResult("ArrayList", "get");
        BenchmarkResult addEndResult = new BenchmarkResult("ArrayList", "add-at-end");
        BenchmarkResult addFrontResult = new BenchmarkResult("ArrayList", "add-at-front");
        BenchmarkResult containsResult = new BenchmarkResult("ArrayList", "contains");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            ArrayList<Integer> list = createIntList(n);
            
            // GET
            warmup(() -> sink += list.get(random.nextInt(n)));
            getResult.nanoPerOp[i] = timeOperation(() -> sink += list.get(random.nextInt(n)));
            
            // ADD-AT-END
            ArrayList<Integer> list2 = new ArrayList<>();
            warmup(() -> { if (list2.add(random.nextInt())) sink++; });
            addEndResult.nanoPerOp[i] = timeOperation(() -> { if (list2.add(random.nextInt())) sink++; });
            
            // ADD-AT-FRONT
            ArrayList<Integer> list3 = new ArrayList<>();
            warmup(() -> { list3.add(0, random.nextInt()); sink++; });
            addFrontResult.nanoPerOp[i] = timeOperation(() -> { list3.add(0, random.nextInt()); sink++; });
            
            // CONTAINS
            warmup(() -> { if (list.contains(random.nextInt())) sink++; });
            containsResult.nanoPerOp[i] = timeOperation(() -> { if (list.contains(random.nextInt())) sink++; });
        }
        
        computeRatios(getResult);
        computeRatios(addEndResult);
        computeRatios(addFrontResult);
        computeRatios(containsResult);
        
        results.add(getResult);
        results.add(addEndResult);
        results.add(addFrontResult);
        results.add(containsResult);
    }
    
    private static void benchmarkLinkedList(List<BenchmarkResult> results) {
        BenchmarkResult getResult = new BenchmarkResult("LinkedList", "get");
        BenchmarkResult addEndResult = new BenchmarkResult("LinkedList", "add-at-end");
        BenchmarkResult addFrontResult = new BenchmarkResult("LinkedList", "add-at-front");
        BenchmarkResult containsResult = new BenchmarkResult("LinkedList", "contains");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            LinkedList<Integer> list = createIntLinkedList(n);
            
            // GET
            warmup(() -> sink += list.get(random.nextInt(n)));
            getResult.nanoPerOp[i] = timeOperation(() -> sink += list.get(random.nextInt(n)));
            
            // ADD-AT-END
            LinkedList<Integer> list2 = new LinkedList<>();
            warmup(() -> { if (list2.add(random.nextInt())) sink++; });
            addEndResult.nanoPerOp[i] = timeOperation(() -> { if (list2.add(random.nextInt())) sink++; });
            
            // ADD-AT-FRONT
            LinkedList<Integer> list3 = new LinkedList<>();
            warmup(() -> { list3.addFirst(random.nextInt()); sink++; });
            addFrontResult.nanoPerOp[i] = timeOperation(() -> { list3.addFirst(random.nextInt()); sink++; });
            
            // CONTAINS
            warmup(() -> { if (list.contains(random.nextInt())) sink++; });
            containsResult.nanoPerOp[i] = timeOperation(() -> { if (list.contains(random.nextInt())) sink++; });
        }
        
        computeRatios(getResult);
        computeRatios(addEndResult);
        computeRatios(addFrontResult);
        computeRatios(containsResult);
        
        results.add(getResult);
        results.add(addEndResult);
        results.add(addFrontResult);
        results.add(containsResult);
    }
    
    private static void benchmarkArrayDeque(List<BenchmarkResult> results) {
        BenchmarkResult addEndResult = new BenchmarkResult("ArrayDeque", "add-at-end");
        BenchmarkResult addFrontResult = new BenchmarkResult("ArrayDeque", "add-at-front");
        BenchmarkResult containsResult = new BenchmarkResult("ArrayDeque", "contains");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            
            // ADD-AT-END
            ArrayDeque<Integer> deque1 = new ArrayDeque<>();
            warmup(() -> { deque1.addLast(random.nextInt()); sink++; });
            addEndResult.nanoPerOp[i] = timeOperation(() -> { deque1.addLast(random.nextInt()); sink++; });
            
            // ADD-AT-FRONT
            ArrayDeque<Integer> deque2 = new ArrayDeque<>();
            warmup(() -> { deque2.addFirst(random.nextInt()); sink++; });
            addFrontResult.nanoPerOp[i] = timeOperation(() -> { deque2.addFirst(random.nextInt()); sink++; });
            
            // CONTAINS
            ArrayDeque<Integer> deque3 = createIntArrayDeque(n);
            warmup(() -> { if (deque3.contains(random.nextInt())) sink++; });
            containsResult.nanoPerOp[i] = timeOperation(() -> { if (deque3.contains(random.nextInt())) sink++; });
        }
        
        computeRatios(addEndResult);
        computeRatios(addFrontResult);
        computeRatios(containsResult);
        
        results.add(addEndResult);
        results.add(addFrontResult);
        results.add(containsResult);
    }
    
    private static void benchmarkHashSet(List<BenchmarkResult> results) {
        BenchmarkResult addResult = new BenchmarkResult("HashSet", "add");
        BenchmarkResult containsResult = new BenchmarkResult("HashSet", "contains");
        BenchmarkResult removeResult = new BenchmarkResult("HashSet", "remove");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] values = createIntegerArray(n);
            
            // ADD
            HashSet<Integer> set1 = new HashSet<>();
            warmup(() -> { if (set1.add(random.nextInt())) sink++; });
            addResult.nanoPerOp[i] = timeOperation(() -> { if (set1.add(random.nextInt())) sink++; });
            
            // CONTAINS
            HashSet<Integer> set2 = new HashSet<>(Arrays.asList(values));
            warmup(() -> { if (set2.contains(values[random.nextInt(n)])) sink++; });
            containsResult.nanoPerOp[i] = timeOperation(() -> { if (set2.contains(values[random.nextInt(n)])) sink++; });
            
            // REMOVE
            HashSet<Integer> set3 = new HashSet<>(Arrays.asList(values));
            warmup(() -> { if (!set3.isEmpty()) { int idx = random.nextInt(set3.size()); Integer val = (Integer) set3.toArray()[idx]; if (set3.remove(val)) sink++; } });
            removeResult.nanoPerOp[i] = timeOperation(() -> { if (!set3.isEmpty()) { int idx = random.nextInt(set3.size()); Integer val = (Integer) set3.toArray()[idx]; if (set3.remove(val)) sink++; } });
        }
        
        computeRatios(addResult);
        computeRatios(containsResult);
        computeRatios(removeResult);
        
        results.add(addResult);
        results.add(containsResult);
        results.add(removeResult);
    }
    
    private static void benchmarkLinkedHashSet(List<BenchmarkResult> results) {
        BenchmarkResult addResult = new BenchmarkResult("LinkedHashSet", "add");
        BenchmarkResult containsResult = new BenchmarkResult("LinkedHashSet", "contains");
        BenchmarkResult removeResult = new BenchmarkResult("LinkedHashSet", "remove");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] values = createIntegerArray(n);
            
            // ADD
            LinkedHashSet<Integer> set1 = new LinkedHashSet<>();
            warmup(() -> { if (set1.add(random.nextInt())) sink++; });
            addResult.nanoPerOp[i] = timeOperation(() -> { if (set1.add(random.nextInt())) sink++; });
            
            // CONTAINS
            LinkedHashSet<Integer> set2 = new LinkedHashSet<>(Arrays.asList(values));
            warmup(() -> { if (set2.contains(values[random.nextInt(n)])) sink++; });
            containsResult.nanoPerOp[i] = timeOperation(() -> { if (set2.contains(values[random.nextInt(n)])) sink++; });
            
            // REMOVE
            LinkedHashSet<Integer> set3 = new LinkedHashSet<>(Arrays.asList(values));
            warmup(() -> { if (!set3.isEmpty()) { Integer val = set3.iterator().next(); if (set3.remove(val)) sink++; } });
            removeResult.nanoPerOp[i] = timeOperation(() -> { if (!set3.isEmpty()) { Integer val = set3.iterator().next(); if (set3.remove(val)) sink++; } });
        }
        
        computeRatios(addResult);
        computeRatios(containsResult);
        computeRatios(removeResult);
        
        results.add(addResult);
        results.add(containsResult);
        results.add(removeResult);
    }
    
    private static void benchmarkTreeSet(List<BenchmarkResult> results) {
        BenchmarkResult addResult = new BenchmarkResult("TreeSet", "add");
        BenchmarkResult containsResult = new BenchmarkResult("TreeSet", "contains");
        BenchmarkResult removeResult = new BenchmarkResult("TreeSet", "remove");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] values = createIntegerArray(n);
            
            // ADD
            TreeSet<Integer> set1 = new TreeSet<>();
            warmup(() -> { if (set1.add(random.nextInt())) sink++; });
            addResult.nanoPerOp[i] = timeOperation(() -> { if (set1.add(random.nextInt())) sink++; });
            
            // CONTAINS
            TreeSet<Integer> set2 = new TreeSet<>(Arrays.asList(values));
            warmup(() -> { if (set2.contains(values[random.nextInt(n)])) sink++; });
            containsResult.nanoPerOp[i] = timeOperation(() -> { if (set2.contains(values[random.nextInt(n)])) sink++; });
            
            // REMOVE
            TreeSet<Integer> set3 = new TreeSet<>(Arrays.asList(values));
            warmup(() -> { if (!set3.isEmpty()) { if (set3.remove(set3.first())) sink++; } });
            removeResult.nanoPerOp[i] = timeOperation(() -> { if (!set3.isEmpty()) { if (set3.remove(set3.first())) sink++; } });
        }
        
        computeRatios(addResult);
        computeRatios(containsResult);
        computeRatios(removeResult);
        
        results.add(addResult);
        results.add(containsResult);
        results.add(removeResult);
    }
    
    private static void benchmarkHashMap(List<BenchmarkResult> results) {
        BenchmarkResult putResult = new BenchmarkResult("HashMap", "put");
        BenchmarkResult getResult = new BenchmarkResult("HashMap", "get");
        BenchmarkResult containsKeyResult = new BenchmarkResult("HashMap", "containsKey");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] keys = createIntegerArray(n);
            
            // PUT
            HashMap<Integer, Integer> map1 = new HashMap<>();
            warmup(() -> sink += (map1.put(random.nextInt(), random.nextInt()) == null ? 1 : 0));
            putResult.nanoPerOp[i] = timeOperation(() -> sink += (map1.put(random.nextInt(), random.nextInt()) == null ? 1 : 0));
            
            // GET
            HashMap<Integer, Integer> map2 = new HashMap<>();
            for (Integer k : keys) map2.put(k, k);
            warmup(() -> sink += (map2.get(keys[random.nextInt(n)]) != null ? 1 : 0));
            getResult.nanoPerOp[i] = timeOperation(() -> sink += (map2.get(keys[random.nextInt(n)]) != null ? 1 : 0));
            
            // CONTAINSKEY
            warmup(() -> { if (map2.containsKey(keys[random.nextInt(n)])) sink++; });
            containsKeyResult.nanoPerOp[i] = timeOperation(() -> { if (map2.containsKey(keys[random.nextInt(n)])) sink++; });
        }
        
        computeRatios(putResult);
        computeRatios(getResult);
        computeRatios(containsKeyResult);
        
        results.add(putResult);
        results.add(getResult);
        results.add(containsKeyResult);
    }
    
    private static void benchmarkLinkedHashMap(List<BenchmarkResult> results) {
        BenchmarkResult putResult = new BenchmarkResult("LinkedHashMap", "put");
        BenchmarkResult getResult = new BenchmarkResult("LinkedHashMap", "get");
        BenchmarkResult containsKeyResult = new BenchmarkResult("LinkedHashMap", "containsKey");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] keys = createIntegerArray(n);
            
            // PUT
            LinkedHashMap<Integer, Integer> map1 = new LinkedHashMap<>();
            warmup(() -> sink += (map1.put(random.nextInt(), random.nextInt()) == null ? 1 : 0));
            putResult.nanoPerOp[i] = timeOperation(() -> sink += (map1.put(random.nextInt(), random.nextInt()) == null ? 1 : 0));
            
            // GET
            LinkedHashMap<Integer, Integer> map2 = new LinkedHashMap<>();
            for (Integer k : keys) map2.put(k, k);
            warmup(() -> sink += (map2.get(keys[random.nextInt(n)]) != null ? 1 : 0));
            getResult.nanoPerOp[i] = timeOperation(() -> sink += (map2.get(keys[random.nextInt(n)]) != null ? 1 : 0));
            
            // CONTAINSKEY
            warmup(() -> { if (map2.containsKey(keys[random.nextInt(n)])) sink++; });
            containsKeyResult.nanoPerOp[i] = timeOperation(() -> { if (map2.containsKey(keys[random.nextInt(n)])) sink++; });
        }
        
        computeRatios(putResult);
        computeRatios(getResult);
        computeRatios(containsKeyResult);
        
        results.add(putResult);
        results.add(getResult);
        results.add(containsKeyResult);
    }
    
    private static void benchmarkTreeMap(List<BenchmarkResult> results) {
        BenchmarkResult putResult = new BenchmarkResult("TreeMap", "put");
        BenchmarkResult getResult = new BenchmarkResult("TreeMap", "get");
        BenchmarkResult containsKeyResult = new BenchmarkResult("TreeMap", "containsKey");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] keys = createIntegerArray(n);
            
            // PUT
            TreeMap<Integer, Integer> map1 = new TreeMap<>();
            warmup(() -> sink += (map1.put(random.nextInt(), random.nextInt()) == null ? 1 : 0));
            putResult.nanoPerOp[i] = timeOperation(() -> sink += (map1.put(random.nextInt(), random.nextInt()) == null ? 1 : 0));
            
            // GET
            TreeMap<Integer, Integer> map2 = new TreeMap<>();
            for (Integer k : keys) map2.put(k, k);
            warmup(() -> sink += (map2.get(keys[random.nextInt(n)]) != null ? 1 : 0));
            getResult.nanoPerOp[i] = timeOperation(() -> sink += (map2.get(keys[random.nextInt(n)]) != null ? 1 : 0));
            
            // CONTAINSKEY
            warmup(() -> { if (map2.containsKey(keys[random.nextInt(n)])) sink++; });
            containsKeyResult.nanoPerOp[i] = timeOperation(() -> { if (map2.containsKey(keys[random.nextInt(n)])) sink++; });
        }
        
        computeRatios(putResult);
        computeRatios(getResult);
        computeRatios(containsKeyResult);
        
        results.add(putResult);
        results.add(getResult);
        results.add(containsKeyResult);
    }
    
    private static void benchmarkPriorityQueue(List<BenchmarkResult> results) {
        BenchmarkResult offerResult = new BenchmarkResult("PriorityQueue", "offer");
        BenchmarkResult pollResult = new BenchmarkResult("PriorityQueue", "poll");
        BenchmarkResult peekResult = new BenchmarkResult("PriorityQueue", "peek");
        
        for (int i = 0; i < SIZES.length; i++) {
            int n = SIZES[i];
            Integer[] values = createIntegerArray(n);
            
            // OFFER
            PriorityQueue<Integer> pq1 = new PriorityQueue<>();
            warmup(() -> { if (pq1.offer(random.nextInt())) sink++; });
            offerResult.nanoPerOp[i] = timeOperation(() -> { if (pq1.offer(random.nextInt())) sink++; });
            
            // POLL
            PriorityQueue<Integer> pq2 = new PriorityQueue<>(Arrays.asList(values));
            warmup(() -> sink += (pq2.poll() != null ? 1 : 0));
            pollResult.nanoPerOp[i] = timeOperation(() -> sink += (pq2.poll() != null ? 1 : 0));
            
            // PEEK
            PriorityQueue<Integer> pq3 = new PriorityQueue<>(Arrays.asList(values));
            warmup(() -> sink += (pq3.peek() != null ? 1 : 0));
            peekResult.nanoPerOp[i] = timeOperation(() -> sink += (pq3.peek() != null ? 1 : 0));
        }
        
        computeRatios(offerResult);
        computeRatios(pollResult);
        computeRatios(peekResult);
        
        results.add(offerResult);
        results.add(pollResult);
        results.add(peekResult);
    }
    
    private static void warmup(Runnable operation) {
        for (int i = 0; i < WARMUP_OPS; i++) {
            operation.run();
        }
    }
    
    private static long timeOperation(Runnable operation) {
        long start = System.nanoTime();
        for (int i = 0; i < TIMED_OPS; i++) {
            operation.run();
        }
        long end = System.nanoTime();
        return (end - start) / TIMED_OPS;
    }
    
    private static void computeRatios(BenchmarkResult result) {
        for (int i = 0; i < SIZES.length - 1; i++) {
            if (result.nanoPerOp[i] == 0) {
                result.ratios[i] = 1.0;
            } else {
                result.ratios[i] = (double) result.nanoPerOp[i + 1] / result.nanoPerOp[i];
            }
            
            // Guess Big-O
            if (result.ratios[i] < 1.5) {
                result.bigOs[i] = "O(1)";
            } else if (result.ratios[i] < 3.0) {
                result.bigOs[i] = "O(log n)";
            } else if (result.ratios[i] < 15.0) {
                result.bigOs[i] = "O(n)";
            } else {
                result.bigOs[i] = "O(n²)";
            }
        }
    }
    
    private static void printTable(List<BenchmarkResult> results) {
        System.out.println("\n" + "=".repeat(90));
        System.out.println("COLLECTION BENCHMARKS - Nanoseconds per Operation");
        System.out.println("=".repeat(90));
        System.out.printf("%-20s %-15s %12s %12s %12s %10s %10s\n",
            "Collection", "Operation", "1K", "10K", "100K", "1K-10K", "10K-100K");
        System.out.println("-".repeat(90));
        
        for (BenchmarkResult result : results) {
            System.out.printf("%-20s %-15s %12d %12d %12d %10s %10s\n",
                result.structure,
                result.operation,
                result.nanoPerOp[0],
                result.nanoPerOp[1],
                result.nanoPerOp[2],
                String.format("%.2fx", result.ratios[0]),
                String.format("%.2fx", result.ratios[1]));
        }
        System.out.println("=".repeat(90));
    }
    
    private static void writeCSV(List<BenchmarkResult> results) throws IOException {
        new File("output").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter("output/timeA.csv"))) {
            writer.println("Collection,Operation,1K,10K,100K,1K-10K_Ratio,10K-100K_Ratio,1K-10K_BigO,10K-100K_BigO");
            
            for (BenchmarkResult result : results) {
                writer.printf("%s,%s,%d,%d,%d,%.2f,%.2f,%s,%s\n",
                    result.structure,
                    result.operation,
                    result.nanoPerOp[0],
                    result.nanoPerOp[1],
                    result.nanoPerOp[2],
                    result.ratios[0],
                    result.ratios[1],
                    result.bigOs[0],
                    result.bigOs[1]);
            }
        }
        System.out.println("\nResults written to output/timeA.csv");
    }
    
    // Helper methods to create collections
    private static ArrayList<Integer> createIntList(int n) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(random.nextInt());
        return list;
    }
    
    private static LinkedList<Integer> createIntLinkedList(int n) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < n; i++) list.add(random.nextInt());
        return list;
    }
    
    private static ArrayDeque<Integer> createIntArrayDeque(int n) {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < n; i++) deque.add(random.nextInt());
        return deque;
    }
    
    private static int[] createIntArray(int n) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = random.nextInt();
        return arr;
    }
    
    private static Integer[] createIntegerArray(int n) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) arr[i] = random.nextInt();
        return arr;
    }
}
