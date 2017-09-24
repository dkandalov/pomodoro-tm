package pomodoro

import java.applet.Applet

class RingSound {
    fun play(ringVolume: Int) {
        val audioClip = when (ringVolume) {
            0 -> null
            1 -> ringSound1
            2 -> ringSound2
            3 -> ringSound3
            else -> error("")
        }
        audioClip?.play()
    }

    companion object {
        private val ringSound1 = loadSound("/ring.wav")
        private val ringSound2 = loadSound("/ring2.wav")
        private val ringSound3 = loadSound("/ring3.wav")

        private fun loadSound(filePath: String) = Applet.newAudioClip(RingSound::class.java.getResource(filePath))
    }
}
