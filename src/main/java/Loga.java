import okhttp3.OkHttpClient;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Loga {

    public static void aaaaaa() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }
}
