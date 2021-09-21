package bitter.jnibridge;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JNIBridge {

    /* renamed from: bitter.jnibridge.JNIBridge$a */
    private static class C0158a implements InvocationHandler {

        /* renamed from: a */
        private Object f9a = new Object[0];

        /* renamed from: b */
        private long f10b;

        /* renamed from: c */
        private Constructor f11c;

        public C0158a(long j) {
            this.f10b = j;
            Class<MethodHandles.Lookup> cls = MethodHandles.Lookup.class;
            try {
                this.f11c = cls.getDeclaredConstructor(new Class[]{Class.class, Integer.TYPE});
                this.f11c.setAccessible(true);
            } catch (NoClassDefFoundError unused) {
                this.f11c = null;
            } catch (NoSuchMethodException unused2) {
                this.f11c = null;
            }
        }

        /* renamed from: a */
        private Object m19a(Object obj, Method method, Object[] objArr) {
            if (objArr == null) {
                objArr = new Object[0];
            }
            Class<?> declaringClass = method.getDeclaringClass();
            return ((MethodHandles.Lookup) this.f11c.newInstance(new Object[]{declaringClass, 2})).in(declaringClass).unreflectSpecial(method, declaringClass).bindTo(obj).invokeWithArguments(objArr);
        }

        /* renamed from: a */
        public final void mo3389a() {
            synchronized (this.f9a) {
                this.f10b = 0;
            }
        }

        public final void finalize() {
            synchronized (this.f9a) {
                if (this.f10b != 0) {
                    JNIBridge.delete(this.f10b);
                }
            }
        }

        public final Object invoke(Object obj, Method method, Object[] objArr) {
            synchronized (this.f9a) {
                if (this.f10b == 0) {
                    return null;
                }
                try {
                    Object invoke = JNIBridge.invoke(this.f10b, method.getDeclaringClass(), method, objArr);
                    return invoke;
                } catch (NoSuchMethodError e) {
                    if (this.f11c == null) {
                        System.err.println("JNIBridge error: Java interface default methods are only supported since Android Oreo");
                        throw e;
                    } else if ((method.getModifiers() & 1024) == 0) {
                        return m19a(obj, method, objArr);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    static native void delete(long j);

    static void disableInterfaceProxy(Object obj) {
        if (obj != null) {
            ((C0158a) Proxy.getInvocationHandler(obj)).mo3389a();
        }
    }

    static native Object invoke(long j, Class cls, Method method, Object[] objArr);

    static Object newInterfaceProxy(long j, Class[] clsArr) {
        return Proxy.newProxyInstance(JNIBridge.class.getClassLoader(), clsArr, new C0158a(j));
    }
}
