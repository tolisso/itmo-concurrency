import org.jetbrains.annotations.NotNull;

/**
 * В теле класса решения разрешено использовать только финальные переменные типа RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author Malko Egor
 */
public class Solution implements MonotonicClock {
    private final RegularInt c1 = new RegularInt(0);
    private final RegularInt c2 = new RegularInt(0);
    private final RegularInt c3 = new RegularInt(0);

    private final RegularInt b1 = new RegularInt(0);
    private final RegularInt b2 = new RegularInt(0);
    
    @Override
    public void write(@NotNull Time time) {
        b1.setValue(time.getD1());
        b2.setValue(time.getD2());
        c3.setValue(time.getD3());
        c2.setValue(time.getD2());
        c1.setValue(time.getD1());

    }

    @NotNull
    @Override
    public Time read() {
        int w1 = c1.getValue();
	    int w2 = c2.getValue();
        int w3 = c3.getValue();
        int v2 = b2.getValue();
        int v1 = b1.getValue();
        if (w1 == v1 && w2 == v2) {
            return new Time(v1, v2, w3); 
        }
        int i = 0;
        if (w1 == v1) {
            return new Time(v1, v2, 0); 
        }
        return new Time(v1, 0, 0); 
    }
}
