package de.rwth_aachen.phyphox.device;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

import de.rwth_aachen.phyphox.R;

public class DevicePageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Phyphox_DayNight);
        setContentView(R.layout.activity_device_page);
        BottomNavHelper.bind(this, R.id.nav_device);

        DeviceDatabase db = DeviceDatabase.get(this);
        DeviceProfile match = db.matchThisPhone();

        String brand = DeviceDatabase.displayBrand(Build.BRAND, Build.MANUFACTURER);
        String title = brand.isEmpty() ? Build.MODEL : brand + " · " + Build.MODEL;
        ((TextView) findViewById(R.id.deviceTitle)).setText(title);
        ((TextView) findViewById(R.id.deviceSubtitle)).setText(getString(R.string.phyerma_device_internal, Build.MODEL));

        StringBuilder identity = new StringBuilder();
        identity.append(getString(R.string.phyerma_field_brand)).append(": ").append(n(Build.BRAND)).append('\n');
        identity.append(getString(R.string.phyerma_field_manufacturer)).append(": ").append(n(Build.MANUFACTURER)).append('\n');
        identity.append("MODEL: ").append(n(Build.MODEL)).append('\n');
        identity.append("DEVICE: ").append(n(Build.DEVICE)).append('\n');
        identity.append("PRODUCT: ").append(n(Build.PRODUCT)).append('\n');
        identity.append("Android ").append(Build.VERSION.RELEASE)
                .append("  (API ").append(Build.VERSION.SDK_INT).append(")\n");
        identity.append(getString(R.string.phyerma_field_skin)).append(": ").append(DeviceDatabase.guessOsFlavor());
        ((TextView) findViewById(R.id.deviceIdentity)).setText(identity.toString());

        StringBuilder matchText = new StringBuilder();
        if (match == null) {
            matchText.append(getString(R.string.phyerma_db_miss));
        } else {
            matchText.append(getString(R.string.phyerma_db_hit, match.model, match.sampleSize)).append('\n');
            if (match.accelAvg != null)
                matchText.append(String.format(Locale.US, "rest-g = %.4f", match.accelAvg));
            if (match.accelStd != null)
                matchText.append(String.format(Locale.US, "  σ = %.5f", match.accelStd));
            if (match.accelRate != null)
                matchText.append(String.format(Locale.US, "  %.1f Hz", match.accelRate));
            matchText.append('\n');
            if (match.accelName != null)
                matchText.append(getString(R.string.sensorAccelerometer)).append(": ").append(match.accelName).append('\n');
            matchText.append(avail(getString(R.string.sensorGyroscope), match.hasGyro)).append('\n');
            matchText.append(avail(getString(R.string.sensorPressure), match.hasPressure)).append('\n');
            matchText.append(avail(getString(R.string.sensorMagneticField), match.hasMagnetic)).append('\n');
            matchText.append(avail(getString(R.string.sensorLight), match.hasLight));
        }
        matchText.append("\n\n").append(getString(R.string.phyerma_db_meta, db.count, db.importedAt));
        ((TextView) findViewById(R.id.deviceMatch)).setText(matchText.toString());

        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> all = sm != null ? sm.getSensorList(Sensor.TYPE_ALL) : java.util.Collections.emptyList();
        StringBuilder live = new StringBuilder();
        live.append(getString(R.string.phyerma_live_sensors, all.size())).append('\n');
        live.append(present(sm, Sensor.TYPE_ACCELEROMETER, getString(R.string.sensorAccelerometer))).append('\n');
        live.append(present(sm, Sensor.TYPE_GYROSCOPE, getString(R.string.sensorGyroscope))).append('\n');
        live.append(present(sm, Sensor.TYPE_MAGNETIC_FIELD, getString(R.string.sensorMagneticField))).append('\n');
        live.append(present(sm, Sensor.TYPE_PRESSURE, getString(R.string.sensorPressure))).append('\n');
        live.append(present(sm, Sensor.TYPE_LIGHT, getString(R.string.sensorLight)));
        ((TextView) findViewById(R.id.deviceLiveSensors)).setText(live.toString());
    }

    private static String n(String s) {
        return s == null || s.isEmpty() ? "—" : s;
    }

    private String avail(String name, boolean yes) {
        return name + ": " + (yes ? getString(R.string.phyerma_present) : getString(R.string.phyerma_absent));
    }

    private String present(SensorManager sm, int type, String name) {
        boolean yes = sm != null && sm.getDefaultSensor(type) != null;
        return name + ": " + (yes ? getString(R.string.phyerma_present) : getString(R.string.phyerma_absent));
    }
}
