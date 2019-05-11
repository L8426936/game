package solved.klotski;

public class KlotskiTest {
    public static void main(String[] args) {
        try {
            KlotskiNodeUtil.autoPlay(KlotskiNodeUtil.STRAIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
