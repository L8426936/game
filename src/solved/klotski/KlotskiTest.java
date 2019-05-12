package solved.klotski;

public class KlotskiTest {
    public static void main(String[] args) {
        try {
            AutoPlay.play(KlotskiNodeUtil.STRAIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
