package pf.mrbutters.dev.dsync.utils;

import java.util.Random;

public class LinkCode {

    public static String get() {
        Random r = new Random();

        StringBuilder code = new StringBuilder();
        for (int x = 0; x<=5; x++) {
            char c = (char)(r.nextInt(26) + 'a');
            if (r.nextBoolean()) {
                code.append(c);
            } else {
                code.append(Character.toUpperCase(c));
            }
        }
        return code.toString();
    }
}
