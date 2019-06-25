package solved.klotski;

public class KlotskiNodeTree {

    /**
     * 队列
     */
    private KlotskiNodeQueue klotskiNodeQueue = new KlotskiNodeQueue();
    private KlotskiNodeHashMap klotskiNodeHashMap;
    /**
     * 盘面编码类
     */
    private KlotskiNodeStatusCode klotskiNodeStatusCode;
    /**
     * 移动模式
     */
    private int moveMode;

    public KlotskiNodeTree(int[] status, int moveMode) {
        this.moveMode = moveMode;
        klotskiNodeStatusCode = new KlotskiNodeStatusCode(status);
        klotskiNodeHashMap = new KlotskiNodeHashMap(klotskiNodeStatusCode.getTotal());

        KlotskiNode root = new KlotskiNode();
        root.setStatus(status);
        klotskiNodeQueue.offer(root);

        klotskiNodeHashMap.put(klotskiNodeStatusCode.statusCoding(status), root);
        klotskiNodeHashMap.put(klotskiNodeStatusCode.mirrorSymmetricStatusCoding(status), root);
    }

    public KlotskiNode[] BFS() {
        while (!klotskiNodeQueue.isEmpty()) {
            KlotskiNode parent = klotskiNodeQueue.poll();
            int[] status = parent.getStatus();
            if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[17]] == KlotskiNodeUtil.T
                    && KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[18]] == KlotskiNodeUtil.T) {
                int count = 0;
                KlotskiNode klotskiNode = parent;
                while (klotskiNode.getParent() != null) {
                    count++;
                    klotskiNode = klotskiNode.getParent();
                }
                KlotskiNode[] klotskiNodes = new KlotskiNode[count];
                while (--count >= 0) {
                    klotskiNodes[count] = parent;
                    parent = parent.getParent();
                }
                return klotskiNodes;
            }
            nextStep(parent);
        }
        return null;
    }

    private void nextStep(KlotskiNode parent) {
        int[] status = parent.getStatus();
        int empty1Position = 0, empty2Position = 0, emptyCount = 0, doubleEmptyType = 0;
        for (int i = 0; i < status.length; i++) {
            if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[i]] == KlotskiNodeUtil.E) {
                empty1Position = empty2Position;
                empty2Position = i;
                if (++emptyCount >= 2) {
                    break;
                }
            }
        }
        if (KlotskiNodeUtil.COL[empty1Position] < 3 && empty1Position + 1 == empty2Position) {
            // 空格横向联立
            doubleEmptyType = 1;
        } else if (empty1Position + 4 == empty2Position) {
            // 空格纵向联立
            doubleEmptyType = 2;
        }
        int[] empty1Around = singleEmpty(parent, empty1Position);
        int[] empty2Around = singleEmpty(parent, empty2Position);
        switch (doubleEmptyType) {
            case 1:
                horizontalDoubleEmpty(parent, empty1Around, empty2Around, empty1Position, empty2Position);
                break;
            case 2:
                verticalDoubleEmpty(parent, empty1Around, empty2Around, empty1Position, empty2Position);
                break;
            default:
        }
    }

    private int[] singleEmpty(KlotskiNode parent, int emptyPosition) {
        // 记录空格四周
        int[] emptyAround = new int[4];
        int[] status = parent.getStatus();
        int typeIndex = 0, col = KlotskiNodeUtil.COL[emptyPosition], row = KlotskiNodeUtil.ROW[emptyPosition];
        // 空格左移
        if (col > 0) {
            switch (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[emptyPosition - 1]]) {
                case KlotskiNodeUtil.S:
                    move(parent, emptyPosition - 1, emptyPosition);
                    break;
                case KlotskiNodeUtil.H:
                    move(parent, emptyPosition - 2, emptyPosition - 1);
                    break;
                default:
            }
            emptyAround[typeIndex] = status[emptyPosition - 1];
        }
        typeIndex++;
        // 空格右移
        if (col < 3) {
            switch (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[emptyPosition + 1]]) {
                case KlotskiNodeUtil.S:
                case KlotskiNodeUtil.H:
                    move(parent, emptyPosition + 1, emptyPosition);
                    break;
                default:
            }
            emptyAround[typeIndex] = status[emptyPosition + 1];
        }
        typeIndex++;
        // 空格上移
        if (row > 0) {
            switch (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[emptyPosition - 4]]) {
                case KlotskiNodeUtil.S:
                    move(parent, emptyPosition - 4, emptyPosition);
                    break;
                case KlotskiNodeUtil.V:
                    move(parent, emptyPosition - 8, emptyPosition - 4);
                    break;
                default:
            }
            emptyAround[typeIndex] = status[emptyPosition - 4];
        }
        typeIndex++;
        // 空格下移
        if (row < 4) {
            switch (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[emptyPosition + 4]]) {
                case KlotskiNodeUtil.S:
                case KlotskiNodeUtil.V:
                    move(parent, emptyPosition + 4, emptyPosition);
                    break;
                default:
            }
            emptyAround[typeIndex] = status[emptyPosition + 4];
        }
        return emptyAround;
    }

    private void horizontalDoubleEmpty(KlotskiNode parent, int[] empty1Around, int[] empty2Around, int empty1Position, int empty2Position) {
        if (moveMode == KlotskiNodeUtil.RIGHT_ANGLE_TURN || moveMode == KlotskiNodeUtil.STRAIGHT) {
            if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty1Around[0]] == KlotskiNodeUtil.H) {
                move(parent, empty1Position - 2, empty1Position);
            } else if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty2Around[1]] == KlotskiNodeUtil.H) {
                move(parent, empty2Position + 1, empty1Position);
            }
            if (moveMode == KlotskiNodeUtil.RIGHT_ANGLE_TURN) {
                soldierMoveByDoubleEmpty(parent, empty1Around, empty1Position, empty2Position);
                soldierMoveByDoubleEmpty(parent, empty2Around, empty2Position, empty1Position);
            } else {
                if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty1Around[0]] == KlotskiNodeUtil.S) {
                    move(parent, empty1Position - 1, empty2Position);
                }
                if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty2Around[1]] == KlotskiNodeUtil.S) {
                    move(parent, empty2Position + 1, empty1Position);
                }
            }
        }
        if (empty1Around[2] != 0 && empty1Around[2] == empty2Around[2]) {
            switch (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty1Around[2]]) {
                case KlotskiNodeUtil.H:
                    move(parent, empty1Position - 4, empty1Position);
                    break;
                case KlotskiNodeUtil.T:
                    move(parent, empty1Position - 8, empty1Position - 4);
                    break;
                default:
            }
        }
        if (empty1Around[3] != 0 && empty1Around[3] == empty2Around[3]) {
            move(parent, empty1Position + 4, empty1Position);
        }
    }

    private void verticalDoubleEmpty(KlotskiNode parent, int[] empty1Around, int[] empty2Around, int empty1Position, int empty2Position) {
        if (moveMode == KlotskiNodeUtil.RIGHT_ANGLE_TURN || moveMode == KlotskiNodeUtil.STRAIGHT) {
            if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty1Around[2]] == KlotskiNodeUtil.V) {
                move(parent, empty1Position - 8, empty1Position);
            } else if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty2Around[3]] == KlotskiNodeUtil.V) {
                move(parent, empty2Position + 4, empty1Position);
            }
            if (moveMode == KlotskiNodeUtil.RIGHT_ANGLE_TURN) {
                soldierMoveByDoubleEmpty(parent, empty1Around, empty1Position, empty2Position);
                soldierMoveByDoubleEmpty(parent, empty2Around, empty2Position, empty1Position);
            } else {
                if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty1Around[2]] == KlotskiNodeUtil.S) {
                    move(parent, empty1Position - 4, empty2Position);
                }
                if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty2Around[3]] == KlotskiNodeUtil.S) {
                    move(parent, empty2Position + 4, empty1Position);
                }
            }
        }
        if (empty1Around[0] != 0 && empty1Around[0] == empty2Around[0]) {
            switch (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[empty1Around[0]]) {
                case KlotskiNodeUtil.V:
                    move(parent, empty1Position - 1, empty1Position);
                    break;
                case KlotskiNodeUtil.T:
                    move(parent, empty1Position - 2, empty1Position - 1);
                    break;
                default:
            }
        }
        if (empty1Around[1] != 0 && empty1Around[1] == empty2Around[1]) {
            move(parent, empty1Position + 1, empty1Position);
        }
    }

    private final static int[] DIRECTIONS_OFFSET = {-1, 1, -4, 4};
    private void soldierMoveByDoubleEmpty(KlotskiNode parent, int[] emptyAround, int src, int dest) {
        for (int i = 0; i < emptyAround.length; i++) {
            if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[emptyAround[i]] == KlotskiNodeUtil.S) {
                move(parent, src + DIRECTIONS_OFFSET[i], dest);
            }
        }
    }

    /**
     * 移动棋子
     * @param parent
     * @param src 棋子位置（占多格棋子，左上角）
     * @param dest 空格位置（联立空格，第一个空格）
     */
    private void move(KlotskiNode parent, int src, int dest) {
        int[] status = new int[parent.getStatus().length];
        System.arraycopy(parent.getStatus(), 0, status, 0, status.length);
        int intType = status[src];
        char charType = KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[intType];
        switch (charType) {
            case KlotskiNodeUtil.S:
                status[src] = 0;
                status[dest] = intType;
                break;
            case KlotskiNodeUtil.H:
                status[src] = status[src + 1] = 0;
                status[dest] = status[dest + 1] = intType;
                break;
            case KlotskiNodeUtil.V:
                status[src] = status[src + 4] = 0;
                status[dest] = status[dest + 4] = intType;
                break;
            case KlotskiNodeUtil.T:
                status[src] = status[src + 1] = status[src + 4] = status[src + 5] = 0;
                status[dest] = status[dest + 1] = status[dest + 4] = status[dest + 5] = intType;
                break;
            default:
        }
        int statusCode = klotskiNodeStatusCode.statusCoding(status);
        if (klotskiNodeHashMap.get(statusCode) == null) {
            int mirrorSymmetryStatusCode = klotskiNodeStatusCode.mirrorSymmetricStatusCoding(status);
            if (klotskiNodeHashMap.get(mirrorSymmetryStatusCode) == null) {
                KlotskiNode child = new KlotskiNode();
                child.setStatus(status);
                child.setParent(parent);
                child.setSrc(src);
                child.setDest(dest);
                klotskiNodeHashMap.put(statusCode, child);
                klotskiNodeHashMap.put(mirrorSymmetryStatusCode, child);
                klotskiNodeQueue.offer(child);
            }
        }
    }

    public int statusCoding(int[] status) {
        return klotskiNodeStatusCode.statusCoding(status);
    }
}
