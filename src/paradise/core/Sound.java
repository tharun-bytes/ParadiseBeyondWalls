package paradise.core;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class Sound {
    private Clip clip;
    private final String[] soundFile = {
            "src/paradise/res/sound/bgm.wav",
            "src/paradise/res/sound/pickup.wav",
            "src/paradise/res/sound/damage.wav",
            "src/paradise/res/sound/dash.wav",
            "src/paradise/res/sound/door.wav"
    };

    public void setFile(int i) {
        try {
            File file = new File(soundFile[i]);
            if (file.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(ais);
            } else {
                System.out.println("Sound file not found: " + soundFile[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) clip.start();
    }

    public void loop() {
        if (clip != null) clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        if (clip != null) clip.stop();
    }
}