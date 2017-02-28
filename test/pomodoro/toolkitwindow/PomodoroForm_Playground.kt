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
package pomodoro.toolkitwindow

import pomodoro.model.*
import pomodoro.model.time.Duration
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main(args: Array<String>) {
    val config = Settings(pomodoroDuration = Duration(1), breakDuration = Duration(1))
    val model = PomodoroModel(config, PomodoroState())
    val presenter = PomodoroPresenter(model)
    ControlThread(model).start()

    JFrame().apply {
        contentPane = presenter.contentPane
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        pack()
        isVisible = true
    }
}
