package pomodoro

import java.applet.Applet

class RingSound {
    private val ringSound1 = Applet.newAudioClip(javaClass.getResource("/resources/ring.wav"))
    private val ringSound2 = Applet.newAudioClip(javaClass.getResource("/resources/ring2.wav"))
    private val ringSound3 = Applet.newAudioClip(javaClass.getResource("/resources/ring3.wav"))

    fun play(ringVolume: Int) {
        if (ringVolume == 0) return

        val audioClip = if (ringVolume == 1) ringSound1
            else if (ringVolume == 2) ringSound2
            else if (ringVolume == 3) ringSound3
            else throw IllegalStateException()

        audioClip.play()
    }
}
