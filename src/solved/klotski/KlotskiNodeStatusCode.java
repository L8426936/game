package solved.klotski;

public class KlotskiNodeStatusCode {
    /**
     * i的二进制是第SEQUENCE[i]个有n个1的值
     */
    private final static int[] SEQUENCE = new int[3969];
    static {
        // i个1的值有count[i]个
        int[] count = new int[13];
        for (int i = 1, n = i, index = 0; i < SEQUENCE.length; i++, n = i, index = 0) {
            while (n > 0) {
                index++;
                // 移除最高位的1
                n &= (n - 1);
            }
            SEQUENCE[i] = ++count[index];
        }
        // System.out.println(Arrays.toString(count));
        // System.out.println(Arrays.toString(SEQUENCE));
    }

    /**
     * 权值表
     */
    private int[] horizontalWeight, verticalWeight, targetWeight = new int[13];

    /**
     * 可能的布局总数
     */
    private int total;

    public KlotskiNodeStatusCode(int[] status) {
        // 横将、竖将的数量
        int horizontalCount = 0, verticalCount = 0;
        for (int i = 0; i < status.length; i++) {
            if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[i]] == KlotskiNodeUtil.H) {
                horizontalCount++;
            } else if (KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[i]] == KlotskiNodeUtil.V) {
                verticalCount++;
            }
        }
        // 横将、竖将排列数量
        int horizontalTotal = SEQUENCE[KlotskiNodeUtil.maxValue(horizontalCount >> 1)] + 1,
                verticalTotal = SEQUENCE[KlotskiNodeUtil.maxValue(verticalCount >> 1)] + 1;
        horizontalWeight = new int[horizontalTotal];
        verticalWeight = new int[verticalTotal];

        int verticalBaseWeight = 15, horizontalBaseWeight = 15 * verticalTotal,
                targetBaseWeight = 15 * verticalTotal * horizontalTotal;
        for (int i = 0; i < verticalTotal; i++) {
            verticalWeight[i] = verticalBaseWeight * i;
        }
        for (int i = 0; i < horizontalTotal; i++) {
            horizontalWeight[i] = horizontalBaseWeight * i;
        }
        for (int i = 0; i < targetWeight.length; i++) {
            targetWeight[i] = targetBaseWeight * i;
        }
        // System.out.println(Arrays.toString(verticalWeight));
        // System.out.println(Arrays.toString(horizontalWeight));
        // System.out.println(Arrays.toString(targetWeight));
        total = targetBaseWeight * targetWeight.length;
        // System.out.format("%d %d%n", total, SEQUENCE[60] + verticalWeight[verticalWeight.length - 1]
        //         + horizontalWeight[horizontalWeight.length - 1] + targetWeight[targetWeight.length - 1]);
    }

    /**
     * 盘面编码
     * @param status 盘面
     * @return
     */
    public int statusCoding(int[] status) {
        int hashCode = 0, soldierIndex = -1, soldierSequence = 0,
                horizontalIndex = -1, horizontalSequence = 0,
                verticalIndex = -1, verticalSequence = 0;
        boolean[] flag = new boolean[16];
        for (int i = 0; i < status.length; i++) {
            int intType = status[i];
            int charType = KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[intType];
            if (charType == KlotskiNodeUtil.T) {
                if (!flag[intType]) {
                    // System.out.format("%d %d %d%n", i - KlotskiNodeUtil.ROW[i], KlotskiNodeUtil.BINARY[i - KlotskiNodeUtil.ROW[i]],
                    //         SEQUENCE[KlotskiNodeUtil.BINARY[i - KlotskiNodeUtil.ROW[i]]]);
                    hashCode += targetWeight[SEQUENCE[KlotskiNodeUtil.BINARY[i - KlotskiNodeUtil.ROW[i]]]];
                    flag[intType] = true;
                }
                continue;
            }
            if (KlotskiNodeUtil.COL[i] < 3 && KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[i + 1]] != KlotskiNodeUtil.T) {
                horizontalIndex++;
                if (charType == KlotskiNodeUtil.H) {
                    if (!flag[intType]) {
                        // System.out.format("%d %d%n", horizontalIndex, KlotskiNodeUtil.BINARY[horizontalIndex]);
                        horizontalSequence += KlotskiNodeUtil.BINARY[horizontalIndex];
                        flag[intType] = true;
                    }
                    continue;
                }
            }
            if (KlotskiNodeUtil.ROW[i] < 4
                    && KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[i + 4]] != KlotskiNodeUtil.T
                    && KlotskiNodeUtil.INT_TO_CHAR_BY_TYPE[status[i + 4]] != KlotskiNodeUtil.H) {
                verticalIndex++;
                if (charType == KlotskiNodeUtil.V) {
                    if (!flag[intType]) {
                        // System.out.format("%d %d%n", verticalIndex, KlotskiNodeUtil.BINARY[verticalIndex]);
                        verticalSequence += KlotskiNodeUtil.BINARY[verticalIndex];
                        flag[intType] = true;
                    }
                    continue;
                }
            }
            if (charType == KlotskiNodeUtil.E || charType == KlotskiNodeUtil.S) {
                soldierIndex++;
                if (charType == KlotskiNodeUtil.S) {
                    // System.out.format("%d %d%n", soldierIndex, KlotskiNodeUtil.BINARY[soldierIndex]);
                    soldierSequence += KlotskiNodeUtil.BINARY[soldierIndex];
                }
            }
        }
        // System.out.format("%d %d %d%n", horizontalSequence, verticalSequence, soldierSequence);
        // System.out.format("%d %d %d%n", SEQUENCE[horizontalSequence], SEQUENCE[verticalSequence], SEQUENCE[soldierSequence]);
        hashCode += horizontalWeight[SEQUENCE[horizontalSequence]]
                + verticalWeight[SEQUENCE[verticalSequence]] + SEQUENCE[soldierSequence];
        return hashCode;
    }

    /**
     * 对称盘面编码
     * @param status 盘面
     * @return
     */
    public int mirrorSymmetryStatusCoding(int[] status) {
        int[] mirrorSymmetryStatus = new int[status.length];
        for (int i = 0; i < status.length; i += 4) {
            mirrorSymmetryStatus[i] = status[i + 3];
            mirrorSymmetryStatus[i + 1] = status[i + 2];
            mirrorSymmetryStatus[i + 2] = status[i + 1];
            mirrorSymmetryStatus[i + 3] = status[i];
        }
        return statusCoding(mirrorSymmetryStatus);
    }

    public int getTotal() {
        return total;
    }
}
