package pomodoro

import java.applet.Applet

class RingSound {
    fun play(ringVolume: Int) {
        if (ringVolume == 0) return

        val audioClip = if (ringVolume == 1) ringSound1
            else if (ringVolume == 2) ringSound2
            else if (ringVolume == 3) ringSound3
            else throw IllegalStateException()

        audioClip.play()
    }

    companion object {
        private val ringSound1 = loadSound("/resources/ring.wav")
        private val ringSound2 = loadSound("/resources/ring2.wav")
        private val ringSound3 = loadSound("/resources/ring3.wav")

        private fun loadSound(filePath: String) = Applet.newAudioClip(RingSound::class.java.getResource(filePath))
    }
}
