package solved.klotski;

public class KlotskiNodeTree {

    private int[] rootStatus;
    /**
     * 盘面编码
     */
    private KlotskiStatusCode klotskiStatusCode;
    /**
     * 队列
     */
    private KlotskiNode[] queue;
    /**
     * 队列头，尾索引
     */
    private int head, rear = 1;
    /**
     * 已生成的布局
     */
    private boolean[] generated;
    private boolean process = true;
    /**
     *
     * @param status 盘面
     */
    public KlotskiNodeTree(int[] status) {
        this.rootStatus = status;
    }

    public void BFS() {

        KlotskiNode root = new KlotskiNode();
        root.setStatus(rootStatus);

        klotskiStatusCode = new KlotskiStatusCode(root.getStatus());
        queue = new KlotskiNode[1024];
        generated = new boolean[klotskiStatusCode.getTotal() + 1];

        queue[head] = root;
        generated[klotskiStatusCode.statusCoding(root.getStatus())] = true;
        generated[klotskiStatusCode.mirrorSymmetryStatusCoding(root.getStatus())] = true;

        while (process && head != rear) {
            nextStep(queue[head++]);
            // 取模运算
            head &= queue.length - 1;
        }
    }

    /**
     * 生成子节点
     * @param parent
     */
    private void nextStep(KlotskiNode parent) {
        int[] status = parent.getStatus();
        // RP1：空格1的位置；RP2：空格2的位置，DRT：空格联立的方式，默认不联立
        int RP1 = 0, RP2 = 0, DRT = 0;
        for (int i = 0, j = 0, length = status.length; j < 2 && i < length; i++) {
            if (status[i] == 0) {
                RP1 = RP2;
                RP2 = i;
                j++;
            }
        }
        if (RP1 + 4 == RP2) {
            // 空格竖联立
            DRT = 1;
        } else if (RP1 + 1 == RP2 && KlotskiNodeUtil.COL[RP1] < 3) {
            // 空格横联立
            DRT = 2;
        }

        // 记录已判断的棋子
        boolean[] flag = new boolean[16];
        for (int i = 0, length = status.length; i < length; i++) {
            int intType = status[i], col = KlotskiNodeUtil.COL[i];
            if (flag[intType]) {
                continue;
            }
            char charType = KlotskiNodeUtil.TYPE[intType];
            switch (charType) {
                case KlotskiNodeUtil.T:
                    if (i + 8 == RP1 && DRT == 2) {
                        // 向下
                        move(parent, i, i + 4);
                    } else if (i - 4 == RP1 && DRT == 2) {
                        // 向上
                        move(parent, i, i - 4);
                    } else if (col < 2 && i + 2 == RP1 && DRT == 1) {
                        // 向右
                        move(parent, i, i + 1);
                    } else if (col > 0 && i - 1 == RP1 && DRT == 1) {
                        // 向左
                        move(parent, i, i - 1);
                    }
                    flag[intType] = true;
                    break;
                case KlotskiNodeUtil.H:
                    if (i + 4 == RP1 && DRT == 2) {
                        // 向下
                        move(parent, i, i + 4);
                    } else if (i - 4 == RP1 && DRT == 2) {
                        move(parent, i, i - 4);
                        // 向上
                    } else {
                        if (col < 2 && (i + 2 == RP1 || i + 2 == RP2)) {
                            // 向右
                            move(parent, i, i + 1);
                            if (DRT == 2) {
                                move(parent, i, RP1);
                            }
                        } else if (col > 0 && (i - 1 == RP1 || i - 1 == RP2)) {
                            // 向左
                            move(parent, i, i - 1);
                            if (DRT == 2) {
                                move(parent, i, RP1);
                            }
                        }
                    }
                    flag[intType] = true;
                    break;
                case KlotskiNodeUtil.V:
                    if (i + 8 == RP1 || i + 8 == RP2) {
                        // 向下
                        move(parent, i, i + 4);
                        if (DRT  == 1) {
                            move(parent, i, RP1);
                        }
                    } else if (i - 4 == RP1 || i - 4 == RP2) {
                        // 向上
                        move(parent, i, i - 4);
                        if (DRT  == 1) {
                            move(parent, i, RP1);
                        }
                    } else {
                        if (col < 3 && i + 1 == RP1 && DRT == 1) {
                            // 向右
                            move(parent, i, i + 1);
                        } else if (col > 0 && i - 1 == RP1 && DRT == 1) {
                            // 向左
                            move(parent, i, i - 1);
                        }
                    }
                    flag[intType] = true;
                    break;
                case KlotskiNodeUtil.S:
                    if (i + 4 == RP1 || i + 4 == RP2) {
                        if (DRT > 0) {
                            move(parent, i, RP1);
                            move(parent, i, RP2);
                            continue;
                        } else {
                            // 向下
                            move(parent, i, i + 4);
                        }
                    }
                    if (i - 4 == RP1 || i - 4 == RP2) {
                        if (DRT > 0) {
                            move(parent, i, RP1);
                            move(parent, i, RP2);
                            continue;
                        } else {
                            // 向上
                            move(parent, i, i - 4);
                        }
                    }
                    if (col < 3 && (i + 1 == RP1 || i + 1 == RP2)) {
                        if (DRT > 0) {
                            move(parent, i, RP1);
                            move(parent, i, RP2);
                            continue;
                        } else {
                            // 向右
                            move(parent, i, i + 1);
                        }
                    }
                    if (col > 0 && (i - 1 == RP1 || i - 1 == RP2)) {
                        if (DRT > 0) {
                            move(parent, i, RP1);
                            move(parent, i, RP2);
                        } else {
                            // 向左
                            move(parent, i, i - 1);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 移动棋子
     * @param parent 父节点
     * @param src
     * @param dest
     */
    private void move(KlotskiNode parent, int src, int dest) {
        int[] childStatus = new int[rootStatus.length];
        System.arraycopy(parent.getStatus(), 0, childStatus, 0, rootStatus.length);
        int intType = parent.getStatus()[src];
        char charType = KlotskiNodeUtil.TYPE[intType];
        switch (charType) {
            case KlotskiNodeUtil.T:
                childStatus[src] = childStatus[src + 1] = childStatus[src + 4] = childStatus[src + 5] = 0;
                childStatus[dest] = childStatus[dest + 1] = childStatus[dest + 4] = childStatus[dest + 5] = intType;
                break;
            case KlotskiNodeUtil.H:
                childStatus[src] = childStatus[src + 1] = 0;
                childStatus[dest] = childStatus[dest + 1] = intType;
                break;
            case KlotskiNodeUtil.V:
                childStatus[src] = childStatus[src + 4] = 0;
                childStatus[dest] = childStatus[dest + 4] = intType;
                break;
            case KlotskiNodeUtil.S:
                childStatus[src] = 0;
                childStatus[dest] = intType;
                break;
        }
        // 搜索完成
        if (childStatus[17] == 15 && childStatus[18] == 15) {
            int count = 0;
            System.out.println(count++);
            KlotskiNodeUtil.printStatus(childStatus);
            while (parent != null) {
                System.out.println(count++);
                KlotskiNodeUtil.printStatus(parent.getStatus());
                parent = parent.getParent();
            }
            System.out.format("队列数组长度%d 实际队列数组长度%d 队列数组利用率%f%%%n", queue.length, rear - head,
                    ((float) (rear - head) / queue.length) * 100);
            process = false;
            return;
        }
        int statusCode = klotskiStatusCode.statusCoding(childStatus);
        if (generated[statusCode]) {
            return;
        }
        int symmetryStatusCode = klotskiStatusCode.mirrorSymmetryStatusCoding(childStatus);
        if (generated[symmetryStatusCode]) {
            return;
        }
        KlotskiNode child = new KlotskiNode();
        child.setParent(parent);
        child.setStatus(childStatus);
        // 队列已满，扩容队列
        if ((rear + 1 & queue.length - 1) == head) {
            expansionQueue();
        }
        queue[rear++] = child;
        rear &= queue.length - 1;
        generated[statusCode] = true;
        generated[symmetryStatusCode] = true;
    }

    /**
     * 扩容队列
     */
    private void expansionQueue() {
        KlotskiNode[] klotskiNodes = new KlotskiNode[queue.length << 1];
        System.arraycopy(queue, 0, klotskiNodes, 0, rear);
        System.arraycopy(queue, head, klotskiNodes, klotskiNodes.length - queue.length + head, queue.length - head);
        head = klotskiNodes.length - queue.length + head;
        queue = klotskiNodes;
    }

}
