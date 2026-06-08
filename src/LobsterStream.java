import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;

public class LobsterStream {

    // ---- one resting order ----
    static final class Order {
        final long id; final long price; int size; final int side;
        Order(long id, long price, int size, int side){ this.id=id; this.price=price; this.size=size; this.side=side; }
    }

    // ---- the order book ----
    final TreeMap<Long, ArrayDeque<Order>> bids = new TreeMap<>(Collections.reverseOrder());
    final TreeMap<Long, ArrayDeque<Order>> asks = new TreeMap<>();
    final HashMap<Long, Order> byId = new HashMap<>();
    final ArrayList<Long> liveIds = new ArrayList<>();

    long nextId = 1;
    long mid = 100_00;

    TreeMap<Long, ArrayDeque<Order>> side(int s){ return s == 1 ? bids : asks; }

    void submit(int side, long price, int size){
        Order o = new Order(nextId++, price, size, side);
        side(side).computeIfAbsent(price, k -> new ArrayDeque<>()).addLast(o);
        byId.put(o.id, o);
        liveIds.add(o.id);
    }

    void cancel(long id){
        Order o = byId.remove(id);
        if (o == null) return;
        ArrayDeque<Order> q = side(o.side).get(o.price);
        if (q != null){ q.remove(o); if (q.isEmpty()) side(o.side).remove(o.price); }
    }

    void execute(int aggressorSide, int size){
        TreeMap<Long, ArrayDeque<Order>> book = side(-aggressorSide);
        while (size > 0) {
            Map.Entry<Long, ArrayDeque<Order>> level = book.firstEntry();
            if (level == null) break;
            ArrayDeque<Order> q = level.getValue();
            while (size > 0 && !q.isEmpty()) {
                Order head = q.peekFirst();
                if (head == null) break;
                int take = Math.min(size, head.size);
                head.size -= take;
                size -= take;
                if (head.size == 0) {
                    q.removeFirst();
                    byId.remove(head.id);
                }
            }
            if (q.isEmpty()) book.remove(level.getKey());
        }
    }

    void step(ThreadLocalRandom rng){
        mid += rng.nextInt(-3, 4);
        double r = rng.nextDouble();
        if (r < 0.62 || liveIds.isEmpty()){
            int side  = rng.nextBoolean() ? 1 : -1;
            int depth = 0; while (rng.nextDouble() > 0.40 && depth < 40) depth++;
            long price = side == 1 ? mid - 100 - 100L*depth : mid + 100 + 100L*depth;
            int size  = 100 * (1 + (int)(rng.nextDouble() * 4));
            submit(side, price, size);
        } else if (r < 0.95){
            int idx = rng.nextInt(liveIds.size());
            long id = liveIds.get(idx);
            liveIds.set(idx, liveIds.get(liveIds.size() - 1));
            liveIds.remove(liveIds.size() - 1);
            cancel(id);
        } else {
            execute(rng.nextBoolean() ? 1 : -1, 100 * (1 + rng.nextInt(5)));
        }
    }

    static long usedBytes(){ Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }

    static void measureAndWrite(LobsterStream s, ThreadLocalRandom rng, PrintWriter csv, int restingTarget, long eventsSoFar) {
        final int TRIALS = 200;
        for (int i = 0; i < 50; i++) { s.bids.firstEntry(); s.asks.firstEntry(); }

        long sumSubmit = 0;
        for (int i = 0; i < TRIALS; i++) {
            int side = rng.nextBoolean() ? 1 : -1;
            long price = s.mid + rng.nextInt(-50, 51);
            int size = 100 * (1 + rng.nextInt(4));
            long t0 = System.nanoTime();
            s.submit(side, price, size);
            long t1 = System.nanoTime();
            sumSubmit += (t1 - t0);
            long createdId = s.nextId - 1;
            s.cancel(createdId);
        }
        double avgSubmit = sumSubmit / (double) TRIALS;

        long sumCancel = 0; int cancelsMeasured = 0;
        for (int i = 0; i < TRIALS; i++) {
            if (s.liveIds.isEmpty()) break;
            int idx = (s.liveIds.size() == 1) ? 0 : rng.nextInt(s.liveIds.size());
            long id = s.liveIds.get(idx);
            Order existing = s.byId.get(id);
            if (existing == null) continue;
            long price = existing.price; int sz = existing.size; int side = existing.side;
            long t0 = System.nanoTime();
            s.cancel(id);
            long t1 = System.nanoTime();
            sumCancel += (t1 - t0);
            cancelsMeasured++;
            s.submit(side, price, sz);
        }
        double avgCancel = cancelsMeasured == 0 ? 0.0 : sumCancel / (double) cancelsMeasured;

        long sumBest = 0;
        for (int i = 0; i < TRIALS; i++) {
            long t0 = System.nanoTime();
            s.bids.firstEntry();
            long t1 = System.nanoTime();
            sumBest += (t1 - t0);
        }
        double avgBest = sumBest / (double) TRIALS;

        double used = usedBytes();
        int resting = s.byId.size();
        double bytesPer = resting == 0 ? 0.0 : used / (double) resting;

        csv.printf(Locale.ROOT, "%d,%.0f,%.3f,%.3f,%.3f,%.3f,%d%n",
                restingTarget, used, bytesPer, avgSubmit, avgCancel, avgBest, eventsSoFar);
        csv.flush();
    }

    public static void main(String[] args) {
        double gb = args.length > 0 ? Double.parseDouble(args[0]) : 0.5;
        long target = (long)(gb * 1024 * 1024 * 1024);
        LobsterStream s = new LobsterStream();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        int[] samples = new int[] {1000, 2000, 5000, 10000, 20000, 50000, 100000};
        boolean[] recorded = new boolean[samples.length];

        new File("output").mkdirs();
        try (PrintWriter csv = new PrintWriter(new FileWriter("output/scaleC.csv"))) {
            csv.println("restingOrders,usedBytes,bytesPerOrder,submit_ns,cancel_ns,best_ns,events");

            long events = 0, t0 = System.nanoTime();
            while (usedBytes() < target) {
                s.step(rng);
                events++;
                if ((events & 0xFFFFFF) == 0) {
                    double secs = (System.nanoTime() - t0) / 1e9;
                    System.out.printf("events=%,dM  rate=%,.1fM/s  liveHeap=%,d MB  restingOrders=%,d%n",
                            events / 1_000_000, (events / 1e6) / secs, usedBytes() / 1_048_576, s.byId.size());
                }
                for (int i = 0; i < samples.length; i++) {
                    if (!recorded[i] && s.byId.size() >= samples[i]) {
                        recorded[i] = true;
                        measureAndWrite(s, rng, csv, samples[i], events);
                    }
                }
            }
            double secs = (System.nanoTime() - t0) / 1e9;
            System.out.printf("REACHED ~%.1f GB: processed %,d events in %.1fs (%,.1fM events/s), %,d resting orders%n",
                    gb, events, secs, (events / 1e6) / secs, s.byId.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}