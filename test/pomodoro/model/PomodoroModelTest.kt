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
import java.time.Instant
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

class PomodoroModelTest {

    @Test fun `do one pomodoro`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(0.toDurationMinutes()))
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(time(0))
            assertThat(state.type, equalTo(RUN))
            assertThat(progressMax, equalTo(2.toDurationMinutes()))

            onTimer(minute(1))
            assertThat(state.progress, equalTo(1.toDurationMinutes()))

            onTimer(minute(2))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.progress, equalTo(0.toDurationMinutes()))
            assertThat(state.pomodorosAmount, equalTo(1))
        }
    }

    @Test fun `do several pomodoros`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(minute(0))
            assertThat(state.type, equalTo(RUN))

            onTimer(minute(2))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(1))

            onTimer(minute(4))
            onUserSwitchToNextState(time(4))
            assertThat(state.type, equalTo(RUN))

            onTimer(minute(6))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(2))
        }
    }

    @Test fun `stop during pomodoro`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(0.toDurationMinutes()))
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(minute(0))
            assertThat(state.type, equalTo(RUN))

            onTimer(minute(1))
            assertThat(state.progress, equalTo(1.toDurationMinutes()))

            onUserSwitchToNextState(minute(2))
            assertThat(state.type, equalTo(STOP))
            assertThat(state.pomodorosAmount, equalTo(0))
        }
    }

    @Test fun `auto stop after break`() {
        PomodoroModel(settings(1, 2), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            onUserSwitchToNextState(time(0))

            onTimer(minute(1))
            assertThat(state.type, equalTo(BREAK))

            onTimer(minute(2))
            assertThat(state.progress, equalTo(1.toDurationMinutes()))

            onTimer(minute(3))
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(2.toDurationMinutes()))
        }
    }

    @Test fun `after idea restart continue from saved state and finish pomodoro`() {
        val settings = Settings(25.toDurationMinutes())
        PomodoroModel(settings, PomodoroState(RUN, RUN, minute(-24), second(-1))).apply {
            onIdeStartup(minute(0))

            assertThat(state.type, equalTo(RUN))
            assertTrue(state.progress > 0.toDurationMinutes())
            assertThat(state.pomodorosAmount, equalTo(0))

            onTimer(minute(1))

            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(1))
        }
    }

    @Test fun `after idea restart should not continue from last state if a lot of time has passed`() {
        // last state was RUN
        var modelState = PomodoroState(RUN, RUN, minute(-60), minute(-20))
        var model = PomodoroModel(settings(25, 2), modelState)
        model.onIdeStartup(minute(0))

        assertThat(model.state.type, equalTo(STOP))
        assertThat(model.state.progress, equalTo(0.toDurationMillis()))
        assertThat(model.state.pomodorosAmount, equalTo(0))

        // last state was BREAK
        modelState = PomodoroState(BREAK, RUN, minute(-60), minute(-20))
        model = PomodoroModel(settings(minute(6).toEpochMilli(), 2000), modelState)
        model.onIdeStartup(minute(0))

        assertThat(model.state.type, equalTo(STOP))
        assertThat(model.state.progress, equalTo(0.toDurationMillis()))
        assertThat(model.state.pomodorosAmount, equalTo(0))
    }

    @Test fun `save pomodoro model state`() {
        val pomodoroStartTime = minute(-2)
        val modelState = PomodoroState(RUN, RUN, pomodoroStartTime, time(0))
        val model = PomodoroModel(settings(2, 2), modelState)

        assertThat(model.state.type, equalTo(RUN))

        model.onTimer(minute(1))

        assertThat(modelState.type, equalTo(BREAK))
        assertTrue("model state was updated", modelState.startTime > pomodoroStartTime)
    }

    companion object {
        private fun minute(n: Long) = time(MINUTES.toMillis(n))

        private fun second(n: Long) = time(SECONDS.toMillis(n))

        private fun time(n: Long) = Instant.ofEpochMilli(n)

        private fun settings(pomodoroDuration: Long, breakDuration: Long): Settings {
            return Settings(
                pomodoroDuration = pomodoroDuration.toDurationMinutes(),
                breakDuration = breakDuration.toDurationMinutes()
            )
        }
    }
}

