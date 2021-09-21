package net.codestage.actk.androidnative;

public interface CodeHashCallback {
    void OnError(String str);

    void OnSuccess(String str, String[] strArr, String[] strArr2, String str2);
}
