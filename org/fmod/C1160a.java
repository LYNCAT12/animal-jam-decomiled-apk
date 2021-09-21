package org.fmod;

import android.media.AudioRecord;
import android.util.Log;
import java.nio.ByteBuffer;

/* renamed from: org.fmod.a */
final class C1160a implements Runnable {

    /* renamed from: a */
    private final FMODAudioDevice f1949a;

    /* renamed from: b */
    private final ByteBuffer f1950b;

    /* renamed from: c */
    private final int f1951c;

    /* renamed from: d */
    private final int f1952d;

    /* renamed from: e */
    private final int f1953e = 2;

    /* renamed from: f */
    private volatile Thread f1954f;

    /* renamed from: g */
    private volatile boolean f1955g;

    /* renamed from: h */
    private AudioRecord f1956h;

    /* renamed from: i */
    private boolean f1957i;

    C1160a(FMODAudioDevice fMODAudioDevice, int i, int i2) {
        this.f1949a = fMODAudioDevice;
        this.f1951c = i;
        this.f1952d = i2;
        this.f1950b = ByteBuffer.allocateDirect(AudioRecord.getMinBufferSize(i, i2, 2));
    }

    /* renamed from: d */
    private void m1002d() {
        AudioRecord audioRecord = this.f1956h;
        if (audioRecord != null) {
            if (audioRecord.getState() == 1) {
                this.f1956h.stop();
            }
            this.f1956h.release();
            this.f1956h = null;
        }
        this.f1950b.position(0);
        this.f1957i = false;
    }

    /* renamed from: a */
    public final int mo9497a() {
        return this.f1950b.capacity();
    }

    /* renamed from: b */
    public final void mo9498b() {
        if (this.f1954f != null) {
            mo9499c();
        }
        this.f1955g = true;
        this.f1954f = new Thread(this);
        this.f1954f.start();
    }

    /* renamed from: c */
    public final void mo9499c() {
        while (this.f1954f != null) {
            this.f1955g = false;
            try {
                this.f1954f.join();
                this.f1954f = null;
            } catch (InterruptedException unused) {
            }
        }
    }

    public final void run() {
        int i = 3;
        while (this.f1955g) {
            if (!this.f1957i && i > 0) {
                m1002d();
                this.f1956h = new AudioRecord(1, this.f1951c, this.f1952d, this.f1953e, this.f1950b.capacity());
                boolean z = true;
                if (this.f1956h.getState() != 1) {
                    z = false;
                }
                this.f1957i = z;
                if (this.f1957i) {
                    this.f1950b.position(0);
                    this.f1956h.startRecording();
                    i = 3;
                } else {
                    Log.e("FMOD", "AudioRecord failed to initialize (status " + this.f1956h.getState() + ")");
                    i += -1;
                    m1002d();
                }
            }
            if (this.f1957i && this.f1956h.getRecordingState() == 3) {
                AudioRecord audioRecord = this.f1956h;
                ByteBuffer byteBuffer = this.f1950b;
                this.f1949a.fmodProcessMicData(this.f1950b, audioRecord.read(byteBuffer, byteBuffer.capacity()));
                this.f1950b.position(0);
            }
        }
        m1002d();
    }
}
