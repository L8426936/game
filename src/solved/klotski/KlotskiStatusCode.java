package solved.klotski;

/**
 * 盘面编码
 */
public class KlotskiStatusCode {

    /**
     * <p>组合序号表</p>
     * <p>数组下角标作为序号</p>
     * <p>下角标对应的数组值是第n个有m个1的序号</p>
     */
    private final static int[] SEQUENCE = new int[2048];
    static {
        // 记录0~11个1的序号各有多少个
        int[] count = new int[12];
        for (int i = 0, index = 0, n = i; i < SEQUENCE.length; i++, index = 0, n = i) {
            while (n > 0) {
                n &= (n - 1);
                index++;
            }
            SEQUENCE[i] = count[index];
            count[index]++;
        }
    }

    /**
     * 权值表
     * <p>horizontal weights: 横长方形权值表</p>
     * <p>vertical weights: 竖长方形权值表</p>
     * <p>target weights: 大正方形权值表</p>
     */
    private int[] HW, VW, TW;
    /**
     * 总编码节点数量
     */
    private int total;

    public KlotskiStatusCode(int[] status) {
        /*
         * horizontal count: 计数横长方形棋子
         * vertical count: 计数竖长方形棋子
         */
        int HC = 0, VC = 0;
        for (int i = 0; i < status.length; i++) {
            if (KlotskiNodeUtil.TYPE[status[i]] == KlotskiNodeUtil.H) {
                HC++;
            } else if (KlotskiNodeUtil.TYPE[status[i]] == KlotskiNodeUtil.V) {
                VC++;
            }
        }

        int HMaxSequence = KlotskiNodeUtil.maxCombinationNumber(KlotskiNodeUtil.BINARY_WEIGHTS[11], HC >> 1),
                VMaxSequence = KlotskiNodeUtil.maxCombinationNumber(KlotskiNodeUtil.BINARY_WEIGHTS[12 - HC], VC >> 1);
        TW = new int[12];
        HW = new int[SEQUENCE[HMaxSequence] + 1];
        VW = new int[SEQUENCE[VMaxSequence] + 1];
        /*
         * vertical base weights：竖将位权
         * vertical base weights：横将位权
         * vertical base weights：王位权
         */
        int VBW = 15, HBW = 15 * (SEQUENCE[VMaxSequence] + 1),
                TBW = 15 * (SEQUENCE[VMaxSequence] + 1) * (SEQUENCE[HMaxSequence] + 1);
        for (int i = 0; i < TW.length; i++) {
            TW[i] = i * TBW;
        }
        for (int i = 0; i < HW.length; i++) {
            HW[i] = i * HBW;
        }
        for (int i = 0; i < VW.length; i++) {
            VW[i] = i * VBW;
        }
        total = TBW * 12;
    }

    /**
     * 盘面编码值
     * @param status
     * @return
     */
    public int statusCoding(int[] status) {
        // 各类棋子的组合序号
        int SSequence = 0, SIndex = -1, VSequence = 0, VIndex = -1, HSequence = 0, HIndex = -1, TIndex = 0;
        boolean[] flag = new boolean[16];
        for (int i = 0; i < status.length; i++) {
            int intType = status[i];
            char charType = KlotskiNodeUtil.TYPE[intType];
            if (charType == KlotskiNodeUtil.T) {
                if (!flag[intType]) {
                    TIndex += i - KlotskiNodeUtil.ROW[i];
                    flag[intType] = true;
                }
                continue;
            }
            if (KlotskiNodeUtil.COL[i] < 3 && KlotskiNodeUtil.TYPE[status[i + 1]] != KlotskiNodeUtil.T) {
                HIndex++;
            }
            if (charType == KlotskiNodeUtil.H) {
                if (!flag[intType]) {
                    HSequence += KlotskiNodeUtil.BINARY_WEIGHTS[HIndex];
                    flag[intType] = true;
                }
                continue;
            }
            if (KlotskiNodeUtil.ROW[i] < 4 && KlotskiNodeUtil.TYPE[status[i + 4]] != KlotskiNodeUtil.T
                    && KlotskiNodeUtil.TYPE[status[i + 4]] != KlotskiNodeUtil.H) {
                VIndex++;
            }
            if (charType == KlotskiNodeUtil.V) {
                if (!flag[intType]) {
                    VSequence += KlotskiNodeUtil.BINARY_WEIGHTS[VIndex];
                    flag[intType] = true;
                }
                continue;
            }
            if (charType == KlotskiNodeUtil.R || charType == KlotskiNodeUtil.S) {
                SIndex++;
            }
            if (charType == KlotskiNodeUtil.S) {
                SSequence += KlotskiNodeUtil.BINARY_WEIGHTS[SIndex];
            }
        }
        return SEQUENCE[SSequence] + VW[SEQUENCE[VSequence]] + HW[SEQUENCE[HSequence]] + TW[TIndex];
    }

    /**
     * 镜面对称盘面编码值
     * @param status
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
