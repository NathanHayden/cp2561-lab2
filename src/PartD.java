import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class PartD {

    // --- SINK TO PREVENT JIT OPTIMIZATION ---
    private static long sink = 0;

    // =========================================================================
    // 1. CUSTOM ARRAYLIST IMPLEMENTATION
    // =========================================================================
    public static class MyArrayList {
        private Object[] elements;
        private int size;

        public MyArrayList() {
            elements = new Object[10];
            size = 0;
        }

        public void add(Object e) {
            if (size == elements.length) {
                Object[] newElements = new Object[elements.length * 2];
                System.arraycopy(elements, 0, newElements, 0, elements.length);
                elements = newElements;
            }
            elements[size++] = e;
        }

        public Object get(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            return elements[index];
        }

        public boolean contains(Object o) {
            for (int i = 0; i < size; i++) {
                if (Objects.equals(elements[i], o)) {
                    return true;
                }
            }
            return false;
        }

        public int size() {
            return size;
        }
    }

    // =========================================================================
    // 2. CUSTOM HASHMAP IMPLEMENTATION
    // =========================================================================
    public static class MyHashMap {
        private static class Node {
            final Object key;
            Object value;
            Node next;

            Node(Object key, Object value, Node next) {
                this.key = key;
                this.value = value;
                this.next = next;
            }
        }

        private Node[] table;
        private int size;
        private final double loadFactor;
        private int threshold;

        public MyHashMap() {
            this.table = new Node[16];
            this.loadFactor = 0.75;
            this.threshold = (int) (16 * loadFactor);
            this.size = 0;
        }

        private int hash(Object key) {
            return key == null ? 0 : Math.abs(key.hashCode()) % table.length;
        }

        public void put(Object key, Object value) {
            int index = hash(key);
            Node head = table[index];
            Node current = head;
            
            while (current != null) {
                if (Objects.equals(current.key, key)) {
                    current.value = value;
                    return;
                }
                current = current.next;
            }

            Node newNode = new Node(key, value, head);
            table[index] = newNode;
            size++;

            if (size >= threshold) {
                resize();
            }
        }

        public Object get(Object key) {
            int index = hash(key);
            Node current = table[index];
            while (current != null) {
                if (Objects.equals(current.key, key)) {
                    return current.value;
                }
                current = current.next;
            }
            return null;
        }

        public boolean containsKey(Object key) {
            int index = hash(key);
            Node current = table[index];
            while (current != null) {
                if (Objects.equals(current.key, key)) {
                    return true;
                }
                current = current.next;
            }
            return false;
        }

        private void resize() {
            Node[] oldTable = table;
            table = new Node[oldTable.length * 2];
            threshold = (int) (table.length * loadFactor);
            size = 0;

            for (Node head : oldTable) {
                Node current = head;
                while (current != null) {
                    put(current.key, current.value);
                    current = current.next;
                }
            }
        }
    }

    // =========================================================================
    // 3. BENCHMARKING ENGINE
    // =========================================================================
    public static void main(String[] args) {
        int[] N_VALUES = {1000, 10000, 100000};
        int ops = 100000;

        // Results storage for console and CSV export
        // Matrix format: [Structure/Op Index][N Scale Index]
        double[][] readTimes = new double[4][3]; 
        double[][] writeTimes = new double[4][3];
        double[][] bytesPerElement = new double[4][3];

        String[] structures = {"MyArrayList", "ArrayList", "MyHashMap", "HashMap"};

        System.out.println("Starting Warmup (100,000 operations)...");
        runWarmup(ops);
        System.out.println("Warmup Complete.\nRunning Benchmarks...");

        for (int i = 0; i < N_VALUES.length; i++) {
            int n = N_VALUES[i];

            // --- 1. MyArrayList ---
            System.gc();
            long memoryBefore = getUsedMemory();
            long start = System.nanoTime();
            MyArrayList myAL = new MyArrayList();
            for (int k = 0; k < n; k++) {
                myAL.add(k);
            }
            writeTimes[0][i] = (System.nanoTime() - start) / 1_000_000.0;
            bytesPerElement[0][i] = (double) (getUsedMemory() - memoryBefore) / n;

            start = System.nanoTime();
            for (int j = 0; j < ops; j++) {
                sink += (int) myAL.get(j % n);
                if (myAL.contains(-1)) sink++; 
            }
            readTimes[0][i] = (System.nanoTime() - start) / 1_000_000.0;

            // --- 2. Standard ArrayList ---
            System.gc();
            memoryBefore = getUsedMemory();
            start = System.nanoTime();
            ArrayList<Object> standardAL = new ArrayList<>();
            for (int k = 0; k < n; k++) {
                standardAL.add(k);
            }
            writeTimes[1][i] = (System.nanoTime() - start) / 1_000_000.0;
            bytesPerElement[1][i] = (double) (getUsedMemory() - memoryBefore) / n;

            start = System.nanoTime();
            for (int j = 0; j < ops; j++) {
                sink += (int) standardAL.get(j % n);
                if (standardAL.contains(-1)) sink++;
            }
            readTimes[1][i] = (System.nanoTime() - start) / 1_000_000.0;

            // --- 3. MyHashMap ---
            System.gc();
            memoryBefore = getUsedMemory();
            start = System.nanoTime();
            MyHashMap myHM = new MyHashMap();
            for (int k = 0; k < n; k++) {
                myHM.put(k, k);
            }
            writeTimes[2][i] = (System.nanoTime() - start) / 1_000_000.0;
            bytesPerElement[2][i] = (double) (getUsedMemory() - memoryBefore) / n;

            start = System.nanoTime();
            for (int j = 0; j < ops; j++) {
                sink += (int) myHM.get(j % n);
                if (myHM.containsKey(-1)) sink++;
            }
            readTimes[2][i] = (System.nanoTime() - start) / 1_000_000.0;

            // --- 4. Standard HashMap ---
            System.gc();
            memoryBefore = getUsedMemory();
            start = System.nanoTime();
            HashMap<Object, Object> standardHM = new HashMap<>();
            for (int k = 0; k < n; k++) {
                standardHM.put(k, k);
            }
            writeTimes[3][i] = (System.nanoTime() - start) / 1_000_000.0;
            bytesPerElement[3][i] = (double) (getUsedMemory() - memoryBefore) / n;

            start = System.nanoTime();
            for (int j = 0; j < ops; j++) {
                sink += (int) standardHM.get(j % n);
                if (standardHM.containsKey(-1)) sink++;
            }
            readTimes[3][i] = (System.nanoTime() - start) / 1_000_000.0;
        }

        // Print Out Console Table
        printConsoleTable(structures, writeTimes, readTimes, bytesPerElement);

        // Export data to CSV
        exportToCSV(structures, writeTimes, readTimes, bytesPerElement);
    }

    private static void runWarmup(int ops) {
        MyArrayList warmAL = new MyArrayList();
        MyHashMap warmHM = new MyHashMap();
        for (int i = 0; i < 1000; i++) {
            warmAL.add(i);
            warmHM.put(i, i);
        }
        for (int j = 0; j < ops; j++) {
            sink += (int) warmAL.get(j % 1000);
            sink += (int) warmHM.get(j % 1000);
        }
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static void printConsoleTable(String[] structs, double[][] writes, double[][] reads, double[][] bytes) {
        System.out.println("\n" + "=".repeat(85));
        System.out.printf("%-15s | %-10s | %-12s | %-12s | %-12s | %-15s\n", 
                "Structure", "Operation", "1K (ms)", "10K (ms)", "100K (ms)", "Bytes/Element");
        System.out.println("-".repeat(85));

        for (int i = 0; i < structs.length; i++) {
            System.out.printf("%-15s | %-10s | %-12.4f | %-12.4f | %-12.4f | %-15.1f\n",
                    structs[i], "Write", writes[i][0], writes[i][1], writes[i][2], Math.max(0, bytes[i][2]));
            System.out.printf("%-15s | %-10s | %-12.4f | %-12.4f | %-12.4f | %-15s\n",
                    structs[i], "Read/Search", reads[i][0], reads[i][1], reads[i][2], "N/A");
            System.out.println("-".repeat(85));
        }
    }

    private static void exportToCSV(String[] structs, double[][] writes, double[][] reads, double[][] bytes) {
        File dir = new File("output");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter writer = new FileWriter("output/compareD.csv")) {
            writer.write("Structure,Operation,1K,10K,100K,BytesPerElement\n");
            for (int i = 0; i < structs.length; i++) {
                writer.write(String.format("%s,Write,%.4f,%.4f,%.4f,%.1f\n", 
                        structs[i], writes[i][0], writes[i][1], writes[i][2], Math.max(0, bytes[i][2])));
                writer.write(String.format("%s,Read/Search,%.4f,%.4f,%.4f,N/A\n", 
                        structs[i], reads[i][0], reads[i][1], reads[i][2]));
            }
            System.out.println("Results successfully written to output/compareD.csv");
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }
}