package cc.xypp.cjluFree;

import android.os.Binder;
import android.os.IBinder;

public interface IWifiManager {
    abstract class Stub extends Binder implements IWifiManager {

        public static IWifiManager asInterface(IBinder binder) {
            throw new RuntimeException("stub!");
        }
    }
}
