package solved.evlover;

public class EvloverNode {
    private EvloverNode parent;
    /**
     * <p>最多可以保存61个点的四层六边形</p>
     */
    private long status;
    /**
     * <p>position（点击位置）：20~31位，x、y、z依次从高位到低位每四位二进制保存</p>
     * <p>action（操作）：18~19位，两位二进制保存</p>
     * <p>step（步数）：9~17位，九位二进制保存</p>
     * <p>score（估计总步数）：0~8位，九位二进制保存</p>
     */
    private int positionActionStepScore;

    public EvloverNode getParent() {
        return parent;
    }

    public void setParent(EvloverNode parent) {
        this.parent = parent;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public int getX() {
        return positionActionStepScore >> 28;
    }

    public void setX(int x) {
        positionActionStepScore = (positionActionStepScore & 0XFFFFFFF) | (x << 28);
    }

    public int getY() {
        return (positionActionStepScore << 4) >> 28;
    }

    public void setY(int y) {
        positionActionStepScore = (positionActionStepScore & 0XF0FFFFFF) | ((y & 0XF) << 24);
    }

    public int getZ() {
        return (positionActionStepScore << 8) >> 28;
    }

    public void setZ(int z) {
        positionActionStepScore = (positionActionStepScore & 0XFF0FFFFF) | ((z & 0XF) << 20);
    }

    public int getAction() {
        return (positionActionStepScore >> 18) & 0X3;
    }

    public void setAction(int action) {
        positionActionStepScore = (positionActionStepScore & 0XFFF3FFFF) | (action << 18);
    }

    public int getStep() {
        return (positionActionStepScore >> 9) & 0X1FF;
    }

    public void setStep(int step) {
        positionActionStepScore = (positionActionStepScore & 0XFFFC01FF) | (step << 9);
    }

    public int getScore() {
        return positionActionStepScore & 0X1FF;
    }

    public void setScore(int score) {
        positionActionStepScore = (positionActionStepScore & 0XFFFFFE00) | score;
    }
}
