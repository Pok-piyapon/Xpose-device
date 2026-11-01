package top.niunaijun.blackbox.core.device;

import java.io.File;
import java.nio.charset.StandardCharsets;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * Per-app choice of {@link DeviceProfile}.
 * <p>
 * Stored as a small text file holding the profile id:
 * blackbox/hotfix/device_profile/u&lt;userId&gt;/&lt;packageName&gt;.
 * Same idea as LibcHookConfig: the virtual app process shares the host uid,
 * so it reads the file directly at startup without any IPC.
 */
public final class DeviceProfileConfig {
    private DeviceProfileConfig() {
    }

    /** @return the chosen profile, or null when the app should see the real device. */
    public static DeviceProfile get(int userId, String packageName) {
        try {
            File file = BEnvironment.getDeviceProfileFile(userId, packageName);
            if (!file.exists()) return null;
            String id = new String(FileUtils.toByteArray(file), StandardCharsets.UTF_8).trim();
            return DeviceProfile.byId(id);
        } catch (Throwable t) {
            return null;
        }
    }

    /** @param profileId a preset id, or null to go back to the real device. */
    public static void set(int userId, String packageName, String profileId) {
        File file = BEnvironment.getDeviceProfileFile(userId, packageName);
        try {
            if (profileId == null || DeviceProfile.byId(profileId) == null) {
                if (file.exists()) file.delete();
                return;
            }
            FileUtils.mkdirs(file.getParentFile());
            FileUtils.writeToFile(profileId.getBytes(StandardCharsets.UTF_8), file);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
