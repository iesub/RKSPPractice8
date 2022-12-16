package xenophan.microservice.authservice.util;

import java.util.UUID;

public class Utilities {
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public static boolean validString(String input) {
        return input!=null && !input.isEmpty();
    }
}
