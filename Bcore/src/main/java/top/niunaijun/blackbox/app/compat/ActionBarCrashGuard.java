package top.niunaijun.blackbox.app.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;
import top.niunaijun.blackbox.utils.Slog;

/**
 * Guards against a framework crash seen with fullscreen games in the container.
 * <p>
 * WindowDecorActionBar.init() registers itself as the overlay layout's
 * visibility callback before it has looked up its container view. If the
 * window already has the "hide system bars" flags at that moment, the callback
 * immediately calls hideForSystem() -> doHide(), which dereferences the
 * still-null mContainerView and throws a NullPointerException.
 * <p>
 * While mContainerView is null there is nothing to animate, so doHide/doShow
 * are skipped. The hidden-by-system state is still recorded by hideForSystem()
 * and applied normally once the action bar has finished initialising.
 */
public final class ActionBarCrashGuard {
    private static final String TAG = "ActionBarCrashGuard";
    private static boolean sInstalled;

    private ActionBarCrashGuard() {
    }

    public static synchronized void install() {
        if (sInstalled) return;
        sInstalled = true;
        try {
            Class<?> clazz = Class.forName("com.android.internal.app.WindowDecorActionBar");
            final Field container = clazz.getDeclaredField("mContainerView");
            container.setAccessible(true);

            MethodHook guard = new MethodHook() {
                @Override
                public void beforeCall(Pine.CallFrame frame) {
                    try {
                        if (container.get(frame.thisObject) == null) {
                            frame.setResult(null);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            };

            int hooked = 0;
            for (Method m : clazz.getDeclaredMethods()) {
                String name = m.getName();
                if ("doHide".equals(name) || "doShow".equals(name)) {
                    Pine.hook(m, guard);
                    hooked++;
                }
            }
            Slog.w(TAG, "installed on " + hooked + " methods");
        } catch (Throwable t) {
            Slog.e(TAG, "install failed", t);
        }
    }
}
