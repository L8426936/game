package solved.evlover;

import solved.util.AVLTree;
import solved.util.Queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvloverNodeTree {

    private int layer;
    private int[] xClickPosition, yClickPosition, zClickPosition;
    private EvloverUtil evloverUtil;

    public EvloverNodeTree(int layer) {
        this.layer = layer;
        evloverUtil = new EvloverUtil(layer);
        xClickPosition = new int[3 * layer * (layer - 1) + 1];
        yClickPosition = new int[3 * layer * (layer - 1) + 1];
        zClickPosition = new int[3 * layer * (layer - 1) + 1];
        for (int index = 0, z = -(layer - 1); z <= layer - 1; z++) {
            for (int x = -(layer - 1); x <= layer - 1; x++) {
                for (int y = -(layer - 1); y <= layer - 1; y++) {
                    if (x + y + z == 0) {
                        xClickPosition[index] = x;
                        yClickPosition[index] = y;
                        zClickPosition[index++] = z;
                    }
                }
            }
        }
        // System.out.println(Arrays.toString(xClickPosition));
        // System.out.println(Arrays.toString(yClickPosition));
        // System.out.println(Arrays.toString(zClickPosition));
    }

    /**
     * 构建以status为根节点的搜索树
     *
     * @param status
     * @return
     */
    public List<List<EvloverNode>> buildTree(char[] status) {
        List<List<EvloverNode>> tree = new ArrayList<>();
        AVLTree<EvloverNode> avlTree = new AVLTree<>();

        EvloverNode rootEvloverNode = new EvloverNode();
        rootEvloverNode.setStatus(EvloverUtil.charsStatusToLongStatus(status));
        List<EvloverNode> treeLayer = new ArrayList<>(1);

        treeLayer.add(rootEvloverNode);
        tree.add(treeLayer);
        avlTree.put(rootEvloverNode.getStatus(), rootEvloverNode);

        int[] actions = {EvloverUtil.C, EvloverUtil.P, EvloverUtil.A};
        for (int i = 0; i < tree.size(); i++) {
            treeLayer = tree.get(i);
            List<EvloverNode> nextTreeLayer = new ArrayList<>();
            for (EvloverNode parentEvloverNode : treeLayer) {
                long parentStatus = parentEvloverNode.getStatus();
                for (int j = 0; j < xClickPosition.length; j++) {
                    int x = xClickPosition[j], y = yClickPosition[j], z = zClickPosition[j];
                    for (int action : actions) {
                        long childStatus = nextStatus(parentStatus, x, y, z, action);
                        if (avlTree.get(childStatus) == null) {
                            EvloverNode childEvloverNode = new EvloverNode();
                            childEvloverNode.setParent(parentEvloverNode);
                            childEvloverNode.setStatus(childStatus);
                            childEvloverNode.setX(x);
                            childEvloverNode.setY(y);
                            childEvloverNode.setZ(z);
                            childEvloverNode.setAction(action);
                            avlTree.put(childStatus, childEvloverNode);
                            nextTreeLayer.add(childEvloverNode);
                        }
                    }
                }
            }
            if (nextTreeLayer.size() > 0) {
                tree.add(nextTreeLayer);
            }
        }
        return tree;
    }

    /**
     * <p>广度优先搜索</p>
     * <p>BFS: breadth first get</p>
     *
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BFS(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.charsStatusToLongStatus(startStatus);
        long endLongStatus = EvloverUtil.charsStatusToLongStatus(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> queue = new Queue<>();
            AVLTree<EvloverNode> avlTree = new AVLTree<>();

            EvloverNode rootEvloverNode = new EvloverNode();
            rootEvloverNode.setStatus(startLongStatus);

            queue.offer(rootEvloverNode);
            avlTree.put(startLongStatus, rootEvloverNode);

            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!queue.isEmpty()) {
                EvloverNode parentEvloverNode = queue.poll();
                long parentStatus = parentEvloverNode.getStatus();
                for (int i = 0; i < xClickPosition.length; i++) {
                    int x = xClickPosition[i], y = yClickPosition[i], z = zClickPosition[i];
                    for (int action : actions) {
                        long childStatus = nextStatus(parentStatus, x, y, z, action);
                        if (avlTree.get(childStatus) == null) {
                            EvloverNode childEvloverNode = new EvloverNode();
                            childEvloverNode.setParent(parentEvloverNode);
                            childEvloverNode.setStatus(childStatus);
                            childEvloverNode.setX(x);
                            childEvloverNode.setY(y);
                            childEvloverNode.setZ(z);
                            childEvloverNode.setAction(action);
                            avlTree.put(childStatus, childEvloverNode);
                            queue.offer(childEvloverNode);

                            // 找到终点状态
                            if (childStatus == endLongStatus) {
                                System.out.format("treeSum:%d queueSize:%d%n", avlTree.size(), queue.size());
                                return BFSBuildPassPath(childEvloverNode);
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * <p>广度优先搜索，以每个节点和结束节点的共同对称去除对称节点</p>
     * <p>BFS: breadth first get</p>
     *
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BFSRemoveSymmetry(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.charsStatusToLongStatus(startStatus);
        long endLongStatus = EvloverUtil.charsStatusToLongStatus(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> queue = new Queue<>();
            AVLTree<EvloverNode> avlTree = new AVLTree<>();

            EvloverNode rootEvloverNode = new EvloverNode();
            rootEvloverNode.setStatus(startLongStatus);

            queue.offer(rootEvloverNode);
            avlTree.put(startLongStatus, rootEvloverNode);

            int endStatusAllSymmetryType = evloverUtil.allSymmetryType(endLongStatus);
            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!queue.isEmpty()) {
                EvloverNode parentEvloverNode = queue.poll();
                long parentStatus = parentEvloverNode.getStatus();
                int allCommonSymmetryType = evloverUtil.allSymmetryType(parentStatus) & endStatusAllSymmetryType;
                int mergeAllCommonSymmetryType = parentEvloverNode.getSymmetry() | allCommonSymmetryType;
                // 记录已经点击过的位置，对称位置无需再次点击
                long logClickIndex = 0;
                for (int i = 0; i < xClickPosition.length; i++) {
                    int x = xClickPosition[i], y = yClickPosition[i], z = zClickPosition[i];
                    int index = evloverUtil.index(x, y, z);
                    // 当前位置没有被点击过
                    if ((logClickIndex & EvloverUtil.BINARY_VALUE[index]) == 0) {
                        for (int action : actions) {
                            long childStatus = nextStatus(parentStatus, x, y, z, action);
                            if (avlTree.get(childStatus) == null) {

                                boolean add = true;
                                for (int symmetryType = 1; add && symmetryType <= mergeAllCommonSymmetryType; symmetryType <<= 1) {
                                    long symmetryStatus = evloverUtil.symmetryStatus(childStatus, symmetryType & mergeAllCommonSymmetryType);
                                    add = avlTree.get(symmetryStatus) == null;
                                }

                                EvloverNode childEvloverNode = new EvloverNode();
                                childEvloverNode.setParent(parentEvloverNode);
                                childEvloverNode.setStatus(childStatus);
                                childEvloverNode.setX(x);
                                childEvloverNode.setY(y);
                                childEvloverNode.setZ(z);
                                childEvloverNode.setAction(action);
                                childEvloverNode.setSymmetry(mergeAllCommonSymmetryType);
                                avlTree.put(childStatus, childEvloverNode);

                                if (add) {
                                    queue.offer(childEvloverNode);
                                    // 找到终点状态
                                    if (childStatus == endLongStatus) {
                                        System.out.format("treeSum:%d queueSize:%d%n", avlTree.size(), queue.size());
                                        return BFSBuildPassPath(childEvloverNode);
                                    }
                                }

                            }
                        }
                        logClickIndex |= EvloverUtil.BINARY_VALUE[index];
                        for (int symmetryType = 1; symmetryType <= allCommonSymmetryType; symmetryType <<= 1) {
                            // 记录对称的点击位置
                            int symmetryX = EvloverUtil.symmetryX(x, y, z, symmetryType & allCommonSymmetryType);
                            int symmetryY = EvloverUtil.symmetryY(x, y, z, symmetryType & allCommonSymmetryType);
                            int symmetryZ = EvloverUtil.symmetryZ(x, y, z, symmetryType & allCommonSymmetryType);
                            int symmetryIndex = evloverUtil.index(symmetryX, symmetryY, symmetryZ);
                            logClickIndex |= EvloverUtil.BINARY_VALUE[symmetryIndex];
                        }
                    }
                }
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * <p>双向广度优先搜索</p>
     * <p>BBFS: bidirectional breadth first get</p>
     *
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BBFS(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.charsStatusToLongStatus(startStatus);
        long endLongStatus = EvloverUtil.charsStatusToLongStatus(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> startQueue = new Queue<>();
            Queue<EvloverNode> endQueue = new Queue<>();
            AVLTree<EvloverNode> startAVLTree = new AVLTree<>();
            AVLTree<EvloverNode> endAVLTree = new AVLTree<>();

            EvloverNode startEvloverNode = new EvloverNode();
            startEvloverNode.setStatus(startLongStatus);
            startQueue.offer(startEvloverNode);
            startAVLTree.put(startLongStatus, startEvloverNode);

            EvloverNode endEvloverNode = new EvloverNode();
            endEvloverNode.setStatus(endLongStatus);
            endQueue.offer(endEvloverNode);
            endAVLTree.put(endLongStatus, endEvloverNode);

            Queue<EvloverNode> queue;
            AVLTree<EvloverNode> avlTree, checkAVLTree;
            int startIndex, borderIndex, offset;
            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!startQueue.isEmpty() || !endQueue.isEmpty()) {
                if (!startQueue.isEmpty() && startQueue.size() < endQueue.size()) {
                    queue = startQueue;
                    avlTree = startAVLTree;
                    checkAVLTree = endAVLTree;
                    startIndex = 0;
                    borderIndex = xClickPosition.length;
                    offset = 1;
                    actions[1] = EvloverUtil.C;
                    actions[2] = EvloverUtil.A;
                } else {
                    queue = endQueue;
                    avlTree = endAVLTree;
                    checkAVLTree = startAVLTree;
                    startIndex = xClickPosition.length - 1;
                    borderIndex = -1;
                    offset = -1;
                    actions[1] = EvloverUtil.A;
                    actions[2] = EvloverUtil.C;
                }
                for (int i = 0, j = queue.size(); i < j; i++) {
                    EvloverNode parentEvloverNode = queue.poll();
                    long parentStatus = parentEvloverNode.getStatus();
                    for (int k = startIndex; k != borderIndex; k += offset) {
                        int x = xClickPosition[k], y = yClickPosition[k], z = zClickPosition[k];
                        for (int action : actions) {
                            long childStatus = nextStatus(parentStatus, x, y, z, action);
                            if (avlTree.get(childStatus) == null) {
                                EvloverNode childEvloverNode = new EvloverNode();
                                childEvloverNode.setParent(parentEvloverNode);
                                childEvloverNode.setStatus(childStatus);
                                childEvloverNode.setX(x);
                                childEvloverNode.setY(y);
                                childEvloverNode.setZ(z);
                                childEvloverNode.setAction(action);
                                avlTree.put(childStatus, childEvloverNode);
                                queue.offer(childEvloverNode);

                                EvloverNode junctionEvloverNode = checkAVLTree.get(childStatus);
                                // 找到相同状态
                                if (junctionEvloverNode != null) {
                                    System.out.format("treeSum:%d queueSize:%d%n", startAVLTree.size() + endAVLTree.size(), startQueue.size() + endQueue.size());
                                    return BBFSBuildPassPath(childStatus, startAVLTree, endAVLTree);
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * <p>双向广度优先搜索，以每个节点和目标节点的共同对称去除对称节点</p>
     * <p>BBFS: bidirectional breadth first get</p>
     *
     * @param startStatus
     * @param endStatus
     * @return
     */
    public List<EvloverNode> BBFSRemoveSymmetry(char[] startStatus, char[] endStatus) {
        long startLongStatus = EvloverUtil.charsStatusToLongStatus(startStatus);
        long endLongStatus = EvloverUtil.charsStatusToLongStatus(endStatus);

        if (startLongStatus != endLongStatus && EvloverUtil.bitCount(startLongStatus) == EvloverUtil.bitCount(endLongStatus)) {
            Queue<EvloverNode> startQueue = new Queue<>();
            Queue<EvloverNode> endQueue = new Queue<>();
            AVLTree<EvloverNode> startAVLTree = new AVLTree<>();
            AVLTree<EvloverNode> endAVLTree = new AVLTree<>();

            EvloverNode startEvloverNode = new EvloverNode();
            startEvloverNode.setStatus(startLongStatus);
            startQueue.offer(startEvloverNode);
            startAVLTree.put(startLongStatus, startEvloverNode);

            EvloverNode endEvloverNode = new EvloverNode();
            endEvloverNode.setStatus(endLongStatus);
            endQueue.offer(endEvloverNode);
            endAVLTree.put(endLongStatus, endEvloverNode);

            int startStatusAllSymmetryType = evloverUtil.allSymmetryType(startLongStatus);
            int endStatusAllSymmetryType = evloverUtil.allSymmetryType(endLongStatus);

            Queue<EvloverNode> queue;
            AVLTree<EvloverNode> avlTree, checkAVLTree;
            int startIndex, borderIndex, offset, statusAllSymmetryType;
            int[] actions = {EvloverUtil.P, EvloverUtil.C, EvloverUtil.A};
            while (!startQueue.isEmpty() || !endQueue.isEmpty()) {
                if (!startQueue.isEmpty() && startQueue.size() < endQueue.size()) {
                    queue = startQueue;
                    avlTree = startAVLTree;
                    checkAVLTree = endAVLTree;
                    statusAllSymmetryType = endStatusAllSymmetryType;
                    startIndex = 0;
                    borderIndex = xClickPosition.length;
                    offset = 1;
                    actions[1] = EvloverUtil.C;
                    actions[2] = EvloverUtil.A;
                } else {
                    queue = endQueue;
                    avlTree = endAVLTree;
                    checkAVLTree = startAVLTree;
                    statusAllSymmetryType = startStatusAllSymmetryType;
                    startIndex = xClickPosition.length - 1;
                    borderIndex = -1;
                    offset = -1;
                    actions[1] = EvloverUtil.A;
                    actions[2] = EvloverUtil.C;
                }
                for (int i = 0, j = queue.size(); i < j; i++) {
                    EvloverNode parentEvloverNode = queue.poll();
                    long parentStatus = parentEvloverNode.getStatus();
                    int allCommonSymmetryType = evloverUtil.allSymmetryType(parentStatus) & statusAllSymmetryType;
                    int mergeAllCommonSymmetryType = parentEvloverNode.getSymmetry() | allCommonSymmetryType;
                    // 记录已经点击过的位置，对称位置无需再次点击
                    long logClickIndex = 0;
                    for (int k = startIndex; k != borderIndex; k += offset) {
                        int x = xClickPosition[k], y = yClickPosition[k], z = zClickPosition[k];
                        int index = evloverUtil.index(x, y, z);
                        // 当前位置没有被点击过
                        if ((logClickIndex & EvloverUtil.BINARY_VALUE[index]) == 0) {
                            for (int action : actions) {
                                long childStatus = nextStatus(parentStatus, x, y, z, action);
                                if (avlTree.get(childStatus) == null) {
                                    boolean add = true;
                                    for (int symmetryType = 1; add && symmetryType <= mergeAllCommonSymmetryType; symmetryType <<= 1) {
                                        long symmetryStatus = evloverUtil.symmetryStatus(childStatus, symmetryType & mergeAllCommonSymmetryType);
                                        add = avlTree.get(symmetryStatus) == null;
                                    }

                                    EvloverNode childEvloverNode = new EvloverNode();
                                    childEvloverNode.setParent(parentEvloverNode);
                                    childEvloverNode.setStatus(childStatus);
                                    childEvloverNode.setX(x);
                                    childEvloverNode.setY(y);
                                    childEvloverNode.setZ(z);
                                    childEvloverNode.setAction(action);
                                    childEvloverNode.setSymmetry(mergeAllCommonSymmetryType);
                                    avlTree.put(childStatus, childEvloverNode);

                                    if (add) {
                                        queue.offer(childEvloverNode);
                                        EvloverNode junctionEvloverNode = checkAVLTree.get(childStatus);
                                        // 找到相同状态
                                        if (junctionEvloverNode != null) {
                                            System.out.format("treeSum:%d queueSize:%d%n", startAVLTree.size() + endAVLTree.size(), startQueue.size() + endQueue.size());
                                            return BBFSBuildPassPath(childStatus, startAVLTree, endAVLTree);
                                        }
                                    }
                                }
                            }
                        }
                        logClickIndex |= EvloverUtil.BINARY_VALUE[index];
                        for (int symmetryType = 1; symmetryType <= allCommonSymmetryType; symmetryType <<= 1) {
                            // 记录对称的点击位置
                            int symmetryX = EvloverUtil.symmetryX(x, y, z, symmetryType & allCommonSymmetryType);
                            int symmetryY = EvloverUtil.symmetryY(x, y, z, symmetryType & allCommonSymmetryType);
                            int symmetryZ = EvloverUtil.symmetryZ(x, y, z, symmetryType & allCommonSymmetryType);
                            int symmetryIndex = evloverUtil.index(symmetryX, symmetryY, symmetryZ);
                            logClickIndex |= EvloverUtil.BINARY_VALUE[symmetryIndex];
                        }
                    }
                }
            }
        }
        return new ArrayList<>(0);
    }

    /**
     * @param parentStatus
     * @param x
     * @param y
     * @param z
     * @param action
     */
    private long nextStatus(long parentStatus, int x, int y, int z, int action) {
        long childStatus = 0;
        for (int xStart = x - 1, xEnd = x + 1; xStart <= xEnd; xStart++) {
            for (int yStart = y - 1, yEnd = y + 1; yStart <= yEnd; yStart++) {
                for (int zStart = z - 1, zEnd = z + 1; zStart <= zEnd; zStart++) {
                    if (xStart + yStart + zStart == 0) {
                        int index = evloverUtil.index(xStart, yStart, zStart);
                        long value = parentStatus & EvloverUtil.BINARY_VALUE[index];
                        if (value > 0) {
                            int swapX = x, swapY = y, swapZ = z;
                            switch (action) {
                                case EvloverUtil.P:
                                    swapX = (x << 1) - xStart;
                                    swapY = (y << 1) - yStart;
                                    swapZ = (z << 1) - zStart;
                                    break;
                                case EvloverUtil.C:
                                    swapX = -(y + zStart);
                                    swapY = -(z + xStart);
                                    swapZ = -(x + yStart);
                                    break;
                                case EvloverUtil.A:
                                    swapX = -(z + yStart);
                                    swapY = -(x + zStart);
                                    swapZ = -(y + xStart);
                                    break;
                                default:
                            }
                            index = evloverUtil.index(swapX, swapY, swapZ);
                            childStatus |= EvloverUtil.BINARY_VALUE[index];
                            parentStatus ^= value;
                        }
                    }
                }
            }
        }
        return childStatus | parentStatus;
    }

    private List<EvloverNode> BFSBuildPassPath(EvloverNode EvloverNode) {
        List<EvloverNode> passPath = new ArrayList<>();
        while (EvloverNode != null) {
            passPath.add(0, EvloverNode);
            EvloverNode = EvloverNode.getParent();
        }
        return rebuildPassPath(passPath);
    }

    private List<EvloverNode> BBFSBuildPassPath(long status, AVLTree<EvloverNode> startAVLTree, AVLTree<EvloverNode> endAVLTree) {
        List<EvloverNode> passPath = new ArrayList<>();

        EvloverNode junctionEvloverNode = startAVLTree.get(status);
        while (junctionEvloverNode != null) {
            passPath.add(0, junctionEvloverNode);
            junctionEvloverNode = junctionEvloverNode.getParent();
        }
        junctionEvloverNode = endAVLTree.get(status);
        while (junctionEvloverNode.getParent() != null) {
            EvloverNode parentEvloverNode = junctionEvloverNode.getParent();
            junctionEvloverNode.setStatus(parentEvloverNode.getStatus());
            junctionEvloverNode.setAction(EvloverUtil.reverseAction(junctionEvloverNode.getAction()));
            passPath.add(junctionEvloverNode);
            junctionEvloverNode = junctionEvloverNode.getParent();
        }
        return rebuildPassPath(passPath);
    }

    private List<EvloverNode> rebuildPassPath(List<EvloverNode> passPath) {
        for (int i = 1; i < passPath.size(); i++) {
            EvloverNode beforeEvloverNode = passPath.get(i - 1);
            EvloverNode nextEvloverNode = passPath.get(i);
            beforeEvloverNode.setX(nextEvloverNode.getX());
            beforeEvloverNode.setY(nextEvloverNode.getY());
            beforeEvloverNode.setZ(nextEvloverNode.getZ());
            beforeEvloverNode.setAction(nextEvloverNode.getAction());
        }
        EvloverNode endEvloverNode = passPath.get(passPath.size() - 1);
        endEvloverNode.setX(0);
        endEvloverNode.setY(0);
        endEvloverNode.setZ(0);
        endEvloverNode.setAction(0);
        return passPath;
    }

    /**
     * <p>打印通关路径</p>
     *
     * @param passPath
     */
    public void printlnPassPath(List<EvloverNode> passPath) {
        for (int i = 0; i < passPath.size(); i++) {
            EvloverNode EvloverNode = passPath.get(i);
            char[] status = EvloverUtil.longStatusToCharsStatus(layer, EvloverNode.getStatus());
            for (int index = 0, z = -layer; z <= layer; z++) {
                for (int col = Math.abs(z); col > 0; col--) {
                    System.out.print(' ');
                }
                /*
                 * 当前行点的个数
                 * h(z) = 2 * layer + 1 - |z|；-layer <= z <= layer
                 */
                for (int col = 2 * layer + 1 - Math.abs(z); col > 0; col--) {
                    if (index == evloverUtil.index(EvloverNode.getX(), EvloverNode.getY(), EvloverNode.getZ())) {
                        System.out.format("- ");
                    } else if (status[index] != '0') {
                        System.out.format("● ");
                    } else {
                        System.out.format("○ ");
                    }
                    index++;
                }
                System.out.println();
            }
            System.out.format("x:%d y:%d z:%d action:%c%n", EvloverNode.getX(), EvloverNode.getY(), EvloverNode.getZ(), EvloverUtil.actionIntToChar(EvloverNode.getAction()));
        }
    }

}
