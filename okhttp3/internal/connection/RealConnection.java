package okhttp3.internal.connection;

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Connection;
import okhttp3.ConnectionSpec;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.Version;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http1.Http1Codec;
import okhttp3.internal.http2.ErrorCode;
import okhttp3.internal.http2.Http2Connection;
import okhttp3.internal.http2.Http2Stream;
import okhttp3.internal.platform.Platform;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public final class RealConnection extends Http2Connection.Listener implements Connection {
    public int allocationLimit;
    public final List<Reference<StreamAllocation>> allocations = new ArrayList();
    private Handshake handshake;
    public volatile Http2Connection http2Connection;
    public long idleAtNanos = Long.MAX_VALUE;
    public boolean noNewStreams;
    private Protocol protocol;
    private Socket rawSocket;
    private final Route route;
    public BufferedSink sink;
    public Socket socket;
    public BufferedSource source;
    public int successCount;

    public RealConnection(Route route2) {
        this.route = route2;
    }

    public void connect(int i, int i2, int i3, List<ConnectionSpec> list, boolean z) {
        if (this.protocol == null) {
            ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(list);
            if (this.route.address().sslSocketFactory() == null) {
                if (list.contains(ConnectionSpec.CLEARTEXT)) {
                    String host = this.route.address().url().host();
                    if (!Platform.get().isCleartextTrafficPermitted(host)) {
                        throw new RouteException(new UnknownServiceException("CLEARTEXT communication to " + host + " not permitted by network security policy"));
                    }
                } else {
                    throw new RouteException(new UnknownServiceException("CLEARTEXT communication not enabled for client"));
                }
            }
            RouteException routeException = null;
            while (this.protocol == null) {
                try {
                    if (this.route.requiresTunnel()) {
                        buildTunneledConnection(i, i2, i3, connectionSpecSelector);
                    } else {
                        buildConnection(i, i2, i3, connectionSpecSelector);
                    }
                } catch (IOException e) {
                    Util.closeQuietly(this.socket);
                    Util.closeQuietly(this.rawSocket);
                    this.socket = null;
                    this.rawSocket = null;
                    this.source = null;
                    this.sink = null;
                    this.handshake = null;
                    this.protocol = null;
                    if (routeException == null) {
                        routeException = new RouteException(e);
                    } else {
                        routeException.addConnectException(e);
                    }
                    if (!z || !connectionSpecSelector.connectionFailed(e)) {
                        throw routeException;
                    }
                }
            }
            return;
        }
        throw new IllegalStateException("already connected");
    }

    private void buildTunneledConnection(int i, int i2, int i3, ConnectionSpecSelector connectionSpecSelector) throws IOException {
        Request createTunnelRequest = createTunnelRequest();
        HttpUrl url = createTunnelRequest.url();
        int i4 = 0;
        while (true) {
            i4++;
            if (i4 <= 21) {
                connectSocket(i, i2);
                createTunnelRequest = createTunnel(i2, i3, createTunnelRequest, url);
                if (createTunnelRequest == null) {
                    establishProtocol(i2, i3, connectionSpecSelector);
                    return;
                }
                Util.closeQuietly(this.rawSocket);
                this.rawSocket = null;
                this.sink = null;
                this.source = null;
            } else {
                throw new ProtocolException("Too many tunnel connections attempted: " + 21);
            }
        }
    }

    private void buildConnection(int i, int i2, int i3, ConnectionSpecSelector connectionSpecSelector) throws IOException {
        connectSocket(i, i2);
        establishProtocol(i2, i3, connectionSpecSelector);
    }

    private void connectSocket(int i, int i2) throws IOException {
        Proxy proxy = this.route.proxy();
        this.rawSocket = (proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP) ? this.route.address().socketFactory().createSocket() : new Socket(proxy);
        this.rawSocket.setSoTimeout(i2);
        try {
            Platform.get().connectSocket(this.rawSocket, this.route.socketAddress(), i);
            this.source = Okio.buffer(Okio.source(this.rawSocket));
            this.sink = Okio.buffer(Okio.sink(this.rawSocket));
        } catch (ConnectException e) {
            ConnectException connectException = new ConnectException("Failed to connect to " + this.route.socketAddress());
            connectException.initCause(e);
            throw connectException;
        }
    }

    private void establishProtocol(int i, int i2, ConnectionSpecSelector connectionSpecSelector) throws IOException {
        if (this.route.address().sslSocketFactory() != null) {
            connectTls(i, i2, connectionSpecSelector);
        } else {
            this.protocol = Protocol.HTTP_1_1;
            this.socket = this.rawSocket;
        }
        if (this.protocol == Protocol.HTTP_2) {
            this.socket.setSoTimeout(0);
            Http2Connection build = new Http2Connection.Builder(true).socket(this.socket, this.route.address().url().host(), this.source, this.sink).listener(this).build();
            build.start();
            this.allocationLimit = build.maxConcurrentStreams();
            this.http2Connection = build;
            return;
        }
        this.allocationLimit = 1;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: javax.net.ssl.SSLSocket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v5, resolved type: javax.net.ssl.SSLSocket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v7, resolved type: javax.net.ssl.SSLSocket} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: java.lang.String} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v8, resolved type: javax.net.ssl.SSLSocket} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0115 A[Catch:{ all -> 0x010b }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x011b A[Catch:{ all -> 0x010b }] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x011e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void connectTls(int r6, int r7, okhttp3.internal.connection.ConnectionSpecSelector r8) throws java.io.IOException {
        /*
            r5 = this;
            okhttp3.Route r6 = r5.route
            okhttp3.Address r6 = r6.address()
            javax.net.ssl.SSLSocketFactory r7 = r6.sslSocketFactory()
            r0 = 0
            java.net.Socket r1 = r5.rawSocket     // Catch:{ AssertionError -> 0x010e }
            okhttp3.HttpUrl r2 = r6.url()     // Catch:{ AssertionError -> 0x010e }
            java.lang.String r2 = r2.host()     // Catch:{ AssertionError -> 0x010e }
            okhttp3.HttpUrl r3 = r6.url()     // Catch:{ AssertionError -> 0x010e }
            int r3 = r3.port()     // Catch:{ AssertionError -> 0x010e }
            r4 = 1
            java.net.Socket r7 = r7.createSocket(r1, r2, r3, r4)     // Catch:{ AssertionError -> 0x010e }
            javax.net.ssl.SSLSocket r7 = (javax.net.ssl.SSLSocket) r7     // Catch:{ AssertionError -> 0x010e }
            okhttp3.ConnectionSpec r8 = r8.configureSecureSocket(r7)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            boolean r1 = r8.supportsTlsExtensions()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            if (r1 == 0) goto L_0x0041
            okhttp3.internal.platform.Platform r1 = okhttp3.internal.platform.Platform.get()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okhttp3.HttpUrl r2 = r6.url()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r2 = r2.host()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.util.List r3 = r6.protocols()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r1.configureTlsExtensions(r7, r2, r3)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
        L_0x0041:
            r7.startHandshake()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            javax.net.ssl.SSLSession r1 = r7.getSession()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okhttp3.Handshake r1 = okhttp3.Handshake.get(r1)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            javax.net.ssl.HostnameVerifier r2 = r6.hostnameVerifier()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okhttp3.HttpUrl r3 = r6.url()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r3 = r3.host()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            javax.net.ssl.SSLSession r4 = r7.getSession()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            boolean r2 = r2.verify(r3, r4)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            if (r2 == 0) goto L_0x00b4
            okhttp3.CertificatePinner r2 = r6.certificatePinner()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okhttp3.HttpUrl r6 = r6.url()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = r6.host()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.util.List r3 = r1.peerCertificates()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r2.check((java.lang.String) r6, (java.util.List<java.security.cert.Certificate>) r3)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            boolean r6 = r8.supportsTlsExtensions()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            if (r6 == 0) goto L_0x0083
            okhttp3.internal.platform.Platform r6 = okhttp3.internal.platform.Platform.get()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r0 = r6.getSelectedProtocol(r7)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
        L_0x0083:
            r5.socket = r7     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.net.Socket r6 = r5.socket     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okio.Source r6 = okio.Okio.source((java.net.Socket) r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okio.BufferedSource r6 = okio.Okio.buffer((okio.Source) r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r5.source = r6     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.net.Socket r6 = r5.socket     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okio.Sink r6 = okio.Okio.sink((java.net.Socket) r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okio.BufferedSink r6 = okio.Okio.buffer((okio.Sink) r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r5.sink = r6     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r5.handshake = r1     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            if (r0 == 0) goto L_0x00a6
            okhttp3.Protocol r6 = okhttp3.Protocol.get(r0)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            goto L_0x00a8
        L_0x00a6:
            okhttp3.Protocol r6 = okhttp3.Protocol.HTTP_1_1     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
        L_0x00a8:
            r5.protocol = r6     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            if (r7 == 0) goto L_0x00b3
            okhttp3.internal.platform.Platform r6 = okhttp3.internal.platform.Platform.get()
            r6.afterHandshake(r7)
        L_0x00b3:
            return
        L_0x00b4:
            java.util.List r8 = r1.peerCertificates()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r0 = 0
            java.lang.Object r8 = r8.get(r0)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.security.cert.X509Certificate r8 = (java.security.cert.X509Certificate) r8     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            javax.net.ssl.SSLPeerUnverifiedException r0 = new javax.net.ssl.SSLPeerUnverifiedException     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r1.<init>()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r2 = "Hostname "
            r1.append(r2)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            okhttp3.HttpUrl r6 = r6.url()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = r6.host()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = " not verified:\n    certificate: "
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = okhttp3.CertificatePinner.pin(r8)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = "\n    DN: "
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.security.Principal r6 = r8.getSubjectDN()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = r6.getName()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = "\n    subjectAltNames: "
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.util.List r6 = okhttp3.internal.tls.OkHostnameVerifier.allSubjectAltNames(r8)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r1.append(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            java.lang.String r6 = r1.toString()     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            r0.<init>(r6)     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
            throw r0     // Catch:{ AssertionError -> 0x0108, all -> 0x0106 }
        L_0x0106:
            r6 = move-exception
            goto L_0x011c
        L_0x0108:
            r6 = move-exception
            r0 = r7
            goto L_0x010f
        L_0x010b:
            r6 = move-exception
            r7 = r0
            goto L_0x011c
        L_0x010e:
            r6 = move-exception
        L_0x010f:
            boolean r7 = okhttp3.internal.Util.isAndroidGetsocknameError(r6)     // Catch:{ all -> 0x010b }
            if (r7 == 0) goto L_0x011b
            java.io.IOException r7 = new java.io.IOException     // Catch:{ all -> 0x010b }
            r7.<init>(r6)     // Catch:{ all -> 0x010b }
            throw r7     // Catch:{ all -> 0x010b }
        L_0x011b:
            throw r6     // Catch:{ all -> 0x010b }
        L_0x011c:
            if (r7 == 0) goto L_0x0125
            okhttp3.internal.platform.Platform r8 = okhttp3.internal.platform.Platform.get()
            r8.afterHandshake(r7)
        L_0x0125:
            okhttp3.internal.Util.closeQuietly((java.net.Socket) r7)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: okhttp3.internal.connection.RealConnection.connectTls(int, int, okhttp3.internal.connection.ConnectionSpecSelector):void");
    }

    private Request createTunnel(int i, int i2, Request request, HttpUrl httpUrl) throws IOException {
        String str = "CONNECT " + Util.hostHeader(httpUrl, true) + " HTTP/1.1";
        while (true) {
            Http1Codec http1Codec = new Http1Codec((OkHttpClient) null, (StreamAllocation) null, this.source, this.sink);
            this.source.timeout().timeout((long) i, TimeUnit.MILLISECONDS);
            this.sink.timeout().timeout((long) i2, TimeUnit.MILLISECONDS);
            http1Codec.writeRequest(request.headers(), str);
            http1Codec.finishRequest();
            Response build = http1Codec.readResponse().request(request).build();
            long contentLength = HttpHeaders.contentLength(build);
            if (contentLength == -1) {
                contentLength = 0;
            }
            Source newFixedLengthSource = http1Codec.newFixedLengthSource(contentLength);
            Util.skipAll(newFixedLengthSource, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            newFixedLengthSource.close();
            int code = build.code();
            if (code != 200) {
                if (code == 407) {
                    Request authenticate = this.route.address().proxyAuthenticator().authenticate(this.route, build);
                    if (authenticate == null) {
                        throw new IOException("Failed to authenticate with proxy");
                    } else if ("close".equalsIgnoreCase(build.header("Connection"))) {
                        return authenticate;
                    } else {
                        request = authenticate;
                    }
                } else {
                    throw new IOException("Unexpected response code for CONNECT: " + build.code());
                }
            } else if (this.source.buffer().exhausted() && this.sink.buffer().exhausted()) {
                return null;
            } else {
                throw new IOException("TLS tunnel buffered too many bytes!");
            }
        }
    }

    private Request createTunnelRequest() {
        return new Request.Builder().url(this.route.address().url()).header("Host", Util.hostHeader(this.route.address().url(), true)).header("Proxy-Connection", "Keep-Alive").header("User-Agent", Version.userAgent()).build();
    }

    public Route route() {
        return this.route;
    }

    public void cancel() {
        Util.closeQuietly(this.rawSocket);
    }

    public Socket socket() {
        return this.socket;
    }

    public boolean isHealthy(boolean z) {
        int soTimeout;
        if (this.socket.isClosed() || this.socket.isInputShutdown() || this.socket.isOutputShutdown()) {
            return false;
        }
        if (this.http2Connection != null) {
            return !this.http2Connection.isShutdown();
        }
        if (z) {
            try {
                soTimeout = this.socket.getSoTimeout();
                this.socket.setSoTimeout(1);
                if (this.source.exhausted()) {
                    this.socket.setSoTimeout(soTimeout);
                    return false;
                }
                this.socket.setSoTimeout(soTimeout);
                return true;
            } catch (SocketTimeoutException unused) {
            } catch (IOException unused2) {
                return false;
            } catch (Throwable th) {
                this.socket.setSoTimeout(soTimeout);
                throw th;
            }
        }
        return true;
    }

    public void onStream(Http2Stream http2Stream) throws IOException {
        http2Stream.close(ErrorCode.REFUSED_STREAM);
    }

    public void onSettings(Http2Connection http2Connection2) {
        this.allocationLimit = http2Connection2.maxConcurrentStreams();
    }

    public Handshake handshake() {
        return this.handshake;
    }

    public boolean isMultiplexed() {
        return this.http2Connection != null;
    }

    public Protocol protocol() {
        if (this.http2Connection != null) {
            return Protocol.HTTP_2;
        }
        Protocol protocol2 = this.protocol;
        return protocol2 != null ? protocol2 : Protocol.HTTP_1_1;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connection{");
        sb.append(this.route.address().url().host());
        sb.append(":");
        sb.append(this.route.address().url().port());
        sb.append(", proxy=");
        sb.append(this.route.proxy());
        sb.append(" hostAddress=");
        sb.append(this.route.socketAddress());
        sb.append(" cipherSuite=");
        Handshake handshake2 = this.handshake;
        sb.append(handshake2 != null ? handshake2.cipherSuite() : "none");
        sb.append(" protocol=");
        sb.append(this.protocol);
        sb.append('}');
        return sb.toString();
    }
}
