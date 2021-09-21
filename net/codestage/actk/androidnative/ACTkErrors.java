package net.codestage.actk.androidnative;

class ACTkErrors {
    static final String APKReadError = "[CodeHashGenerator ERROR] Can't read APK with path: ";
    private static final String ErrorPrefix = "[CodeHashGenerator ERROR] ";
    static final String NoApkFile = "[CodeHashGenerator ERROR] Can't create file for APK path: ";
    static final String NoApkFolder = "[CodeHashGenerator ERROR] Can't find parent folder of APK file at path: ";
    static final String NoApkPath = "[CodeHashGenerator ERROR] Can't get APK path!";
    static final String NoApplicationInfo = "[CodeHashGenerator ERROR] Couldn't get ApplicationInfo!";
    static final String NoContext = "[CodeHashGenerator ERROR] Couldn't get Unity context!";
    static final String NoFilePath = "[CodeHashGenerator ERROR] Can't get path for file in APK folder!";
    static final String NoFilesInApkFolder = "[CodeHashGenerator ERROR] Can't get any files in APK folder at path: ";
    static final String NoJarEntries = "[CodeHashGenerator ERROR] Nothing found in APK JAR!";
    static final String NoPackageManager = "[CodeHashGenerator ERROR] Couldn't get package manager!";
    static final String NoPackageName = "[CodeHashGenerator ERROR] Couldn't get package name!";
    static final String NoSHA1Digest = "[CodeHashGenerator ERROR] Can't get SHA-1 digest instance!";
    static final String NoUnityActivity = "[CodeHashGenerator ERROR] Couldn't get Activity from com.unity3d.player.UnityPlayer:currentActivity field!";
    static final String NoUnityClass = "[CodeHashGenerator ERROR] Couldn't get com.unity3d.player.UnityPlayer class!";
    static final String NoUnityField = "[CodeHashGenerator ERROR] Couldn't get com.unity3d.player.UnityPlayer:currentActivity field!";
    static final String NothingToHash = "[CodeHashGenerator ERROR] Nothing to hash!";
    static final String NullJarEntry = "[CodeHashGenerator ERROR] Some JAR entry is null!";
    static final String NullJarEntryPath = "[CodeHashGenerator ERROR] Some JAR entry has null path!";
    static final String NullJarInputStream = "[CodeHashGenerator ERROR] JAR has null input stream for entry with path: ";

    ACTkErrors() {
    }

    static String GetText(String str) {
        return ErrorPrefix + str;
    }
}
