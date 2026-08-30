package paradise.core;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Sound {

    private final Map<String, Object> soundSources = new HashMap<>();
    private Clip musicClip;
    private Clip voiceClip; // Tracks the narrator so we can stop it

    public Sound() {
        findAndRegister("bgm", "bgm.wav");
        findAndRegister("calm", "calm.wav");
        findAndRegister("fight", "fight.wav");
        findAndRegister("pickup", "pickup.wav");
        findAndRegister("damage", "damage.wav");
        findAndRegister("dash", "dash.wav");
        findAndRegister("door", "door.wav");
        findAndRegister("story_voice", "story_voice.wav");
    }

    private void findAndRegister(String key, String fileName) {
        String[] paths = {
                "src/sound/" + fileName,
                "src/paradise/sound/" + fileName,
                "ParadiseBeyondWalls1/src/sound/" + fileName,
                "ParadiseBeyondWalls1/src/paradise/sound/" + fileName,
                "./" + fileName,
                "sound/" + fileName
        };

        for (String path : paths) {
            File f = new File(path);
            if (f.exists()) {
                soundSources.put(key, f);
                System.out.println("[Audio Engine] Found '" + key + "' at: " + f.getAbsolutePath());
                return;
            }
        }

        URL url = getClass().getResource("/sound/" + fileName);
        if (url == null) {
            url = getClass().getResource("/paradise/sound/" + fileName);
        }

        if (url != null) {
            soundSources.put(key, url);
            System.out.println("[Audio Engine] Found '" + key + "' on classpath: " + url);
        } else {
            System.err.println("[Audio Engine] NOT FOUND: '" + fileName + "' (Place inside src/sound/)");
        }
    }

    public void playMusic(String key) {
        stopMusic();
        musicClip = createClip(key);
        if (musicClip != null) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            musicClip.start();
            System.out.println("[Audio Engine] Playing BGM: " + key);
        }
    }

    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
    }

    public void playSFX(String key) {
        new Thread(() -> {
            Clip sfx = createClip(key);
            if (sfx != null) {
                sfx.start();
            }
        }).start();
    }

    // NEW: Play voice on a trackable clip
    public void playVoice(String key) {
        new Thread(() -> {
            voiceClip = createClip(key);
            if (voiceClip != null) {
                voiceClip.start();
            }
        }).start();
    }

    // NEW: Stop voice immediately if skipped
    public void stopVoice() {
        if (voiceClip != null) {
            voiceClip.stop();
            voiceClip.close();
            voiceClip = null;
        }
    }

    private Clip createClip(String key) {
        Object source = soundSources.get(key);
        if (source == null) {
            System.err.println("[Audio Engine] Cannot play: No registered file for key '" + key + "'");
            return null;
        }

        try {
            AudioInputStream in;
            if (source instanceof File) {
                in = AudioSystem.getAudioInputStream((File) source);
            } else {
                in = AudioSystem.getAudioInputStream((URL) source);
            }

            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );

            AudioInputStream decodedIn = AudioSystem.getAudioInputStream(decodedFormat, in);
            DataLine.Info info = new DataLine.Info(Clip.class, decodedFormat);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(decodedIn);
            return clip;

        } catch (Exception e) {
            System.err.println("[Audio Engine] Format Error on key '" + key + "': " + e.getMessage());
            return null;
        }
    }
}