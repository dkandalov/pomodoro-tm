package pomodoro;

import java.applet.Applet;
import java.applet.AudioClip;

public class RingSound {
	private final AudioClip ringSound1 = Applet.newAudioClip(getClass().getResource("/resources/ring.wav"));
	private final AudioClip ringSound2 = Applet.newAudioClip(getClass().getResource("/resources/ring2.wav"));
	private final AudioClip ringSound3 = Applet.newAudioClip(getClass().getResource("/resources/ring3.wav"));

	public void play(int ringVolume) {
		if (ringVolume == 0) return;
		AudioClip audioClip;
		if (ringVolume == 1) audioClip = ringSound1;
		else if (ringVolume == 2) audioClip = ringSound2;
		else if (ringVolume == 3) audioClip = ringSound3;
		else throw new IllegalStateException();
		audioClip.play();
	}
}
