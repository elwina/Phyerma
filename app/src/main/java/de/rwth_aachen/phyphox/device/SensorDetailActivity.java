package de.rwth_aachen.phyphox.device;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import de.rwth_aachen.phyphox.R;

public class SensorDetailActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensor;
    private final SampleWindow window = new SampleWindow();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private TextView liveView, statsView, metaView;
    private String unit = "";
    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            renderStats();
            ui.postDelayed(this, 250);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Phyphox_DayNight);
        setContentView(R.layout.activity_sensor_detail);

        int type = getIntent().getIntExtra(SensorLabActivity.EXTRA_TYPE, -1);
        String name = getIntent().getStringExtra(SensorLabActivity.EXTRA_NAME);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            for (Sensor s : sensorManager.getSensorList(type >= 0 ? type : Sensor.TYPE_ALL)) {
                if (name == null || name.equals(s.getName())) {
                    sensor = s;
                    break;
                }
            }
        }

        liveView = findViewById(R.id.sensorDetailLive);
        statsView = findViewById(R.id.sensorDetailStats);
        metaView = findViewById(R.id.sensorDetailMeta);
        findViewById(R.id.sensorDetailBack).setOnClickListener(v -> finish());

        if (sensor == null) {
            ((TextView) findViewById(R.id.sensorDetailTitle)).setText(R.string.phyerma_sensor_missing);
            liveView.setText("—");
            return;
        }

        unit = SensorCatalog.siUnit(sensor.getType());
        String title = getString(SensorCatalog.titleRes(sensor.getType()));
        ((TextView) findViewById(R.id.sensorDetailTitle)).setText(title);
        ((TextView) findViewById(R.id.sensorDetailChip)).setText(sensor.getName());

        DeviceProfile match = DeviceDatabase.get(this).matchThisPhone();
        StringBuilder meta = new StringBuilder();
        meta.append(getString(R.string.phyerma_field_vendor)).append(": ").append(sensor.getVendor()).append('\n');
        meta.append("type = ").append(sensor.getType()).append('\n');
        meta.append(getString(R.string.phyerma_claimed_range)).append(": ").append(sensor.getMaximumRange()).append(' ').append(unit).append('\n');
        meta.append(getString(R.string.phyerma_claimed_res)).append(": ").append(sensor.getResolution()).append(' ').append(unit).append('\n');
        meta.append("minDelay = ").append(sensor.getMinDelay()).append(" µs");
        if (android.os.Build.VERSION.SDK_INT >= 21)
            meta.append("   maxDelay = ").append(sensor.getMaxDelay()).append(" µs");
        String baseline = SensorCatalog.dbBaseline(match, sensor.getType());
        if (baseline != null)
            meta.append("\n\n").append(baseline);
        metaView.setText(meta.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && sensor != null)
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        ui.post(refresh);
    }

    @Override
    protected void onPause() {
        ui.removeCallbacks(refresh);
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        window.add(event.timestamp, event.values);
        if (event.values.length >= 3 && SensorCatalog.is3d(event.sensor.getType())) {
            double abs = Math.sqrt(event.values[0] * event.values[0]
                    + event.values[1] * event.values[1]
                    + event.values[2] * event.values[2]);
            liveView.setText(String.format(Locale.US,
                    "x  %+ .6f\ny  %+ .6f\nz  %+ .6f\n|v| %.6f %s",
                    event.values[0], event.values[1], event.values[2], abs, unit));
        } else if (event.values.length > 0) {
            liveView.setText(String.format(Locale.US, "%.6f %s", event.values[0], unit));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void renderStats() {
        SampleWindow.Snapshot snap = window.snapshot();
        if (snap.n < 2) {
            statsView.setText(R.string.phyerma_collecting);
            return;
        }
        statsView.setText(getString(R.string.phyerma_stats_block,
                snap.formatAbs(unit),
                snap.formatRate(),
                String.format(Locale.US, "%.6f", snap.meanX),
                String.format(Locale.US, "%.6f", snap.meanY),
                String.format(Locale.US, "%.6f", snap.meanZ)));
    }
}
