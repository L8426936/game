package solved.klotski;

public class KlotskiNodeUtil {

    /**
     * 棋子类型表
     * <p>'R': room，空位，0</p>
     * <p>'S': square，正方形，1~4</p>
     * <p>'V': vertical，竖长方形，5~9</p>
     * <p>'H': horizontal，横长方形，10~14</p>
     * <p>'T': target，目标，15</p>
     */
    public final static char R = 'R', S = 'S', V = 'V', H = 'H', T = 'T';
    /**
     * @see #R
     * @see #S
     * @see #H
     * @see #V
     * @see #T
     */
    public final static char[] TYPE = {R, S, S, S, S, V, V, V, V, V, H, H, H, H, H, T};
    /**
     * 行号
     */
    public final static int ROW[] = {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4};
    /**
     * 列号
     */
    public final static int COL[] = {0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3};
    /**
     * 二进制权值
     */
    public final static int BINARY_WEIGHTS[] = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096};

    /**
     * 最大组合数
     * @param limit 最大值
     * @param n 棋子个数
     * @return
     */
    public static int maxCombinationNumber(int limit, int n) {
        int max = 0;
        while (n-- > 0) {
            max >>= 1;
            max += limit >> 1;
        }
        return max;
    }

    public static void printStatus(int[] status) {
        for (int i = 0, length = status.length; i < length; i += 4) {
            System.out.format("%02d %02d %02d %02d%n", status[i], status[i + 1], status[i + 2], status[i + 3]);
        }
        System.out.println();
    }
}
