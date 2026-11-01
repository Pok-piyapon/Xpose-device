package top.niunaijun.blackbox.core.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A device identity that a virtual app sees instead of the real phone.
 * <p>
 * Games commonly decide which graphics options to unlock by reading the
 * device model (Build.MODEL / ro.product.model) and comparing it with an
 * internal list of "known good" phones. Reporting a popular flagship lets
 * those options appear. The real chip, GPU and Android version are left
 * untouched so the game still sizes its workload for the hardware it runs on.
 */
public final class DeviceProfile {
    public final String id;
    public final String label;
    public final String brand;
    public final String manufacturer;
    public final String model;
    public final String device;
    public final String product;
    public final String marketName;

    private DeviceProfile(String id, String label, String brand, String manufacturer,
                          String model, String device, String product, String marketName) {
        this.id = id;
        this.label = label;
        this.brand = brand;
        this.manufacturer = manufacturer;
        this.model = model;
        this.device = device;
        this.product = product;
        this.marketName = marketName;
    }

    /** System property values this profile overrides, keyed by property name. */
    public Map<String, String> toProperties() {
        Map<String, String> map = new LinkedHashMap<>();
        String[] partitions = {"", "system.", "vendor.", "odm.", "product.", "system_ext.", "bootimage."};
        for (String p : partitions) {
            map.put("ro.product." + p + "brand", brand);
            map.put("ro.product." + p + "manufacturer", manufacturer);
            map.put("ro.product." + p + "model", model);
            map.put("ro.product." + p + "device", device);
            map.put("ro.product." + p + "name", product);
        }
        map.put("ro.product.marketname", marketName);
        map.put("ro.vendor.oplus.market.name", marketName);
        return map;
    }

    private static final List<DeviceProfile> PRESETS;

    static {
        List<DeviceProfile> list = new ArrayList<>();
        list.add(new DeviceProfile("s25ultra", "Samsung Galaxy S25 Ultra",
                "samsung", "samsung", "SM-S938B", "pa3q", "pa3qxxx", "Galaxy S25 Ultra"));
        list.add(new DeviceProfile("s24ultra", "Samsung Galaxy S24 Ultra",
                "samsung", "samsung", "SM-S928B", "e3q", "e3qxxx", "Galaxy S24 Ultra"));
        list.add(new DeviceProfile("rog8pro", "ASUS ROG Phone 8 Pro",
                "asus", "asus", "ASUS_AI2401_A", "ASUS_AI2401", "WW_AI2401", "ROG Phone 8 Pro"));
        list.add(new DeviceProfile("redmagic9pro", "nubia RedMagic 9 Pro",
                "nubia", "nubia", "NX769J", "NX769J", "NX769J-EEA", "RedMagic 9 Pro"));
        list.add(new DeviceProfile("xiaomi14", "Xiaomi 14",
                "Xiaomi", "Xiaomi", "23127PN0CG", "houji", "houji_global", "Xiaomi 14"));
        list.add(new DeviceProfile("oneplus12", "OnePlus 12",
                "OnePlus", "OnePlus", "CPH2581", "OP595DL1", "CPH2581EEA", "OnePlus 12"));
        PRESETS = Collections.unmodifiableList(list);
    }

    public static List<DeviceProfile> presets() {
        return PRESETS;
    }

    public static DeviceProfile byId(String id) {
        if (id == null) return null;
        for (DeviceProfile p : PRESETS) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }
}
