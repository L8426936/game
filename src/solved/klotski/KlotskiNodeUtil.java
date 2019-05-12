package solved.klotski;

public class KlotskiNodeUtil {
    /**
     * <p>E：empty 空格 0</p>
     * <p>S：soldier 兵 1~4</p>
     * <p>H：horizontal 横将 5~9</p>
     * <p>V：vertical 竖将 10~14</p>
     * <p>T：target 曹操 15</p>
     */
    public final static char E = 'E', S = 'S', H = 'H', V = 'V', T = 'T';
    public final static char[] INT_TO_CHAR_BY_TYPE = {E, S, S, S, S, H, H, H, H, H, V, V, V, V, V, T};
    public final static int[] ROW = {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4},
            COL = {0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3},
            BINARY = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};

    /**
     * <p>棋子移动模式</p>
     * <p>RIGHT_ANGLE_TURN：移动同一方块为一步</p>
     * <p>STRAIGHT：直线移动为一步</p>
     * <p>ONE_CELL_ONLY：移动一格为一步</p>
     */
    public final static int RIGHT_ANGLE_TURN = 0, STRAIGHT = 1, ONE_CELL_ONLY = 2;

    public static void printlnStatus(int[] status) {
        for (int i = 0; i < status.length; i += 4) {
            System.out.format("%02d %02d %02d %02d%n", status[i], status[i + 1], status[i + 2], status[i + 3]);
        }
    }

    /**
     * 计算数组平均值
     * @param rgb
     * @return
     */
    public static double average(int[] rgb) {
        double sum = 0;
        for (int i = 0; i < rgb.length; i++) {
            sum += rgb[i];
        }
        return sum / rgb.length;
    }

    /**
     * 二进制数组转16进制字符
     * @param binaries
     * @return
     */
    public static String binariesToHex(int[] binaries) {
        char[] hashCode = new char[binaries.length >> 2];
        for (int i = 0, j = 0; i < binaries.length; i += 4, j++) {
            int sum = (binaries[i] << 3) + (binaries[i + 1] << 2) + (binaries[i + 2] << 1) + binaries[i + 3];
            switch (sum) {
                case 0:
                    hashCode[j] = '0';
                    break;
                case 1:
                    hashCode[j] = '1';
                    break;
                case 2:
                    hashCode[j] = '2';
                    break;
                case 3:
                    hashCode[j] = '3';
                    break;
                case 4:
                    hashCode[j] = '4';
                    break;
                case 5:
                    hashCode[j] = '5';
                    break;
                case 6:
                    hashCode[j] = '6';
                    break;
                case 7:
                    hashCode[j] = '7';
                    break;
                case 8:
                    hashCode[j] = '8';
                    break;
                case 9:
                    hashCode[j] = '9';
                    break;
                case 10:
                    hashCode[j] = 'A';
                    break;
                case 11:
                    hashCode[j] = 'B';
                    break;
                case 12:
                    hashCode[j] = 'C';
                    break;
                case 13:
                    hashCode[j] = 'D';
                    break;
                case 14:
                    hashCode[j] = 'E';
                    break;
                case 15:
                    hashCode[j] = 'F';
                    break;
                default:
            }
        }
        return String.valueOf(hashCode);
    }

}
