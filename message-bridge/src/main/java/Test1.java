import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Test1 {


    public static void main(String[] args) throws InterruptedException {


    }


    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }
}
