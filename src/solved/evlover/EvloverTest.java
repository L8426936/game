package solved.evlover;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class EvloverTest {
    public static void main(String[] args) {
        int layer = 2, count = 8;
        playRandomStatus(layer, count);
    }

    private static void printlnAllStatus(int layer, int count) {
        List<char[]> allStatus = EvloverUtil.allStatus(layer, count);
        for (char[] status : allStatus) {
            EvloverUtil.printlnStatus(status);
            System.out.println();
        }
    }

    private static void printlnAllUniqueStatus(int layer, int count) {
        EvloverUtil evloverUtil = new EvloverUtil(layer);
        List<char[]> allUniqueStatus = evloverUtil.allUniqueStatus(count);
        for (char[] status : allUniqueStatus) {
            EvloverUtil.printlnStatus(status);
            System.out.println();
        }
    }

    private static void playRandomStatus(int layer, int count) {
        char[] startStatus = EvloverUtil.randomStatus(layer, count);
        char[] endStatus = EvloverUtil.randomStatus(layer, count);
        EvloverNodeTree evloverNodeTree = new EvloverNodeTree(layer);
        long startTime = System.currentTimeMillis();
        List<EvloverNode> passPath = evloverNodeTree.BBFSRemoveSymmetry(startStatus, endStatus);
        evloverNodeTree.printlnPassPath(passPath);
        System.out.format("搜索耗时:%d毫秒 步数:%d%n", System.currentTimeMillis() - startTime, passPath.size() - 1);
    }

    private static void maxStep(char[] status) {
        int layer = EvloverUtil.layer(status);
        EvloverNodeTree evloverNodeTree = new EvloverNodeTree(layer);
        List<List<EvloverNode>> tree = evloverNodeTree.buildTree(status);
        System.out.format("maxStep:%d%n", tree.size() - 1);
    }

    private static void allUniqueStatusMaxStepMultithreading(int layer, int count) {
        EvloverUtil evloverUtil = new EvloverUtil(layer);
        List<char[]> allUniqueStatus = evloverUtil.allUniqueStatus(count);
        System.out.format("allUniqueStatusSum:%d%n", allUniqueStatus.size());
        EvloverNodeTree evloverNodeTree = new EvloverNodeTree(layer);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        AtomicInteger finishStatusCount = new AtomicInteger();
        ConcurrentLinkedQueue<Integer> steps = new ConcurrentLinkedQueue<>();
        for (char[] status : allUniqueStatus) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    long currentTimeMillis = System.currentTimeMillis();
                    List<List<EvloverNode>> tree = evloverNodeTree.buildTree(status);
                    steps.offer(tree.size() - 1);
                    System.out.format("i:%d status:%s step:%d time:%dms%n", finishStatusCount.getAndIncrement(), String.valueOf(status), tree.size() - 1, System.currentTimeMillis() - currentTimeMillis);
                }
            });
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.MINUTES));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<Integer, Integer> stepCount = new HashMap<>();
        for (Integer step : steps) {
            Integer value = stepCount.get(step);
            stepCount.put(step, value == null ? 1 : ++value);
        }
        stepCount.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            System.out.format("layer:%d count:%d step:%d stepCount:%d%n", layer, count, entry.getKey(), entry.getValue());
        });
    }

}
