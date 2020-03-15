package solved.evlover;

public class EvloverNode {
    private EvloverNode parent;
    /**
     * <p>最多可以保存61个点的四层六边形</p>
     */
    private long status;
    /**
     * <p>position（点击位置）：20~31位，x，y，z每四位二进制保存</p>
     * <p>action（操作）：18~19位，两位二进制保存</p>
     * <p>symmetry（对称性）：11~17位，每一位二进制保存</p>
     */
    private int positionActionSymmetry;

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
        return positionActionSymmetry >> 28;
    }

    public void setX(int x) {
        positionActionSymmetry = (positionActionSymmetry & 0XFFFFFFF) | (x << 28);
    }

    public int getY() {
        return (positionActionSymmetry << 4) >> 28;
    }

    public void setY(int y) {
        positionActionSymmetry = (positionActionSymmetry & 0XF0FFFFFF) | ((y & 0XF) << 24);
    }

    public int getZ() {
        return (positionActionSymmetry << 8) >> 28;
    }

    public void setZ(int z) {
        positionActionSymmetry = (positionActionSymmetry & 0XFF0FFFFF) | ((z & 0XF) << 20);
    }

    public int getAction() {
        return (positionActionSymmetry >> 18) & 0X3;
    }

    public void setAction(int action) {
        positionActionSymmetry = (positionActionSymmetry & 0XFFF3FFFF) | (action << 18);
    }

    public int getSymmetry() {
        return (positionActionSymmetry >> 11) & 0X7F;
    }

    public void setSymmetry(int symmetry) {
        positionActionSymmetry = (positionActionSymmetry & 0XFFFC07FF) | (symmetry << 11);
    }

}
