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

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import pomodoro.model.PomodoroState.Type.*
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

class PomodoroModelTest {

    @Test fun `do one pomodoro`() {
        PomodoroModel(settings(2000, 1000), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(0))
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(time(0))
            assertThat(state.type, equalTo(RUN))
            assertThat(getProgressMax(), equalTo(2000 * 60))

            onTimer(time(updateInterval))
            assertThat(state.progress, equalTo(updateInterval.toInt() / 1000))

            onTimer(time((updateInterval * 2)))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.progress, equalTo(0))
            assertThat(state.pomodorosAmount, equalTo(1))
        }
    }

    @Test fun `do several pomodoros`() {
        PomodoroModel(settings(2000, 1000), PomodoroState()).apply {
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(time(0))
            assertThat(state.type, equalTo(RUN))

            onTimer(time((updateInterval * 3)))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(1))

            onTimer(time((updateInterval * 4)))
            onUserSwitchToNextState(time((updateInterval * 4)))
            assertThat(state.type, equalTo(RUN))

            onTimer(time((updateInterval * 6)))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(2))
        }
    }

    @Test fun `stop during pomodoro`() {
        PomodoroModel(settings(2000, 1000), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(0))
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(time(0))

            assertThat(state.type, equalTo(RUN))

            onTimer(time(updateInterval))
//        assertThat(state.progress, equalTo(1)) // TODO

            onUserSwitchToNextState(time((updateInterval * 2)))

            assertThat(state.type, equalTo(STOP))
            assertThat(state.pomodorosAmount, equalTo(0))
        }
    }

    @Test fun `auto stop after break`() {
        PomodoroModel(settings(1000, 2000), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            onUserSwitchToNextState(time(0))

            onTimer(time(updateInterval))
            assertThat(state.type, equalTo(BREAK))

            onTimer(time(updateInterval) * 2)
//        assertThat(state.progress, equalTo(updateInterval.toInt() / 1000)) // TODO

            onTimer(time(updateInterval) * 3)
            assertThat(state.type, equalTo(STOP))
//        assertThat(state.progress, equalTo(2 * updateInterval.toInt() / 1000)) // TODO
        }
    }

    @Test fun `after idea restart continue from saved state and finish pomodoro`() {
        PomodoroModel(settings(25), PomodoroState(RUN, RUN, minute(-24), second(-1))).apply {
            onIdeStartup(minute(0))

            assertThat(state.type, equalTo(RUN))
            assertTrue(state.progress > 0)
            assertThat(state.pomodorosAmount, equalTo(0))

            onTimer(minute(1))

            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(1))
        }
    }

    @Test fun `after idea restart should not continue from last state if a lot of time has passed`() {
        // last state was RUN
        var modelState = PomodoroState(RUN, RUN, minute(-60), minute(-20))
        var model = PomodoroModel(settings(minute(25), 2000), modelState)
        model.onIdeStartup(minute(0))

        assertThat(model.state.type, equalTo(STOP))
        assertThat(model.state.progress, equalTo(0))
        assertThat(model.state.pomodorosAmount, equalTo(0))

        // last state was BREAK
        modelState = PomodoroState(BREAK, RUN, minute(-60), minute(-20))
        model = PomodoroModel(settings(minute(6), 2000), modelState)
        model.onIdeStartup(minute(0))

        assertThat(model.state.type, equalTo(STOP))
        assertThat(model.state.progress, equalTo(0))
        assertThat(model.state.pomodorosAmount, equalTo(0))
    }

    @Test fun `save pomodoro model state`() {
        val pomodoroStartTime = minute(-2)
        val modelState = PomodoroState(RUN, RUN, pomodoroStartTime, time(0))
        val model = PomodoroModel(settings(minute(2), 2000), modelState)

        assertThat(model.state.type, equalTo(RUN))

        model.onTimer(time(updateInterval))

        assertThat(modelState.type, equalTo(BREAK))
        assertTrue("model state was updated", modelState.startTime > pomodoroStartTime)
    }

    companion object {
        private val updateInterval = minute(1100L)

        private fun minute(n: Long) = MINUTES.toMillis(n)

        private fun second(n: Long) = SECONDS.toMillis(n)

        private fun time(n: Long) = n

        private fun settings(pomodoroLength: Long): Settings {
            val settings = Settings()
            settings.pomodoroLengthInMinutes = pomodoroLength.toInt()
            return settings
        }

        private fun settings(pomodoroLength: Long, breakLength: Long): Settings {
            return Settings(
                    pomodoroLengthInMinutes = pomodoroLength.toInt(),
                    breakLengthInMinutes = breakLength.toInt()
            )
        }
    }
}
