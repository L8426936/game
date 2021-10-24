package solved.evlover;

import java.util.Arrays;

public class KM {
    private int[][] table = null;     // 权重矩阵（方阵）
    private int[] xl = null;          // X标号值
    private int[] yl = null;          // Y标号值
    private int[] xMatch = null;      // X点对应的匹配点
    private int[] yMatch = null;      // Y点对应的匹配点
    private int n = 0;                // 矩阵维度

    public int solve(int[][] table) { // 入口，输入权重矩阵
        this.table = table;
        init();

        for (int x = 0; x < n; x++) {
            bfs(x);
        }

        int value = 0;
        for (int x = 0; x < n; x++) {
            value += table[x][xMatch[x]];
        }
        return value;
    }

    private void bfs(int startX) {   // 为一个x点寻找匹配
        boolean find = false;
        int endY = -1;
        int[] yPre = new int[n];     // 标识搜索路径上y点的前一个点
        boolean[] S = new boolean[n], T = new boolean[n]; // S集合，T集合
        Arrays.fill(yPre, -1);

        int a = Integer.MAX_VALUE;
        int[] queue = new int[n];    // 队列
        int qs = 0, qe = 0;          // 队列开始结束索引
        queue[qe++] = startX;
        while (true) {               // 循环直到找到匹配
            while (qs < qe && !find) {   // 队列不为空
                int x = queue[qs++];
                S[x] = true;
                for (int y = 0; y < n; y++) {
                    int tmp = xl[x] + yl[y] - table[x][y];
                    if (tmp == 0) {  // 相等子树中的边
                        if (T[y]) {
                            continue;
                        }
                        T[y] = true;
                        yPre[y] = x;
                        if (yMatch[y] == -1) {
                            endY = y;
                            find = true;
                            break;
                        } else {
                            queue[qe++] = yMatch[y];
                        }
                    } else {      // 不在相等子树中的边，记录一下最小差值
                        a = Math.min(a, tmp);
                    }
                }
            }
            if (find) {
                break;
            }
            qs = qe = 0;
            for (int i = 0; i < n; i++) {  // 根据a修改标号值
                if (S[i]) {
                    xl[i] -= a;
                    queue[qe++] = i;        // 把所有在S中的点加回到队列中
                }
                if (T[i]) {
                    yl[i] += a;
                }
            }
            a = Integer.MAX_VALUE;
        }

        while (endY != -1) {       // 找到可扩路最后的y点后，回溯并扩充
            int preX = yPre[endY], preY = xMatch[preX];
            xMatch[preX] = endY;
            yMatch[endY] = preX;
            endY = preY;
        }
    }

    private void init() {
        this.n = table.length;
        this.xl = new int[n];
        this.yl = new int[n];
        Arrays.fill(xl, Integer.MIN_VALUE);
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                if (table[x][y] > xl[x]) {
                    xl[x] = table[x][y];
                }
            }
        }
        this.xMatch = new int[n];
        this.yMatch = new int[n];
        Arrays.fill(xMatch, -1);
        Arrays.fill(yMatch, -1);
    }
}