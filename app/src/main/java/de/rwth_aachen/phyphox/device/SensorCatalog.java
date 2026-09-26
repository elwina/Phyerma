package de.rwth_aachen.phyphox.device;

import android.hardware.Sensor;

import de.rwth_aachen.phyphox.R;

public final class SensorCatalog {
    private SensorCatalog() {}

    public static int titleRes(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER: return R.string.sensorAccelerometer;
            case Sensor.TYPE_LINEAR_ACCELERATION: return R.string.sensorLinearAcceleration;
            case Sensor.TYPE_GRAVITY: return R.string.sensorGravity;
            case Sensor.TYPE_GYROSCOPE: return R.string.sensorGyroscope;
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED: return R.string.sensorMagneticField;
            case Sensor.TYPE_PRESSURE: return R.string.sensorPressure;
            case Sensor.TYPE_LIGHT: return R.string.sensorLight;
            case Sensor.TYPE_PROXIMITY: return R.string.sensorProximity;
            case Sensor.TYPE_AMBIENT_TEMPERATURE: return R.string.sensorTemperature;
            case Sensor.TYPE_RELATIVE_HUMIDITY: return R.string.sensorHumidity;
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GAME_ROTATION_VECTOR: return R.string.sensorAttitude;
            default: return R.string.sensorVendor;
        }
    }

    public static String siUnit(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_GRAVITY:
                return "m/s²";
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                return "rad/s";
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                return "µT";
            case Sensor.TYPE_PRESSURE:
                return "hPa";
            case Sensor.TYPE_LIGHT:
                return "lx";
            case Sensor.TYPE_PROXIMITY:
                return "cm";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "°C";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "%";
            default:
                return "";
        }
    }

    public static boolean is3d(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_GRAVITY:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
            case Sensor.TYPE_MAGNETIC_FIELD:
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                return true;
            default:
                return false;
        }
    }

    public static String dbBaseline(DeviceProfile p, int type) {
        if (p == null)
            return null;
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return formatBaseline(p.hasAccelerometer, p.accelAvg, p.accelStd, p.accelRate, p.accelName);
            case Sensor.TYPE_GYROSCOPE:
                return formatBaseline(p.hasGyro, null, p.gyroStd, p.gyroRate, p.gyroName);
            case Sensor.TYPE_MAGNETIC_FIELD:
                return formatBaseline(p.hasMagnetic, null, p.magStd, p.magRate, p.magName);
            case Sensor.TYPE_PRESSURE:
                return formatBaseline(p.hasPressure, null, null, p.pressureRate, p.pressureName);
            case Sensor.TYPE_LIGHT:
                return p.hasLight ? "phyphox 库：有光照传感器" : "phyphox 库：无光照传感器";
            default:
                return null;
        }
    }

    private static String formatBaseline(boolean present, Double avg, Double std, Double rate, String name) {
        if (!present)
            return "phyphox 库：该机型通常没有此传感器";
        StringBuilder sb = new StringBuilder("phyphox 库");
        if (name != null)
            sb.append("：").append(name);
        if (avg != null)
            sb.append(String.format(java.util.Locale.US, "  rest-g=%.4f", avg));
        if (std != null)
            sb.append(String.format(java.util.Locale.US, "  σ=%.5f", std));
        if (rate != null)
            sb.append(String.format(java.util.Locale.US, "  %.1f Hz", rate));
        return sb.toString();
    }
}
