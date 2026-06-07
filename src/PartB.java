import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

public class PartB {
    private static final int[] SIZES = {1000, 10000, 100000};

    static class StructureMemoryResult {
        String structure;
        int elements;
        double heapUsedMB;
        double bytesPerElement;

        StructureMemoryResult(String structure, int elements, double heapUsedMB, double bytesPerElement) {
            this.structure = structure;
            this.elements = elements;
            this.heapUsedMB = heapUsedMB;
            this.bytesPerElement = bytesPerElement;
        }
    }

    private static class MeasuredStructure<T> {
        T structure;
        long heapBytes;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        List<StructureMemoryResult> results = new ArrayList<>();
        measureStructures(results);
        printTable(results);
        writeCSV(results);
    }

    private static void measureStructures(List<StructureMemoryResult> results) throws InterruptedException {
        for (int size : SIZES) {
            results.add(measureArrayList(size));
            results.add(measureLinkedList(size));
            results.add(measureArrayDeque(size));
            results.add(measureHashSet(size));
            results.add(measureLinkedHashSet(size));
            results.add(measureTreeSet(size));
            results.add(measureHashMap(size));
            results.add(measureLinkedHashMap(size));
            results.add(measureTreeMap(size));
            results.add(measurePriorityQueue(size));
        }
    }

    private static StructureMemoryResult measureArrayList(int size) throws InterruptedException {
        MeasuredStructure<ArrayList<Integer>> measured = measureStructureMemory(() -> {
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(i);
            }
            return list;
        });

        long heapBytes = measured.heapBytes;
        ArrayList<Integer> list = measured.structure;
        StructureMemoryResult result = createResult("ArrayList", size, heapBytes);
        list.clear();
        list = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureLinkedList(int size) throws InterruptedException {
        MeasuredStructure<LinkedList<Integer>> measured = measureStructureMemory(() -> {
            LinkedList<Integer> list = new LinkedList<>();
            for (int i = 0; i < size; i++) {
                list.add(i);
            }
            return list;
        });

        long heapBytes = measured.heapBytes;
        LinkedList<Integer> list = measured.structure;
        StructureMemoryResult result = createResult("LinkedList", size, heapBytes);
        list.clear();
        list = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureArrayDeque(int size) throws InterruptedException {
        MeasuredStructure<ArrayDeque<Integer>> measured = measureStructureMemory(() -> {
            ArrayDeque<Integer> deque = new ArrayDeque<>();
            for (int i = 0; i < size; i++) {
                deque.add(i);
            }
            return deque;
        });

        long heapBytes = measured.heapBytes;
        ArrayDeque<Integer> deque = measured.structure;
        StructureMemoryResult result = createResult("ArrayDeque", size, heapBytes);
        deque.clear();
        deque = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureHashSet(int size) throws InterruptedException {
        MeasuredStructure<HashSet<Integer>> measured = measureStructureMemory(() -> {
            HashSet<Integer> set = new HashSet<>();
            for (int i = 0; i < size; i++) {
                set.add(i);
            }
            return set;
        });

        long heapBytes = measured.heapBytes;
        HashSet<Integer> set = measured.structure;
        StructureMemoryResult result = createResult("HashSet", size, heapBytes);
        set.clear();
        set = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureLinkedHashSet(int size) throws InterruptedException {
        MeasuredStructure<LinkedHashSet<Integer>> measured = measureStructureMemory(() -> {
            LinkedHashSet<Integer> set = new LinkedHashSet<>();
            for (int i = 0; i < size; i++) {
                set.add(i);
            }
            return set;
        });

        long heapBytes = measured.heapBytes;
        LinkedHashSet<Integer> set = measured.structure;
        StructureMemoryResult result = createResult("LinkedHashSet", size, heapBytes);
        set.clear();
        set = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureTreeSet(int size) throws InterruptedException {
        MeasuredStructure<TreeSet<Integer>> measured = measureStructureMemory(() -> {
            TreeSet<Integer> set = new TreeSet<>();
            for (int i = 0; i < size; i++) {
                set.add(i);
            }
            return set;
        });

        long heapBytes = measured.heapBytes;
        TreeSet<Integer> set = measured.structure;
        StructureMemoryResult result = createResult("TreeSet", size, heapBytes);
        set.clear();
        set = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureHashMap(int size) throws InterruptedException {
        MeasuredStructure<HashMap<Integer, Integer>> measured = measureStructureMemory(() -> {
            HashMap<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(i, i);
            }
            return map;
        });

        long heapBytes = measured.heapBytes;
        HashMap<Integer, Integer> map = measured.structure;
        StructureMemoryResult result = createResult("HashMap", size, heapBytes);
        map.clear();
        map = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureLinkedHashMap(int size) throws InterruptedException {
        MeasuredStructure<LinkedHashMap<Integer, Integer>> measured = measureStructureMemory(() -> {
            LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(i, i);
            }
            return map;
        });

        long heapBytes = measured.heapBytes;
        LinkedHashMap<Integer, Integer> map = measured.structure;
        StructureMemoryResult result = createResult("LinkedHashMap", size, heapBytes);
        map.clear();
        map = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measureTreeMap(int size) throws InterruptedException {
        MeasuredStructure<TreeMap<Integer, Integer>> measured = measureStructureMemory(() -> {
            TreeMap<Integer, Integer> map = new TreeMap<>();
            for (int i = 0; i < size; i++) {
                map.put(i, i);
            }
            return map;
        });

        long heapBytes = measured.heapBytes;
        TreeMap<Integer, Integer> map = measured.structure;
        StructureMemoryResult result = createResult("TreeMap", size, heapBytes);
        map.clear();
        map = null;
        performGC();
        return result;
    }

    private static StructureMemoryResult measurePriorityQueue(int size) throws InterruptedException {
        MeasuredStructure<PriorityQueue<Integer>> measured = measureStructureMemory(() -> {
            PriorityQueue<Integer> queue = new PriorityQueue<>();
            for (int i = 0; i < size; i++) {
                queue.add(i);
            }
            return queue;
        });

        long heapBytes = measured.heapBytes;
        PriorityQueue<Integer> queue = measured.structure;
        StructureMemoryResult result = createResult("PriorityQueue", size, heapBytes);
        queue.clear();
        queue = null;
        performGC();
        return result;
    }

    private static <T> MeasuredStructure<T> measureStructureMemory(Supplier<T> structureSupplier) throws InterruptedException {
        performGC();
        long beforeUsed = usedMemory();
        T structure = structureSupplier.get();
        performGC();
        long afterUsed = usedMemory();

        if (structure == null) {
            throw new IllegalStateException("Measured structure must not be null");
        }

        MeasuredStructure<T> measured = new MeasuredStructure<>();
        measured.structure = structure;
        measured.heapBytes = Math.max(0, afterUsed - beforeUsed);
        return measured;
    }

    private static StructureMemoryResult createResult(String structureName, int elements, long heapBytes) {
        double heapUsedMB = heapBytes / 1024.0 / 1024.0;
        double bytesPerElement = elements > 0 ? (double) heapBytes / elements : 0.0;
        return new StructureMemoryResult(structureName, elements, heapUsedMB, bytesPerElement);
    }

    private static void performGC() throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            runtime.gc();
            Thread.sleep(50);
        }
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static void printTable(List<StructureMemoryResult> results) {
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("MEMORY FOOTPRINT - Structure Size and Heap Usage");
        System.out.println("=".repeat(80));
        System.out.printf("%-18s %12s %14s %18s%n",
            "Structure", "Elements", "HeapUsedMB", "Bytes/Element");
        System.out.println("-".repeat(80));

        for (StructureMemoryResult result : results) {
            System.out.printf("%-18s %12d %14.6f %18.3f%n",
                result.structure,
                result.elements,
                result.heapUsedMB,
                result.bytesPerElement);
        }
        System.out.println("=".repeat(80));
    }

    private static void writeCSV(List<StructureMemoryResult> results) throws IOException {
        File classLocation;
        try {
            classLocation = new File(PartB.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            classLocation = new File(System.getProperty("user.dir"));
        }

        File projectRoot = classLocation;
        if (projectRoot.isFile()) {
            projectRoot = projectRoot.getParentFile();
        }
        if (projectRoot != null && "src".equals(projectRoot.getName())) {
            projectRoot = projectRoot.getParentFile();
        }
        if (projectRoot == null) {
            projectRoot = new File(System.getProperty("user.dir"));
        }

        File outputDir = new File(projectRoot, "output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File csvFile = new File(outputDir, "memoryB.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Structure,Elements,HeapUsedMB,BytesPerElement\n");
            for (StructureMemoryResult result : results) {
                writer.write(String.format("%s,%d,%.6f,%.3f\n",
                    result.structure,
                    result.elements,
                    result.heapUsedMB,
                    result.bytesPerElement));
            }
        }
        System.out.println("Results written to " + csvFile.getPath());
    }
}

