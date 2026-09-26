package de.rwth_aachen.phyphox.device;

import java.util.ArrayDeque;
import java.util.Locale;

/** Rolling 2 s window for mean, σ, and measured rate. */
public class SampleWindow {
    private static final long WINDOW_NS = 2_000_000_000L;

    private static final class Sample {
        final long t;
        final float[] v;
        Sample(long t, float[] v) {
            this.t = t;
            this.v = v;
        }
    }

    private final ArrayDeque<Sample> samples = new ArrayDeque<>();

    public synchronized void add(long timestampNs, float[] values) {
        float[] copy = new float[Math.min(values.length, 3)];
        System.arraycopy(values, 0, copy, 0, copy.length);
        samples.addLast(new Sample(timestampNs, copy));
        long cutoff = timestampNs - WINDOW_NS;
        while (!samples.isEmpty() && samples.peekFirst().t < cutoff)
            samples.removeFirst();
    }

    public synchronized Snapshot snapshot() {
        int n = samples.size();
        if (n == 0)
            return Snapshot.empty();
        double sx = 0, sy = 0, sz = 0, sAbs = 0;
        double sAbs2 = 0;
        Sample first = samples.peekFirst();
        Sample last = samples.peekLast();
        for (Sample s : samples) {
            float x = s.v.length > 0 ? s.v[0] : 0;
            float y = s.v.length > 1 ? s.v[1] : 0;
            float z = s.v.length > 2 ? s.v[2] : 0;
            double abs = Math.sqrt(x * x + y * y + z * z);
            sx += x;
            sy += y;
            sz += z;
            sAbs += abs;
            sAbs2 += abs * abs;
        }
        double meanAbs = sAbs / n;
        double var = Math.max(0, sAbs2 / n - meanAbs * meanAbs);
        double dt = last.t - first.t;
        double hz = (n >= 2 && dt > 0) ? (n - 1) * 1e9 / dt : 0;
        return new Snapshot(n, sx / n, sy / n, sz / n, meanAbs, Math.sqrt(var), hz);
    }

    public static final class Snapshot {
        public final int n;
        public final double meanX, meanY, meanZ, meanAbs, stdAbs, rateHz;

        Snapshot(int n, double meanX, double meanY, double meanZ, double meanAbs, double stdAbs, double rateHz) {
            this.n = n;
            this.meanX = meanX;
            this.meanY = meanY;
            this.meanZ = meanZ;
            this.meanAbs = meanAbs;
            this.stdAbs = stdAbs;
            this.rateHz = rateHz;
        }

        static Snapshot empty() {
            return new Snapshot(0, 0, 0, 0, 0, 0, 0);
        }

        public String formatAbs(String unit) {
            if (n < 2)
                return "—";
            return String.format(Locale.US, "%.4f ± %.4f %s", meanAbs, stdAbs, unit);
        }

        public String formatRate() {
            if (n < 2)
                return "—";
            return String.format(Locale.US, "%.1f Hz  (n=%d / 2s)", rateHz, n);
        }
    }
}
