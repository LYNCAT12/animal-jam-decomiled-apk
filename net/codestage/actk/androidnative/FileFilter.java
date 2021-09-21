package net.codestage.actk.androidnative;

import android.util.Log;

class FileFilter {
    private Boolean caseSensitive;
    private Boolean exactFileNameMatch;
    private Boolean exactFolderMatch;
    private String filterExtension;
    private String filterFileName;
    private String filterFolder;
    private Boolean folderRecursive;

    FileFilter(String str) {
        if (str == null || str.isEmpty()) {
            Log.e(NativeUtils.LogTag, "Can't parse FileFilter: string is null or empty!");
            return;
        }
        String[] split = str.split("\\|");
        if (split.length < 4) {
            String str2 = NativeUtils.LogTag;
            Log.e(str2, "Can't parse FileFilter: string split only to " + split.length + " parts instead of minimum 4 parts!");
            return;
        }
        this.caseSensitive = Boolean.valueOf(Boolean.parseBoolean(split[0]));
        this.folderRecursive = Boolean.valueOf(Boolean.parseBoolean(split[1]));
        this.exactFileNameMatch = Boolean.valueOf(Boolean.parseBoolean(split[2]));
        this.exactFolderMatch = Boolean.valueOf(Boolean.parseBoolean(split[3]));
        if (split.length >= 5) {
            this.filterFolder = split[4];
        }
        if (split.length >= 6) {
            this.filterExtension = split[5];
        }
        if (split.length >= 7) {
            this.filterFileName = split[6];
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004b, code lost:
        if (r5.filterExtension.equalsIgnoreCase(r6) == false) goto L_0x004d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean MatchPath(java.lang.String r6) {
        /*
            r5 = this;
            java.io.File r0 = new java.io.File
            r0.<init>(r6)
            java.lang.String r6 = r5.filterExtension
            r1 = 1
            java.lang.String r2 = "."
            r3 = 0
            if (r6 == 0) goto L_0x004e
            boolean r6 = r6.isEmpty()
            if (r6 != 0) goto L_0x004e
            java.lang.String r6 = r0.getName()
            boolean r4 = r6.isEmpty()
            if (r4 == 0) goto L_0x001e
            return r3
        L_0x001e:
            int r4 = r6.indexOf(r2)
            if (r4 <= 0) goto L_0x004d
            int r4 = r6.lastIndexOf(r2)
            int r4 = r4 + r1
            java.lang.String r6 = r6.substring(r4)
            boolean r4 = r6.isEmpty()
            if (r4 == 0) goto L_0x0034
            return r3
        L_0x0034:
            java.lang.Boolean r4 = r5.caseSensitive
            boolean r4 = r4.booleanValue()
            if (r4 == 0) goto L_0x0045
            java.lang.String r4 = r5.filterExtension
            boolean r6 = r4.equals(r6)
            if (r6 != 0) goto L_0x004e
            return r3
        L_0x0045:
            java.lang.String r4 = r5.filterExtension
            boolean r6 = r4.equalsIgnoreCase(r6)
            if (r6 != 0) goto L_0x004e
        L_0x004d:
            return r3
        L_0x004e:
            java.lang.String r6 = r5.filterFileName
            if (r6 == 0) goto L_0x00ad
            boolean r6 = r6.isEmpty()
            if (r6 != 0) goto L_0x00ad
            java.lang.String r6 = r0.getName()
            boolean r0 = r6.isEmpty()
            if (r0 == 0) goto L_0x0063
            return r3
        L_0x0063:
            int r0 = r6.indexOf(r2)
            if (r0 <= 0) goto L_0x0071
            int r0 = r6.lastIndexOf(r2)
            java.lang.String r6 = r6.substring(r3, r0)
        L_0x0071:
            java.lang.Boolean r0 = r5.exactFileNameMatch
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x0093
            java.lang.Boolean r0 = r5.caseSensitive
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x008a
            java.lang.String r0 = r5.filterFileName
            boolean r6 = r0.equals(r6)
            if (r6 != 0) goto L_0x00ad
            return r3
        L_0x008a:
            java.lang.String r0 = r5.filterFileName
            boolean r6 = r0.equalsIgnoreCase(r6)
            if (r6 != 0) goto L_0x00ad
            return r3
        L_0x0093:
            java.lang.Boolean r0 = r5.caseSensitive
            boolean r0 = r0.booleanValue()
            if (r0 == 0) goto L_0x00a4
            java.lang.String r0 = r5.filterFileName
            boolean r6 = r6.contains(r0)
            if (r6 != 0) goto L_0x00ad
            return r3
        L_0x00a4:
            java.lang.String r0 = r5.filterFileName
            boolean r6 = net.codestage.actk.androidnative.NativeUtils.ContainsIgnoreCase(r6, r0)
            if (r6 != 0) goto L_0x00ad
            return r3
        L_0x00ad:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: net.codestage.actk.androidnative.FileFilter.MatchPath(java.lang.String):boolean");
    }
}
