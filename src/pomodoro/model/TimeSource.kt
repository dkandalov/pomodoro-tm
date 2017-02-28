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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import pomodoro.model.time.Time

/**
 * (Note that [com.intellij.openapi.actionSystem.ActionManager.addTimerListener] won't work as a timer callback)
 */
class TimeSource(private val listener: (Time) -> Unit) {
    @Volatile private var shouldStop = false

    fun start(): TimeSource {
        val application = ApplicationManager.getApplication()
        application.executeOnPooledThread {
            while (!shouldStop) {
                application.invokeLater(
                        { listener(Time.now()) },
                        ModalityState.any() // Use "any" so that timer is updated even while modal dialog like IDE Settings is open.
                )
                Thread.sleep(500)
            }
        }
        return this
    }

    fun stop() {
        shouldStop = true
    }
}
