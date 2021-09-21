package org.fmod;

import android.media.AudioTrack;
import android.util.Log;
import java.nio.ByteBuffer;

public class FMODAudioDevice implements Runnable {

    /* renamed from: h */
    private static int f1938h = 0;

    /* renamed from: i */
    private static int f1939i = 1;

    /* renamed from: j */
    private static int f1940j = 2;

    /* renamed from: k */
    private static int f1941k = 3;

    /* renamed from: a */
    private volatile Thread f1942a = null;

    /* renamed from: b */
    private volatile boolean f1943b = false;

    /* renamed from: c */
    private AudioTrack f1944c = null;

    /* renamed from: d */
    private boolean f1945d = false;

    /* renamed from: e */
    private ByteBuffer f1946e = null;

    /* renamed from: f */
    private byte[] f1947f = null;

    /* renamed from: g */
    private volatile C1160a f1948g;

    private native int fmodGetInfo(int i);

    private native int fmodProcess(ByteBuffer byteBuffer);

    private void releaseAudioTrack() {
        AudioTrack audioTrack = this.f1944c;
        if (audioTrack != null) {
            if (audioTrack.getState() == 1) {
                this.f1944c.stop();
            }
            this.f1944c.release();
            this.f1944c = null;
        }
        this.f1946e = null;
        this.f1947f = null;
        this.f1945d = false;
    }

    public synchronized void close() {
        stop();
    }

    /* access modifiers changed from: package-private */
    public native int fmodProcessMicData(ByteBuffer byteBuffer, int i);

    public boolean isRunning() {
        return this.f1942a != null && this.f1942a.isAlive();
    }

    public void run() {
        int i = 3;
        while (this.f1943b) {
            if (!this.f1945d && i > 0) {
                releaseAudioTrack();
                int fmodGetInfo = fmodGetInfo(f1938h);
                int round = Math.round(((float) AudioTrack.getMinBufferSize(fmodGetInfo, 3, 2)) * 1.1f) & -4;
                int fmodGetInfo2 = fmodGetInfo(f1939i);
                int fmodGetInfo3 = fmodGetInfo(f1940j) * fmodGetInfo2 * 4;
                this.f1944c = new AudioTrack(3, fmodGetInfo, 3, 2, fmodGetInfo3 > round ? fmodGetInfo3 : round, 1);
                this.f1945d = this.f1944c.getState() == 1;
                if (this.f1945d) {
                    this.f1946e = ByteBuffer.allocateDirect(fmodGetInfo2 * 2 * 2);
                    this.f1947f = new byte[this.f1946e.capacity()];
                    this.f1944c.play();
                    i = 3;
                } else {
                    Log.e("FMOD", "AudioTrack failed to initialize (status " + this.f1944c.getState() + ")");
                    releaseAudioTrack();
                    i += -1;
                }
            }
            if (this.f1945d) {
                if (fmodGetInfo(f1941k) == 1) {
                    fmodProcess(this.f1946e);
                    ByteBuffer byteBuffer = this.f1946e;
                    byteBuffer.get(this.f1947f, 0, byteBuffer.capacity());
                    this.f1944c.write(this.f1947f, 0, this.f1946e.capacity());
                    this.f1946e.position(0);
                } else {
                    releaseAudioTrack();
                }
            }
        }
        releaseAudioTrack();
    }

    public synchronized void start() {
        if (this.f1942a != null) {
            stop();
        }
        this.f1942a = new Thread(this, "FMODAudioDevice");
        this.f1942a.setPriority(10);
        this.f1943b = true;
        this.f1942a.start();
        if (this.f1948g != null) {
            this.f1948g.mo9498b();
        }
    }

    public synchronized int startAudioRecord(int i, int i2, int i3) {
        if (this.f1948g == null) {
            this.f1948g = new C1160a(this, i, i2);
            this.f1948g.mo9498b();
        }
        return this.f1948g.mo9497a();
    }

    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Missing exception handler attribute for start block: B:1:0x0001 */
    /* JADX WARNING: Removed duplicated region for block: B:1:0x0001 A[LOOP:0: B:1:0x0001->B:16:0x0001, LOOP_START, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void stop() {
        /*
            r1 = this;
            monitor-enter(r1)
        L_0x0001:
            java.lang.Thread r0 = r1.f1942a     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0011
            r0 = 0
            r1.f1943b = r0     // Catch:{ all -> 0x001c }
            java.lang.Thread r0 = r1.f1942a     // Catch:{ InterruptedException -> 0x0001 }
            r0.join()     // Catch:{ InterruptedException -> 0x0001 }
            r0 = 0
            r1.f1942a = r0     // Catch:{ InterruptedException -> 0x0001 }
            goto L_0x0001
        L_0x0011:
            org.fmod.a r0 = r1.f1948g     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x001a
            org.fmod.a r0 = r1.f1948g     // Catch:{ all -> 0x001c }
            r0.mo9499c()     // Catch:{ all -> 0x001c }
        L_0x001a:
            monitor-exit(r1)
            return
        L_0x001c:
            r0 = move-exception
            monitor-exit(r1)
            throw r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.fmod.FMODAudioDevice.stop():void");
    }

    public synchronized void stopAudioRecord() {
        if (this.f1948g != null) {
            this.f1948g.mo9499c();
            this.f1948g = null;
        }
    }
}
