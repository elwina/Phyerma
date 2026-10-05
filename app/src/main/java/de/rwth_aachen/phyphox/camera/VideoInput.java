package de.rwth_aachen.phyphox.camera;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.rwth_aachen.phyphox.DataBuffer;
import de.rwth_aachen.phyphox.ExperimentTimeReference;
import de.rwth_aachen.phyphox.camera.analyzer.AnalyzingOpenGLRenderer;
import de.rwth_aachen.phyphox.camera.analyzer.VideoEncoder;

//VideoInput is the experiment-side anchor of the <video> input declared in a .phyphox file. It
//holds the requested settings (resolution, frame rate, audio track) and owns the exported files;
//the actual encoding happens in VideoEncoder on the GL thread of AnalyzingOpenGLRenderer, which is
//shared with the camera pipeline - a <camera> and a <video> input in the same experiment ride the
//same camera session.
//
//Every encoded frame's camera timestamp is written to the `t` output buffer in experiment time,
//and the MP4 itself carries the same timestamps as presentation times. Cross-device alignment can
//then use either the wav-style content (a clap in the optional audio track, a flash in the image)
//or simply the frame times.
public class VideoInput implements Serializable {

    private static final String TAG = "VideoInput";

    public final int resolutionHeight; //Requested long side (480/720/1080), device may adjust
    public final double fps;
    public final boolean withAudio;
    public final DataBuffer dataT; //Experiment time of each encoded frame
    private final ExperimentTimeReference timeReference;

    private transient File sessionDir;
    private transient List<Segment> segments = new ArrayList<>();
    private transient Segment current = null;
    public transient String error = null;

    private static class Segment {
        int index;
        File file;
        long startNanos;
        double startExperimentTime;
        double endExperimentTime;
        long frames;
        long framesSubmitted;

        Segment(int index, File file, long startNanos, double startExperimentTime) {
            this.index = index;
            this.file = file;
            this.startNanos = startNanos;
            this.startExperimentTime = startExperimentTime;
        }
    }

    public VideoInput(int resolutionHeight, double fps, boolean withAudio, DataBuffer dataT, ExperimentTimeReference timeReference, File baseDir) {
        this.resolutionHeight = resolutionHeight;
        this.fps = fps;
        this.withAudio = withAudio;
        this.dataT = dataT;
        this.timeReference = timeReference;
        initSessionDir(baseDir);
    }

    private void initSessionDir(File baseDir) {
        if (baseDir == null)
            return;
        sessionDir = new File(baseDir, "video");
        //A leftover directory means the previous session ended without an export (clear, process
        //death). It does not belong to this experiment instance, so it goes away here.
        File[] files = sessionDir.listFiles();
        if (files != null)
            for (File f : files)
                f.delete();
    }

    //Called on the GL executor thread (from AnalyzingOpenGLRenderer). Opens the next segment file.
    public File beginSegmentFile() {
        if (sessionDir == null)
            return null;
        sessionDir.mkdirs();
        File file = new File(sessionDir, "seg_" + segments.size() + ".mp4");
        Segment seg = new Segment(segments.size(), file, SystemClock.elapsedRealtimeNanos(), timeReference.getExperimentTime());
        segments.add(seg);
        current = seg;
        return file;
    }

    //Called on the GL executor thread after the encoder has flushed. framesSubmitted counts the
    //camera frames handed to the codec, framesWritten the packets that made it into the file -
    //they match unless the encoder silently dropped some.
    public void endSegmentFile(long framesWritten, long framesSubmitted) {
        Segment seg = current;
        if (seg == null)
            return;
        current = null;
        seg.endExperimentTime = timeReference.getExperimentTime();
        seg.frames = framesWritten;
        seg.framesSubmitted = framesSubmitted;
        if (framesWritten == 0) {
            seg.file.delete();
            segments.remove(seg);
        }
    }

    public boolean hasData() {
        if (segments == null)
            return false;
        for (Segment seg : segments)
            if (seg.frames > 0)
                return true;
        return false;
    }

    //Clear-data semantics: a clear wipes the run's recordings together with the buffers.
    public void discard() {
        if (segments != null)
            segments.clear();
        current = null;
        if (sessionDir != null) {
            File[] files = sessionDir.listFiles();
            if (files != null)
                for (File f : files)
                    f.delete();
        }
    }

    public void writeToZip(ZipOutputStream zstream, String appVersion) throws IOException {
        if (!hasData())
            return;
        int index = 0;
        for (Segment seg : segments) {
            String name = "video/video_" + index + ".mp4";
            zstream.putNextEntry(new ZipEntry(name));
            FileInputStream in = new FileInputStream(seg.file);
            try {
                byte[] chunk = new byte[8192];
                int n;
                while ((n = in.read(chunk)) != -1)
                    zstream.write(chunk, 0, n);
            } finally {
                in.close();
            }
            zstream.closeEntry();
            index++;
        }
        zstream.putNextEntry(new ZipEntry("video/video.json"));
        zstream.write(toJson(appVersion).toString().getBytes("UTF-8"));
        zstream.closeEntry();
    }

    private JSONObject toJson(String appVersion) {
        JSONObject root = new JSONObject();
        try {
            root.put("app", "Phyerma " + appVersion);
            JSONObject device = new JSONObject();
            device.put("manufacturer", Build.MANUFACTURER);
            device.put("model", Build.MODEL);
            root.put("device", device);
            root.put("resolutionHeight", resolutionHeight);
            root.put("fpsTarget", fps);
            root.put("audio", withAudio);
            root.put("timeBase", "elapsedRealtimeNanos");

            JSONArray segs = new JSONArray();
            for (Segment seg : segments) {
                JSONObject s = new JSONObject();
                s.put("index", seg.index);
                s.put("file", "video_" + seg.index + ".mp4");
                s.put("frames", seg.frames);
                s.put("framesSubmitted", seg.framesSubmitted);
                s.put("startNanos", seg.startNanos);
                s.put("experimentTimeRange", new JSONArray(new double[]{seg.startExperimentTime, seg.endExperimentTime}));
                segs.put(s);
            }
            root.put("segments", segs);

            JSONArray events = new JSONArray();
            for (ExperimentTimeReference.TimeMapping m : timeReference.getTimeMappings()) {
                JSONObject e = new JSONObject();
                e.put("event", m.event.name());
                e.put("experimentTime", m.experimentTime);
                e.put("systemTime", m.systemTime);
                events.put(e);
            }
            root.put("events", events);
        } catch (JSONException e) {
            Log.e(TAG, "Could not build video metadata.", e);
        }
        return root;
    }

}
