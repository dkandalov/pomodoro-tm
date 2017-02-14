/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pomodoro.model

/**
 * (Note that [com.intellij.openapi.actionSystem.ActionManager.addTimerListener]
 * won't work as a timer callback)
 */
class ControlThread(private val model: PomodoroModel) : Thread() {
    @Volatile private var shouldStop: Boolean = false

    init {
        isDaemon = true
    }

    override fun run() {
        while (!shouldStop) {
            try {
                model.onTimer()
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }

            } catch (e: RuntimeException) {
                // this thread shouldn't be destroyed in case there are coding errors
                e.printStackTrace() // TODO report exception
            }

        }
    }

    fun shouldStop() {
        shouldStop = true
    }
}
