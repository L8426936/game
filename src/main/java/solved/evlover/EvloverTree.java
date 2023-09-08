package solved.evlover;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class EvloverTree {

    private int layer;
    private int[] clickIndex;
    private int[][] weight;
    private EvloverUtil evloverUtil;

    public EvloverTree(int layer) {
        this.layer = layer;
        evloverUtil = new EvloverUtil(layer);
        clickIndex = new int[3 * layer * (layer - 1) + 1];
        for (int i = 0, index = 0, z = -layer; z <= layer; z++) {
            for (int x = -layer; x <= layer; x++) {
                for (int y = -layer; y <= layer; y++) {
                    if (x + y + z == 0) {
                        if (-layer < x && x < layer && -layer < y && y < layer && -layer < z && z < layer) {
                            clickIndex[i++] = index;
                        }
                        index++;
                    }
                }
            }
        }
        // System.out.println(Arrays.toString(clickIndex));

        int count = 3 * layer * (layer + 1) + 1;
        weight = new int[count][count];
        for (int row = 0; row < count; row++) {
            char[] startStatus = EvloverUtil.longToArray(layer, 1L << row);
            for (int col = 0; col < count; col++) {
                if (row != col) {
                    char[] endStatus = EvloverUtil.longToArray(layer, 1L << col);
                    List<EvloverNode> evloverNodes = BFS(startStatus, endStatus, false);
                    if (evloverNodes != null) {
                        weight[row][col] = evloverNodes.size() - 1;
                    }
                }
            }
        }
    }

    /**
     * 构建以status为根节点的搜索树
     * @param status
     * @return
     */
    public List<List<EvloverNode>> buildTree(char[] status) {
        List<List<EvloverNode>> tree = new ArrayList<>();
        Map<Long, EvloverNode> map = new HashMap<>();

        EvloverNode rootNode = new EvloverNode();
        rootNode.setStatus(EvloverUtil.arrayToLong(status));
        List<EvloverNode> treeLayer = new ArrayList<>(1);

        treeLayer.add(rootNode);
        tree.add(treeLayer);
        map.put(rootNode.getStatus(), rootNode);

        int[] actions = {EvloverUtil.C, EvloverUtil.P, EvloverUtil.A};
        for (int i = 0; i < tree.size(); i++) {
            treeLayer = tree.get(i);
            List<EvloverNode> nextTreeLayer = new ArrayList<>();
            for (EvloverNode parentNode : treeLayer) {
                long parentStatus = parentNode.getStatus();
                for (int index : clickIndex) {
                    int x = evloverUtil.indexToX(index), y = evloverUtil.indexToY(index), z = evloverUtil.indexToZ(index);
                    for (int action : actions) {
                        long childStatus = nextStatus(parentStatus, x, y, z, action);
                        if (map.get(childStatus) == null) {
                            EvloverNode childNode = new EvloverNode();
                            childNode.setParent(parentNode);
                            childNode.setStatus(childStatus);
                            childNode.setX(x);
                            childNode.setY(y);
                            childNode.setZ(z);
                            childNode.setAction(action);
                            childNode.setStep(parentNode.getStep() + 1);
                            map.put(childStatus, childNode);
                            nextTreeLayer.add(childNode);
                        }
                    }
                }
            }
            if (!nextTreeLayer.isEmpty()) {
                tree.add(nextTreeLayer);
            }
        }
        return tree;
    }

    /**
     * <p>广度优先搜索</p>
     * <p>BFS: breadth first search</p>
     * @param startStatus
     * @param endStatus
     * @return
     */
    private List<EvloverNode> BFS(char[] startStatus, char[] endStatus, boolean printlnInfo) {
        long startLongStatus = EvloverUtil.arrayToLong(startStatus);
        long endLongStatus = EvloverUtil.arrayToLong(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> queue = new LinkedList<>();
            Map<Long, EvloverNode> map = new HashMap<>();

            EvloverNode rootNode = new EvloverNode();
            rootNode.setStatus(startLongStatus);

            queue.offer(rootNode);
            map.put(startLongStatus, rootNode);

            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!queue.isEmpty()) {
                EvloverNode parentNode = queue.poll();
                long parentStatus = parentNode.getStatus();
                for (int index : clickIndex) {
                    int x = evloverUtil.indexToX(index), y = evloverUtil.indexToY(index), z = evloverUtil.indexToZ(index);
                    for (int action : actions) {
                        long childStatus = nextStatus(parentStatus, x, y, z, action);
                        if (map.get(childStatus) == null) {
                            EvloverNode childNode = new EvloverNode();
                            childNode.setParent(parentNode);
                            childNode.setStatus(childStatus);
                            childNode.setX(x);
                            childNode.setY(y);
                            childNode.setZ(z);
                            childNode.setAction(action);
                            map.put(childStatus, childNode);
                            queue.offer(childNode);

                            // 找到终点状态
                            if (childStatus == endLongStatus) {
                                if (printlnInfo) {
                                    System.out.format("mapSize:%d queueSize:%d%n", map.size(), queue.size());
                                }
                                return BFSBuildPassPath(childNode);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<EvloverNode> BFS(char[] startStatus, char[] endStatus) {
        return BFS(startStatus, endStatus, true);
    }

    /**
     * <p>广度优先搜索，以每个节点和结束节点的共同对称去除对称节点</p>
     * <p>BFS: breadth first search</p>
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BFSRemoveSymmetry(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.arrayToLong(startStatus);
        long endLongStatus = EvloverUtil.arrayToLong(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> queue = new LinkedList<>();
            Map<Long, EvloverNode> map = new HashMap<>();

            EvloverNode rootNode = new EvloverNode();
            rootNode.setStatus(startLongStatus);

            queue.offer(rootNode);
            map.put(startLongStatus, rootNode);

            int endStatusSymmetry = evloverUtil.symmetry(endLongStatus);
            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!queue.isEmpty()) {
                EvloverNode parentNode = queue.poll();
                long parentStatus = parentNode.getStatus();
                int commonSymmetry = endStatusSymmetry > 0 ? evloverUtil.symmetry(parentStatus) & endStatusSymmetry : 0;
                for (int value : clickIndex) {
                    int x = evloverUtil.indexToX(value), y = evloverUtil.indexToY(value), z = evloverUtil.indexToZ(value);
                    for (int action : actions) {
                        long childStatus = nextStatus(parentStatus, x, y, z, action);
                        boolean add = map.get(childStatus) == null;
                        if (add) {
                            EvloverNode childNode = new EvloverNode();
                            childNode.setParent(parentNode);
                            childNode.setStatus(childStatus);
                            childNode.setX(x);
                            childNode.setY(y);
                            childNode.setZ(z);
                            childNode.setAction(action);

                            map.put(childStatus, childNode);

                            // 找到终点状态
                            if (childStatus == endLongStatus) {
                                System.out.format("mapSize:%d queueSize:%d%n", map.size(), queue.size());
                                return BFSBuildPassPath(childNode);
                            }
                        }

                        for (int symmetryType = 1; add && symmetryType <= commonSymmetry; symmetryType <<= 1) {
                            long symmetryStatus = evloverUtil.symmetryStatus(childStatus, symmetryType & commonSymmetry);
                            if (symmetryStatus != childStatus) {
                                add = map.get(symmetryStatus) == null;
                            }
                        }

                        if (add) {
                            queue.offer(map.get(childStatus));
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>双向广度优先搜索</p>
     * <p>BBFS: bidirectional breadth first search</p>
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BBFS(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.arrayToLong(startStatus);
        long endLongStatus = EvloverUtil.arrayToLong(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> startQueue = new LinkedList<>();
            Queue<EvloverNode> endQueue = new LinkedList<>();
            Map<Long, EvloverNode> startMap = new HashMap<>();
            Map<Long, EvloverNode> endMap = new HashMap<>();

            EvloverNode startNode = new EvloverNode();
            startNode.setStatus(startLongStatus);
            startQueue.offer(startNode);
            startMap.put(startLongStatus, startNode);

            EvloverNode endNode = new EvloverNode();
            endNode.setStatus(endLongStatus);
            endQueue.offer(endNode);
            endMap.put(endLongStatus, endNode);

            Queue<EvloverNode> queue;
            Map<Long, EvloverNode> map, checkMap;
            int startIndex, borderIndex, offset;
            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!startQueue.isEmpty() || !endQueue.isEmpty()) {
                if (!startQueue.isEmpty() && startQueue.size() < endQueue.size()) {
                    queue = startQueue;
                    map = startMap;
                    checkMap = endMap;
                    startIndex = 0;
                    borderIndex = clickIndex.length;
                    offset = 1;
                    actions[1] = EvloverUtil.C;
                    actions[2] = EvloverUtil.A;
                } else {
                    queue = endQueue;
                    map = endMap;
                    checkMap = startMap;
                    startIndex = clickIndex.length - 1;
                    borderIndex = -1;
                    offset = -1;
                    actions[1] = EvloverUtil.A;
                    actions[2] = EvloverUtil.C;
                }
                for (int i = 0, j = queue.size(); i < j; i++) {
                    EvloverNode parentNode = queue.poll();
                    long parentStatus = parentNode.getStatus();
                    for (int k = startIndex; k != borderIndex; k += offset) {
                        int x = evloverUtil.indexToX(clickIndex[k]), y = evloverUtil.indexToY(clickIndex[k]), z = evloverUtil.indexToZ(clickIndex[k]);
                        for (int action : actions) {
                            long childStatus = nextStatus(parentStatus, x, y, z, action);
                            if (map.get(childStatus) == null) {
                                EvloverNode childNode = new EvloverNode();
                                childNode.setParent(parentNode);
                                childNode.setStatus(childStatus);
                                childNode.setX(x);
                                childNode.setY(y);
                                childNode.setZ(z);
                                childNode.setAction(action);
                                map.put(childStatus, childNode);
                                queue.offer(childNode);

                                // 找到相同状态
                                if (checkMap.get(childStatus) != null) {
                                    System.out.format("mapSize:%d queueSize:%d%n", startMap.size() + endMap.size(), startQueue.size() + endQueue.size());
                                    return BBFSBuildPassPath(childStatus, startMap, endMap);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>有bug，可能无法找到最优解</p>
     * <p>双向广度优先搜索，以每个节点和目标节点的共同对称去除对称节点</p>
     * <p>BBFS: bidirectional breadth first search</p>
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BBFSRemoveSymmetry(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.arrayToLong(startStatus);
        long endLongStatus = EvloverUtil.arrayToLong(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> startQueue = new LinkedList<>();
            Queue<EvloverNode> endQueue = new LinkedList<>();
            Map<Long, EvloverNode> startMap = new HashMap<>();
            Map<Long, EvloverNode> endMap = new HashMap<>();

            EvloverNode startNode = new EvloverNode();
            startNode.setStatus(startLongStatus);
            startQueue.offer(startNode);
            startMap.put(startLongStatus, startNode);

            EvloverNode endNode = new EvloverNode();
            endNode.setStatus(endLongStatus);
            endQueue.offer(endNode);
            endMap.put(endLongStatus, endNode);

            int startStatusSymmetry = evloverUtil.symmetry(startLongStatus);
            int endStatusSymmetry = evloverUtil.symmetry(endLongStatus);

            Queue<EvloverNode> queue;
            Map<Long, EvloverNode> map, checkMap;
            int startIndex, borderIndex, offset, statusSymmetry;
            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!startQueue.isEmpty() || !endQueue.isEmpty()) {
                if (!startQueue.isEmpty() && startQueue.size() < endQueue.size()) {
                    queue = startQueue;
                    map = startMap;
                    checkMap = endMap;
                    statusSymmetry = endStatusSymmetry;
                    startIndex = 0;
                    borderIndex = clickIndex.length;
                    offset = 1;
                    actions[1] = EvloverUtil.C;
                    actions[2] = EvloverUtil.A;
                } else {
                    queue = endQueue;
                    map = endMap;
                    checkMap = startMap;
                    statusSymmetry = startStatusSymmetry;
                    startIndex = clickIndex.length - 1;
                    borderIndex = -1;
                    offset = -1;
                    actions[1] = EvloverUtil.A;
                    actions[2] = EvloverUtil.C;
                }
                for (int i = 0, j = queue.size(); i < j; i++) {
                    EvloverNode parentNode = queue.poll();
                    long parentStatus = parentNode.getStatus();
                    int commonSymmetry = statusSymmetry > 0 ? evloverUtil.symmetry(parentStatus) & statusSymmetry : 0;
                    for (int k = startIndex; k != borderIndex; k += offset) {
                        int x = evloverUtil.indexToX(clickIndex[k]), y = evloverUtil.indexToY(clickIndex[k]), z = evloverUtil.indexToZ(clickIndex[k]);
                        for (int action : actions) {
                            long childStatus = nextStatus(parentStatus, x, y, z, action);
                            boolean add = map.get(childStatus) == null;
                            if (add) {
                                EvloverNode childNode = new EvloverNode();
                                childNode.setParent(parentNode);
                                childNode.setStatus(childStatus);
                                childNode.setX(x);
                                childNode.setY(y);
                                childNode.setZ(z);
                                childNode.setAction(action);

                                map.put(childStatus, childNode);

                                // 找到相同状态
                                if (checkMap.get(childStatus) != null) {
                                    System.out.format("mapSize:%d queueSize:%d%n", startMap.size() + endMap.size(), startQueue.size() + endQueue.size());
                                    return BBFSBuildPassPath(childStatus, startMap, endMap);
                                }
                            }

                            for (int symmetryType = 1; add && symmetryType <= commonSymmetry; symmetryType <<= 1) {
                                long symmetryStatus = evloverUtil.symmetryStatus(childStatus, symmetryType & commonSymmetry);
                                if (symmetryStatus != childStatus) {
                                    add = map.get(symmetryStatus) == null;
                                }
                            }

                            if (add) {
                                queue.offer(map.get(childStatus));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>不一定能找到最优解，搜索时间，空间，取决N的取值</p>
     * @param startStatus
     * @param endStatus
     * @param N
     * @param ratio 每搜索一层，N *= ratio
     * @return
     */
    public List<EvloverNode> beamSearch(char[] startStatus, char[] endStatus, int N, float ratio) {
        long startLongStatus = EvloverUtil.arrayToLong(startStatus);
        long endLongStatus = EvloverUtil.arrayToLong(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> queue = new PriorityQueue<>(Comparator.comparingInt(EvloverNode::getScore));
            Map<Long, EvloverNode> map = new ConcurrentHashMap<>();
            AtomicBoolean keep = new AtomicBoolean(true);

            EvloverNode rootNode = new EvloverNode();
            rootNode.setStatus(startLongStatus);

            queue.offer(rootNode);

            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!queue.isEmpty()) {
                Queue<EvloverNode> treeLayer = new ConcurrentLinkedQueue<>();
                ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                for (int i = 0; !queue.isEmpty() && i < N; i++) {
                    EvloverNode parentNode = queue.poll();
                    service.execute(() -> {
                        if (keep.get()) {
                            long parentStatus = parentNode.getStatus();
                            for (int index : clickIndex) {
                                int x = evloverUtil.indexToX(index), y = evloverUtil.indexToY(index), z = evloverUtil.indexToZ(index);
                                for (int action : actions) {
                                    long childStatus = nextStatus(parentStatus, x, y, z, action);
                                    if (map.get(childStatus) == null) {
                                        EvloverNode childNode = new EvloverNode();
                                        map.put(childStatus, childNode);
                                        childNode.setParent(parentNode);
                                        childNode.setStatus(childStatus);
                                        childNode.setX(x);
                                        childNode.setY(y);
                                        childNode.setZ(z);
                                        childNode.setAction(action);
                                        childNode.setStep(parentNode.getStep() + 1);
                                        childNode.setScore(childNode.getStep() + score(childStatus ^ (childStatus & endLongStatus), endLongStatus ^ (childStatus & endLongStatus)));
                                        treeLayer.offer(childNode);

                                        // 找到终点状态
                                        if (childStatus == endLongStatus) {
                                            keep.set(false);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                service.shutdown();
                try {
                    while (!service.awaitTermination(1, TimeUnit.MINUTES));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!keep.get()) {
                    System.out.format("mapSize:%d queueSize:%d%n", map.size(), queue.size() + treeLayer.size());
                    return BFSBuildPassPath(map.get(endLongStatus));
                }
                queue.clear();
                queue.addAll(treeLayer);
                N *= ratio;
            }
        }
        return null;
    }

    /**
     * <p>不一定能找到最优解，搜索时间，空间，取决N的取值</p>
     * @param startStatus
     * @param endStatus
     * @param N
     * @param ratio 每搜索一层，N *= ratio
     * @return
     */
    public List<EvloverNode> bidirectionalBeamSearch(char[] startStatus, char[] endStatus, int N, float ratio) {
        long startLongStatus = EvloverUtil.arrayToLong(startStatus);
        long endLongStatus = EvloverUtil.arrayToLong(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> startQueue = new PriorityQueue<>(Comparator.comparingInt(EvloverNode::getScore));
            Queue<EvloverNode> endQueue = new PriorityQueue<>(Comparator.comparingInt(EvloverNode::getScore));
            Map<Long, EvloverNode> startMap = new ConcurrentHashMap<>();
            Map<Long, EvloverNode> endMap = new ConcurrentHashMap<>();
            AtomicLong junctionStatus = new AtomicLong();

            EvloverNode startNode = new EvloverNode();
            startNode.setStatus(startLongStatus);
            startQueue.offer(startNode);
            startMap.put(startLongStatus, startNode);

            EvloverNode endNode = new EvloverNode();
            endNode.setStatus(endLongStatus);
            endQueue.offer(endNode);
            endMap.put(endLongStatus, endNode);

            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!startQueue.isEmpty() || !endQueue.isEmpty()) {
                Queue<EvloverNode> queue;
                Map<Long, EvloverNode> map, checkMap;
                if (!startQueue.isEmpty() && startQueue.size() < endQueue.size()) {
                    queue = startQueue;
                    map = startMap;
                    checkMap = endMap;
                } else {
                    queue = endQueue;
                    map = endMap;
                    checkMap = startMap;
                }
                Queue<EvloverNode> treeLayer = new ConcurrentLinkedQueue<>();
                ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                for (int i = 0; !queue.isEmpty() && i < N; i++) {
                    EvloverNode parentNode = queue.poll();
                    service.execute(() -> {
                        if (junctionStatus.get() == 0) {
                            long parentStatus = parentNode.getStatus();
                            for (int index : clickIndex) {
                                int x = evloverUtil.indexToX(index), y = evloverUtil.indexToY(index), z = evloverUtil.indexToZ(index);
                                for (int action : actions) {
                                    long childStatus = nextStatus(parentStatus, x, y, z, action);
                                    if (map.get(childStatus) == null) {
                                        EvloverNode childNode = new EvloverNode();
                                        map.put(childStatus, childNode);
                                        childNode.setParent(parentNode);
                                        childNode.setStatus(childStatus);
                                        childNode.setX(x);
                                        childNode.setY(y);
                                        childNode.setZ(z);
                                        childNode.setAction(action);
                                        childNode.setStep(parentNode.getStep() + 1);
                                        if (map == startMap) {
                                            childNode.setScore(childNode.getStep() + score(childStatus ^ (childStatus & endLongStatus), endLongStatus ^ (childStatus & endLongStatus)));
                                        } else {
                                            childNode.setScore(childNode.getStep() + score(childStatus ^ (childStatus & startLongStatus), startLongStatus ^ (childStatus & startLongStatus)));
                                        }
                                        treeLayer.offer(childNode);

                                        // 找到相同状态
                                        if (checkMap.get(childStatus) != null) {
                                            junctionStatus.set(childStatus);
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                service.shutdown();
                try {
                    while (!service.awaitTermination(1, TimeUnit.MINUTES));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (junctionStatus.get() != 0) {
                    System.out.format("mapSize:%d queueSize:%d%n", startMap.size() + endMap.size(), startQueue.size() + endQueue.size() + treeLayer.size());
                    return BBFSBuildPassPath(junctionStatus.get(), startMap, endMap);
                }
                queue.clear();
                queue.addAll(treeLayer);
                N *= ratio;
            }
        }
        return null;
    }

    public int score(long currentStatus, long endStatus) {
        int count = EvloverUtil.bitCount(currentStatus);

        int[] currentStatusIndex = new int[count];
        for (int i = 0, j = 0; j < count; i++) {
            if ((currentStatus & (1L << i)) > 0) {
                currentStatusIndex[j++] = i;
            }
        }

        int[] endStatusIndex = new int[count];
        for (int i = 0, j = 0; j < count; i++) {
            if ((endStatus & (1L << i)) > 0) {
                endStatusIndex[j++] = i;
            }
        }

        int[][] weight = new int[count][count];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                weight[i][j] = -this.weight[currentStatusIndex[i]][endStatusIndex[j]];
            }
        }

        KM km = new KM();
        return -km.solve(weight);
    }

    /**
     * @param parentStatus
     * @param x
     * @param y
     * @param z
     * @param action
     */
    private long nextStatus(long parentStatus, int x, int y, int z, int action) {
        /*
         *  0 1
         * 2 3 4
         *  5 6
         */
        long childStatus = 0;
        switch (action) {
            case EvloverUtil.P:
                childStatus |= ((parentStatus >> evloverUtil.index(x, y + 1, z - 1)) & 1L) << evloverUtil.index(x, y - 1, z + 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x + 1, y, z - 1)) & 1L) << evloverUtil.index(x - 1, y, z + 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x - 1, y + 1, z)) & 1L) << evloverUtil.index(x + 1, y - 1, z);
                childStatus |= ((parentStatus >> evloverUtil.index(x + 1, y - 1, z)) & 1L) << evloverUtil.index(x - 1, y + 1, z);
                childStatus |= ((parentStatus >> evloverUtil.index(x - 1, y, z + 1)) & 1L) << evloverUtil.index(x + 1, y, z - 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x, y - 1, z + 1)) & 1L) << evloverUtil.index(x, y + 1, z - 1);
                break;
            case EvloverUtil.C:
                childStatus |= ((parentStatus >> evloverUtil.index(x, y + 1, z - 1)) & 1L) << evloverUtil.index(x + 1, y, z - 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x + 1, y, z - 1)) & 1L) << evloverUtil.index(x + 1, y - 1, z);
                childStatus |= ((parentStatus >> evloverUtil.index(x - 1, y + 1, z)) & 1L) << evloverUtil.index(x, y + 1, z - 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x + 1, y - 1, z)) & 1L) << evloverUtil.index(x, y - 1, z + 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x - 1, y, z + 1)) & 1L) << evloverUtil.index(x - 1, y + 1, z);
                childStatus |= ((parentStatus >> evloverUtil.index(x, y - 1, z + 1)) & 1L) << evloverUtil.index(x - 1, y, z + 1);
                break;
            case EvloverUtil.A:
                childStatus |= ((parentStatus >> evloverUtil.index(x, y + 1, z - 1)) & 1L) << evloverUtil.index(x - 1, y + 1, z);
                childStatus |= ((parentStatus >> evloverUtil.index(x + 1, y, z - 1)) & 1L) << evloverUtil.index(x, y + 1, z - 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x - 1, y + 1, z)) & 1L) << evloverUtil.index(x - 1, y, z + 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x + 1, y - 1, z)) & 1L) << evloverUtil.index(x + 1, y, z - 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x - 1, y, z + 1)) & 1L) << evloverUtil.index(x, y - 1, z + 1);
                childStatus |= ((parentStatus >> evloverUtil.index(x, y - 1, z + 1)) & 1L) << evloverUtil.index(x + 1, y - 1, z);
                break;
            default:
        }
        parentStatus ^= parentStatus & (1L << evloverUtil.index(x, y + 1, z - 1));
        parentStatus ^= parentStatus & (1L << evloverUtil.index(x + 1, y, z - 1));
        parentStatus ^= parentStatus & (1L << evloverUtil.index(x - 1, y + 1, z));
        parentStatus ^= parentStatus & (1L << evloverUtil.index(x + 1, y - 1, z));
        parentStatus ^= parentStatus & (1L << evloverUtil.index(x - 1, y, z + 1));
        parentStatus ^= parentStatus & (1L << evloverUtil.index(x, y - 1, z + 1));
        return childStatus | parentStatus;
    }

    private List<EvloverNode> BFSBuildPassPath(EvloverNode evloverNode) {
        List<EvloverNode> passPath = new LinkedList<>();
        while (evloverNode != null) {
            passPath.add(0, evloverNode);
            evloverNode = evloverNode.getParent();
        }
        return rebuildPassPath(passPath);
    }

    private List<EvloverNode> BBFSBuildPassPath(long junctionStatus, Map<Long, EvloverNode> startMap, Map<Long, EvloverNode> endMap) {
        List<EvloverNode> passPath = new LinkedList<>();

        EvloverNode junctionNode = startMap.get(junctionStatus);
        while (junctionNode != null) {
            passPath.add(0, junctionNode);
            junctionNode = junctionNode.getParent();
        }
        junctionNode = endMap.get(junctionStatus);
        while (junctionNode.getParent() != null) {
            EvloverNode parentNode = junctionNode.getParent();
            junctionNode.setStatus(parentNode.getStatus());
            junctionNode.setAction(EvloverUtil.reverseAction(junctionNode.getAction()));
            passPath.add(junctionNode);
            junctionNode = junctionNode.getParent();
        }
        return rebuildPassPath(passPath);
    }

    private List<EvloverNode> rebuildPassPath(List<EvloverNode> passPath) {
        for (int i = 1; i < passPath.size(); i++) {
            EvloverNode beforeNode = passPath.get(i - 1);
            EvloverNode nextNode = passPath.get(i);
            beforeNode.setX(nextNode.getX());
            beforeNode.setY(nextNode.getY());
            beforeNode.setZ(nextNode.getZ());
            beforeNode.setAction(nextNode.getAction());
        }
        EvloverNode endNode = passPath.get(passPath.size() - 1);
        endNode.setX(0);
        endNode.setY(0);
        endNode.setZ(0);
        endNode.setAction(0);
        return passPath;
    }

    /**
     * <p>打印通关路径</p>
     * @param passPath
     */
    public void printlnPassPath(List<EvloverNode> passPath) {
        for (int i = 0; i < passPath.size() - 1; i++) {
            EvloverNode evloverNode = passPath.get(i);
            char[] status = EvloverUtil.longToArray(layer, evloverNode.getStatus());
            for (int index = 0, z = -layer; z <= layer; z++) {
                for (int col = Math.abs(z); col > 0; col--) {
                    System.out.print(' ');
                }
                /*
                 * 当前行点的个数
                 * h(z) = 2 * layer + 1 - |z|；-layer <= z <= layer
                 */
                for (int col = 2 * layer + 1 - Math.abs(z); col > 0; col--) {
                    if (index == evloverUtil.index(evloverNode.getX(), evloverNode.getY(), evloverNode.getZ())) {
                        System.out.print('-');
                    } else if (status[index] != '0') {
                        System.out.print('●');
                    } else {
                        System.out.print('○');
                    }
                    if (col > 1) {
                        System.out.print(' ');
                    }
                    index++;
                }
                System.out.println();
            }
            System.out.format("x:%d y:%d z:%d action:%c%n", evloverNode.getX(), evloverNode.getY(), evloverNode.getZ(), EvloverUtil.actionIntToChar(evloverNode.getAction()));
        }
        EvloverUtil.printlnStatus(EvloverUtil.longToArray(layer, passPath.get(passPath.size() - 1).getStatus()));
    }

}
