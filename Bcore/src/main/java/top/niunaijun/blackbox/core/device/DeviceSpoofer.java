package top.niunaijun.blackbox.core.device;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;
import top.niunaijun.blackbox.utils.Slog;

/**
 * Makes one virtual app see the identity of a {@link DeviceProfile}.
 * <p>
 * Runs inside the virtual app's process before its Application class is
 * created, so the game reads the spoofed values from the very first call.
 * Two layers are covered:
 * <ul>
 *   <li>android.os.Build static fields (what Unity/Unreal and most SDKs read)</li>
 *   <li>android.os.SystemProperties.get for ro.product.* keys</li>
 * </ul>
 * Native reads through __system_property_get are not changed.
 */
public final class DeviceSpoofer {
    private static final String TAG = "DeviceSpoofer";
    private static boolean sApplied;

    private DeviceSpoofer() {
    }

    public static synchronized void apply(int userId, String packageName) {
        if (sApplied) return;
        DeviceProfile profile = DeviceProfileConfig.get(userId, packageName);
        if (profile == null) return;
        sApplied = true;

        setBuildField("BRAND", profile.brand);
        setBuildField("MANUFACTURER", profile.manufacturer);
        setBuildField("MODEL", profile.model);
        setBuildField("DEVICE", profile.device);
        setBuildField("PRODUCT", profile.product);

        hookSystemProperties(profile.toProperties());
        Slog.w(TAG, packageName + " -> " + profile.label + " (MODEL now " + Build.MODEL + ")");
    }

    private static void setBuildField(String name, String value) {
        try {
            Field field = Build.class.getDeclaredField(name);
            field.setAccessible(true);
            clearFinal(field);
            field.set(null, value);
        } catch (Throwable t) {
            Slog.e(TAG, "set Build." + name + " failed", t);
        }
    }

    /** ART keeps modifiers in Field.accessFlags; dropping FINAL lets Field.set write it. */
    private static void clearFinal(Field field) throws Exception {
        if (!Modifier.isFinal(field.getModifiers())) return;
        Field accessFlags = Field.class.getDeclaredField("accessFlags");
        accessFlags.setAccessible(true);
        accessFlags.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static void hookSystemProperties(final Map<String, String> overrides) {
        try {
            Class<?> sp = Class.forName("android.os.SystemProperties");
            MethodHook hook = new MethodHook() {
                @Override
                public void afterCall(Pine.CallFrame frame) {
                    Object key = frame.args.length > 0 ? frame.args[0] : null;
                    if (key instanceof String) {
                        String fake = overrides.get(key);
                        if (fake != null) frame.setResult(fake);
                    }
                }
            };
            for (Method m : sp.getDeclaredMethods()) {
                if (!"get".equals(m.getName())) continue;
                Class<?>[] params = m.getParameterTypes();
                if (params.length >= 1 && params[0] == String.class
                        && m.getReturnType() == String.class) {
                    Pine.hook(m, hook);
                }
            }
        } catch (Throwable t) {
            Slog.e(TAG, "hook SystemProperties failed", t);
        }
    }
}
