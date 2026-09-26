package de.rwth_aachen.phyphox.device;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeviceDatabase {
    private static final String TAG = "DeviceDatabase";
    private static final String ASSET = "device-db/phyphox-sensordb.json";

    private static DeviceDatabase instance;

    public final String source;
    public final String importedAt;
    public final int count;
    private final List<DeviceProfile> devices = new ArrayList<>();

    private DeviceDatabase(String source, String importedAt, int count) {
        this.source = source;
        this.importedAt = importedAt;
        this.count = count;
    }

    public static synchronized DeviceDatabase get(Context context) {
        if (instance == null)
            instance = load(context.getApplicationContext());
        return instance;
    }

    private static DeviceDatabase load(Context context) {
        try (InputStream in = context.getAssets().open(ASSET);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            while ((n = reader.read(buf)) >= 0)
                sb.append(buf, 0, n);
            JSONObject root = new JSONObject(sb.toString());
            DeviceDatabase db = new DeviceDatabase(
                    root.optString("source", "phyphox-sensordb"),
                    root.optString("importedAt", ""),
                    root.optInt("count", 0));
            JSONArray arr = root.optJSONArray("devices");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++)
                    db.devices.add(new DeviceProfile(arr.getJSONObject(i)));
            }
            Log.i(TAG, "Loaded " + db.devices.size() + " device rows");
            return db;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load sensor database", e);
            return new DeviceDatabase("missing", "", 0);
        }
    }

    public DeviceProfile matchThisPhone() {
        return match(Build.MODEL, Build.BRAND, Build.MANUFACTURER);
    }

    public DeviceProfile match(String model, String brand, String manufacturer) {
        if (model == null)
            model = "";
        String modelNorm = model.trim();
        DeviceProfile exact = null;
        DeviceProfile loose = null;
        for (DeviceProfile p : devices) {
            if (p.model.equalsIgnoreCase(modelNorm)) {
                if (brandMatches(p, brand, manufacturer))
                    return p;
                if (exact == null)
                    exact = p;
            } else if (loose == null && !modelNorm.isEmpty()
                    && p.model.toLowerCase(Locale.US).contains(modelNorm.toLowerCase(Locale.US))) {
                loose = p;
            }
        }
        return exact != null ? exact : loose;
    }

    private static boolean brandMatches(DeviceProfile p, String brand, String manufacturer) {
        return equalsIgnore(p.brand, brand) || equalsIgnore(p.manufacturer, manufacturer)
                || equalsIgnore(p.brand, manufacturer) || equalsIgnore(p.manufacturer, brand);
    }

    private static boolean equalsIgnore(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    public static String displayBrand(String brand, String manufacturer) {
        String raw = (brand != null && !brand.isEmpty()) ? brand : manufacturer;
        if (raw == null)
            return "";
        String k = raw.toLowerCase(Locale.US);
        if (k.contains("xiaomi") || k.contains("redmi")) return "小米";
        if (k.contains("huawei")) return "华为";
        if (k.contains("honor")) return "荣耀";
        if (k.contains("oppo")) return "OPPO";
        if (k.contains("vivo")) return "vivo";
        if (k.contains("oneplus")) return "一加";
        if (k.contains("realme")) return "真我";
        if (k.contains("meizu")) return "魅族";
        if (k.contains("samsung")) return "三星";
        if (k.contains("google")) return "Google";
        return raw;
    }

    public static String guessOsFlavor() {
        String man = safe(Build.MANUFACTURER).toLowerCase(Locale.US);
        String display = (safe(Build.DISPLAY) + " " + safe(Build.FINGERPRINT)).toLowerCase(Locale.US);
        if (man.contains("xiaomi") || man.contains("redmi"))
            return display.contains("hyperos") ? "HyperOS" : "MIUI / HyperOS";
        if (man.contains("huawei"))
            return display.contains("harmony") ? "HarmonyOS / EMUI" : "EMUI / HarmonyOS";
        if (man.contains("honor"))
            return "MagicOS / HarmonyOS";
        if (man.contains("oppo") || man.contains("oneplus") || man.contains("realme"))
            return "ColorOS";
        if (man.contains("vivo"))
            return display.contains("origin") ? "OriginOS" : "Funtouch / OriginOS";
        if (man.contains("meizu"))
            return "Flyme";
        if (man.contains("samsung"))
            return "One UI";
        return "Android " + Build.VERSION.RELEASE;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
