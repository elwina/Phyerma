package de.rwth_aachen.phyphox;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//Pins the sync-track artifacts that the offline alignment tooling reads: the WAV container must
//be a canonical PCM16 mono RIFF so any audio tool opens it, and sync.json must carry the
//monotonic anchors that map file frames onto experiment time.
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SyncAudioTrackTest {

    private File baseDir;
    private ExperimentTimeReference timeReference;

    @Before
    public void setUp() {
        baseDir = new File(System.getProperty("java.io.tmpdir"), "synctest-" + System.nanoTime());
        baseDir.mkdirs();
        timeReference = new ExperimentTimeReference(null);
    }

    private SyncAudioTrack recordingTrack() {
        SyncAudioTrack track = new SyncAudioTrack(timeReference, baseDir);
        track.testUsable = true;
        return track;
    }

    @Test
    public void wavIsCanonicalPcm16Mono() throws Exception {
        SyncAudioTrack track = recordingTrack();
        timeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.START);

        short[] samples = new short[4800];
        for (int i = 0; i < samples.length; i++)
            samples[i] = (short) (Math.sin(i * 0.1) * 10000);

        track.beginSegment();
        track.tap(samples, samples.length);
        track.endSegment();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bos);
        track.writeToZip(zip, "0.0-test");
        zip.close();

        Map<String, byte[]> entries = unzip(bos.toByteArray());
        byte[] wav = entries.get("sync/audio_0.wav");
        assertNotNull(wav);

        //RIFF header: "RIFF", size = 36 + dataLen, "WAVE", fmt PCM/mono/48000, dataLen = frames*2
        assertEquals('R', wav[0]); assertEquals('I', wav[1]); assertEquals('F', wav[2]); assertEquals('F', wav[3]);
        assertEquals('W', wav[8]); assertEquals('A', wav[9]); assertEquals('V', wav[10]); assertEquals('E', wav[11]);
        assertEquals(36 + samples.length * 2, le32(wav, 4));
        assertEquals(1, le16(wav, 20)); //PCM
        assertEquals(1, le16(wav, 22)); //mono
        assertEquals(48000, le32(wav, 24)); //sample rate
        assertEquals(48000 * 2, le32(wav, 28)); //byte rate
        assertEquals(16, le16(wav, 34)); //bits per sample
        assertEquals(samples.length * 2, le32(wav, 40));
        assertEquals(44 + samples.length * 2, wav.length);

        //The samples round-trip: first written sample must equal samples[0] little-endian.
        assertEquals(samples[0] & 0xff, wav[44] & 0xff);
        assertEquals((samples[0] >> 8) & 0xff, wav[45] & 0xff);
    }

    @Test
    public void syncJsonCarriesAnchorsAndEvents() throws Exception {
        SyncAudioTrack track = recordingTrack();
        timeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.START);

        track.beginSegment();
        track.tap(new short[9600], 9600);
        track.endSegment();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bos);
        track.writeToZip(zip, "0.0-test");
        zip.close();

        Map<String, byte[]> entries = unzip(bos.toByteArray());
        JSONObject json = new JSONObject(new String(entries.get("sync/sync.json"), "UTF-8"));

        assertEquals(48000, json.getInt("sampleRate"));
        assertEquals("pcm_s16le", json.getString("encoding"));
        assertEquals("elapsedRealtimeNanos", json.getString("timeBase"));
        assertEquals(1, json.getJSONArray("segments").length());

        JSONObject seg = json.getJSONArray("segments").getJSONObject(0);
        assertEquals(9600, seg.getInt("frames"));
        assertEquals("audio_0.wav", seg.getString("file"));
        JSONArray range = seg.getJSONArray("experimentTimeRange");
        assertTrue(range.getDouble(0) < range.getDouble(1));
        //No AudioRecord behind the test seam: getTimestamp never answers, so no anchors.
        assertEquals(0, seg.getJSONArray("anchors").length());

        JSONArray events = json.getJSONArray("events");
        assertEquals("START", events.getJSONObject(0).getString("event"));
    }

    @Test
    public void pauseResumeProducesTwoSegments() throws Exception {
        SyncAudioTrack track = recordingTrack();
        timeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.START);

        track.beginSegment();
        track.tap(new short[4800], 4800);
        track.endSegment();

        timeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.PAUSE);
        timeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.START);

        track.beginSegment();
        track.tap(new short[2400], 2400);
        track.endSegment();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bos);
        track.writeToZip(zip, "0.0-test");
        zip.close();

        Map<String, byte[]> entries = unzip(bos.toByteArray());
        assertNotNull(entries.get("sync/audio_0.wav"));
        assertNotNull(entries.get("sync/audio_1.wav"));

        JSONObject json = new JSONObject(new String(entries.get("sync/sync.json"), "UTF-8"));
        assertEquals(2, json.getJSONArray("segments").length());
    }

    @Test
    public void discardRemovesSession() throws Exception {
        SyncAudioTrack track = recordingTrack();
        timeReference.registerEvent(ExperimentTimeReference.TimeMappingEvent.START);
        track.beginSegment();
        track.tap(new short[4800], 4800);
        track.endSegment();
        assertTrue(track.hasData());

        track.discard();
        assertTrue(!track.hasData());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bos);
        track.writeToZip(zip, "0.0-test");
        zip.close();
        assertTrue(unzip(bos.toByteArray()).isEmpty());
    }

    private static int le16(byte[] b, int off) {
        return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
    }

    private static long le32(byte[] b, int off) {
        return ((long) (b[off] & 0xff)) | ((b[off + 1] & 0xff) << 8) | ((b[off + 2] & 0xff) << 16) | (((long) b[off + 3] & 0xff) << 24);
    }

    private static Map<String, byte[]> unzip(byte[] zipBytes) throws Exception {
        Map<String, byte[]> out = new HashMap<>();
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes));
        ZipEntry e;
        while ((e = zin.getNextEntry()) != null) {
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = zin.read(chunk)) != -1)
                content.write(chunk, 0, n);
            out.put(e.getName(), content.toByteArray());
        }
        return out;
    }
}
