package de.rwth_aachen.phyphox.device;

import org.json.JSONObject;

/**
 * One row from the imported phyphox Sensor Database.
 * Short keys match devices.js (mo/br/ma/aa/...).
 */
public class DeviceProfile {
    public final String model;
    public final String brand;
    public final String manufacturer;
    public final int sampleSize;
    public final boolean hasAccelerometer;
    public final boolean hasGyro;
    public final boolean hasMagnetic;
    public final boolean hasPressure;
    public final boolean hasLight;
    public final boolean hasLinear;
    public final boolean hasTemperature;
    public final boolean hasHumidity;
    public final boolean hasProximity;
    public final Double accelAvg;
    public final Double accelStd;
    public final Double accelRate;
    public final String accelName;
    public final String accelVendor;
    public final Double gyroRate;
    public final Double gyroStd;
    public final String gyroName;
    public final Double magRate;
    public final Double magStd;
    public final String magName;
    public final Double pressureRate;
    public final String pressureName;

    DeviceProfile(JSONObject o) {
        model = o.optString("mo", "");
        brand = o.optString("br", "");
        manufacturer = o.optString("ma", "");
        sampleSize = o.optInt("c", 0);
        hasAccelerometer = o.optBoolean("a", false);
        hasLinear = o.optBoolean("l", false);
        hasGyro = o.optBoolean("g", false);
        hasMagnetic = o.optBoolean("m", false);
        hasPressure = o.optBoolean("p", false);
        hasTemperature = o.optBoolean("t", false);
        hasHumidity = o.optBoolean("h", false);
        hasLight = o.optBoolean("li", false);
        hasProximity = o.optBoolean("pr", false);
        accelAvg = optDouble(o, "aa");
        accelStd = optDouble(o, "as");
        accelRate = optDouble(o, "arat");
        accelName = emptyToNull(o.optString("an", ""));
        accelVendor = emptyToNull(o.optString("av", ""));
        gyroRate = optDouble(o, "grat");
        gyroStd = optDouble(o, "gs");
        gyroName = emptyToNull(o.optString("gn", ""));
        magRate = optDouble(o, "mrat");
        magStd = optDouble(o, "ms");
        magName = emptyToNull(o.optString("mn", ""));
        pressureRate = optDouble(o, "prat");
        pressureName = emptyToNull(o.optString("pn", ""));
    }

    private static Double optDouble(JSONObject o, String key) {
        if (!o.has(key) || o.isNull(key))
            return null;
        try {
            return o.getDouble(key);
        } catch (Exception e) {
            return null;
        }
    }

    private static String emptyToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}
