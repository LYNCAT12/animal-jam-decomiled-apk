package net.codestage.actk.androidnative;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.lang.reflect.Field;

class NativeUtils {
    static String LogTag = "ACTk";
    private static Context applicationContext;
    private static final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    NativeUtils() {
    }

    static String BytesToHex(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            byte b = (bArr[i] ^ 144) & 255;
            int i2 = i * 2;
            char[] cArr2 = hexArray;
            cArr[i2] = cArr2[b >>> 4];
            cArr[i2 + 1] = cArr2[b & 15];
        }
        return new String(cArr);
    }

    static String GetApkPath() throws PackageManager.NameNotFoundException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        GetUnityPlayerActivityIfNeeded();
        Context context = applicationContext;
        if (context == null) {
            Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get Unity context!");
            return null;
        }
        String packageName = context.getPackageName();
        if (packageName == null) {
            Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get package name!");
            return null;
        }
        PackageManager packageManager = applicationContext.getPackageManager();
        if (packageManager == null) {
            Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get package manager!");
            return null;
        }
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        if (applicationInfo != null) {
            return applicationInfo.publicSourceDir;
        }
        Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get ApplicationInfo!");
        return null;
    }

    static boolean ContainsIgnoreCase(String str, String str2) {
        if (str == null || str2 == null) {
            return false;
        }
        return str.toLowerCase().contains(str2.toLowerCase());
    }

    private static void GetUnityPlayerActivityIfNeeded() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        if (applicationContext == null) {
            Class<?> cls = Class.forName("com.unity3d.player.UnityPlayer");
            if (cls == null) {
                Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get com.unity3d.player.UnityPlayer class!");
                return;
            }
            Field declaredField = cls.getDeclaredField("currentActivity");
            if (declaredField == null) {
                Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get com.unity3d.player.UnityPlayer:currentActivity field!");
                return;
            }
            Activity activity = (Activity) declaredField.get((Object) null);
            if (activity == null) {
                Log.e(LogTag, "[CodeHashGenerator ERROR] Couldn't get Activity from com.unity3d.player.UnityPlayer:currentActivity field!");
            } else {
                applicationContext = activity.getApplicationContext();
            }
        }
    }
}
