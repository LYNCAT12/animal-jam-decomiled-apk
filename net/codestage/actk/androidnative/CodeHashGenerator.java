package net.codestage.actk.androidnative;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CodeHashGenerator {
    /* access modifiers changed from: private */
    public static CodeHashCallback lastCallback;

    private static class HashCalculationResult {
        String buildPath;
        String errorText;
        String[] hashes;
        String[] paths;
        String summaryHash;

        private HashCalculationResult() {
        }
    }

    private static class CodeHashCalculator extends AsyncTask<String[], Void, HashCalculationResult> {
        private CodeHashCalculator() {
        }

        /* access modifiers changed from: protected */
        public HashCalculationResult doInBackground(String[]... strArr) {
            try {
                return CodeHashGenerator.GetCodeHashThread(CodeHashGenerator.GetFiltersFromStrings(strArr[0]));
            } catch (Throwable th) {
                HashCalculationResult hashCalculationResult = new HashCalculationResult();
                hashCalculationResult.errorText = ACTkErrors.GetText("Something went wrong while trying to hash current APK: " + th.toString());
                Log.e(NativeUtils.LogTag, hashCalculationResult.errorText, th);
                return hashCalculationResult;
            }
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(HashCalculationResult hashCalculationResult) {
            if (hashCalculationResult.errorText != null) {
                CodeHashGenerator.lastCallback.OnError(hashCalculationResult.errorText);
            } else {
                CodeHashGenerator.lastCallback.OnSuccess(hashCalculationResult.buildPath, hashCalculationResult.paths, hashCalculationResult.hashes, hashCalculationResult.summaryHash);
            }
        }
    }

    public static void GetCodeHash(String[] strArr, CodeHashCallback codeHashCallback) {
        lastCallback = codeHashCallback;
        new CodeHashCalculator().execute(new String[][]{strArr});
    }

    /* access modifiers changed from: private */
    public static HashCalculationResult GetCodeHashThread(FileFilter[] fileFilterArr) throws ClassNotFoundException, PackageManager.NameNotFoundException, NoSuchFieldException, IllegalAccessException, IOException, NoSuchAlgorithmException {
        HashCalculationResult hashCalculationResult = new HashCalculationResult();
        String GetApkPath = NativeUtils.GetApkPath();
        if (GetApkPath == null) {
            hashCalculationResult.errorText = "[CodeHashGenerator ERROR] Can't get APK path!";
            Log.e(NativeUtils.LogTag, hashCalculationResult.errorText);
            return hashCalculationResult;
        }
        ArrayList<String> arrayList = new ArrayList<>();
        File file = new File(GetApkPath);
        if (!file.exists()) {
            hashCalculationResult.errorText = ACTkErrors.GetText("[CodeHashGenerator ERROR] Can't create file for APK path: " + GetApkPath);
            Log.e(NativeUtils.LogTag, hashCalculationResult.errorText);
            return hashCalculationResult;
        }
        File parentFile = file.getParentFile();
        if (parentFile == null) {
            Log.w(NativeUtils.LogTag, "[CodeHashGenerator ERROR] Can't find parent folder of APK file at path: ");
            arrayList.add(GetApkPath);
            hashCalculationResult.buildPath = GetApkPath;
        } else {
            File[] listFiles = parentFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                Log.w(NativeUtils.LogTag, "[CodeHashGenerator ERROR] Can't get any files in APK folder at path: ");
                arrayList.add(GetApkPath);
                hashCalculationResult.buildPath = GetApkPath;
            } else {
                for (File path : listFiles) {
                    String path2 = path.getPath();
                    if (path2.equals("")) {
                        Log.w(NativeUtils.LogTag, "[CodeHashGenerator ERROR] Can't get path for file in APK folder!");
                    } else if (path2.toLowerCase().endsWith(".apk")) {
                        arrayList.add(path2);
                    }
                }
                hashCalculationResult.buildPath = GetApkPath;
            }
        }
        MessageDigest instance = MessageDigest.getInstance("SHA-1");
        if (instance == null) {
            hashCalculationResult.errorText = ACTkErrors.GetText("[CodeHashGenerator ERROR] Can't get SHA-1 digest instance!");
            Log.e(NativeUtils.LogTag, hashCalculationResult.errorText);
            return hashCalculationResult;
        }
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        for (String str : arrayList) {
            JarFile GetJarFromApk = GetJarFromApk(str);
            if (GetJarFromApk == null) {
                hashCalculationResult.errorText = ACTkErrors.GetText("[CodeHashGenerator ERROR] Can't read APK with path: " + str);
                Log.e(NativeUtils.LogTag, hashCalculationResult.errorText);
                return hashCalculationResult;
            }
            HashFiles(instance, GetJarFromApk, fileFilterArr, arrayList2, arrayList3);
        }
        if (arrayList3.size() == 0) {
            hashCalculationResult.errorText = ACTkErrors.GetText("[CodeHashGenerator ERROR] Nothing to hash!");
            Log.e(NativeUtils.LogTag, hashCalculationResult.errorText);
            return hashCalculationResult;
        }
        hashCalculationResult.buildPath = GetApkPath;
        hashCalculationResult.hashes = (String[]) arrayList3.toArray(new String[0]);
        hashCalculationResult.paths = (String[]) arrayList2.toArray(new String[0]);
        hashCalculationResult.summaryHash = GetArrayHash(instance, arrayList3);
        return hashCalculationResult;
    }

    private static JarFile GetJarFromApk(String str) throws IOException {
        if (new File(str).exists()) {
            return new JarFile(str);
        }
        return null;
    }

    private static void HashFiles(MessageDigest messageDigest, JarFile jarFile, FileFilter[] fileFilterArr, List<String> list, List<String> list2) throws IOException {
        boolean z;
        byte[] bArr = new byte[8192];
        Enumeration<JarEntry> entries = jarFile.entries();
        if (entries == null) {
            Log.e(NativeUtils.LogTag, "[CodeHashGenerator ERROR] Nothing found in APK JAR!");
            return;
        }
        for (T t : Collections.list(entries)) {
            if (t == null) {
                Log.w(NativeUtils.LogTag, "[CodeHashGenerator ERROR] Some JAR entry is null!");
            } else {
                String name = t.getName();
                if (name == null) {
                    Log.w(NativeUtils.LogTag, "[CodeHashGenerator ERROR] Some JAR entry has null path!");
                } else {
                    int length = fileFilterArr.length;
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            z = false;
                            break;
                        } else if (fileFilterArr[i].MatchPath(name)) {
                            z = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                    if (z) {
                        InputStream inputStream = jarFile.getInputStream(t);
                        if (inputStream == null) {
                            String str = NativeUtils.LogTag;
                            Log.w(str, "[CodeHashGenerator ERROR] JAR has null input stream for entry with path: " + name);
                        } else {
                            int i2 = 0;
                            while (i2 != -1) {
                                i2 = inputStream.read(bArr);
                                if (i2 > 0) {
                                    messageDigest.update(bArr, 0, i2);
                                }
                            }
                            String BytesToHex = NativeUtils.BytesToHex(messageDigest.digest());
                            list.add(name);
                            list2.add(BytesToHex);
                            messageDigest.reset();
                            inputStream.close();
                        }
                    }
                }
            }
        }
    }

    private static String GetArrayHash(MessageDigest messageDigest, List<String> list) {
        Collections.sort(list);
        messageDigest.update(TextUtils.join("", list).getBytes());
        return NativeUtils.BytesToHex(messageDigest.digest());
    }

    /* access modifiers changed from: private */
    public static FileFilter[] GetFiltersFromStrings(String[] strArr) {
        FileFilter[] fileFilterArr = new FileFilter[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            fileFilterArr[i] = new FileFilter(strArr[i]);
        }
        return fileFilterArr;
    }
}
