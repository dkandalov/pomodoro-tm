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

class PomodoroModelTest {

    @Test fun `do one pomodoro`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(0.minutes))
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(atMinute(0))
            assertThat(state.type, equalTo(RUN))
            assertThat(progressMax, equalTo(2.minutes))

            onTimer(atMinute(1))
            assertThat(state.progress, equalTo(1.minutes))

            onTimer(atMinute(2))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.progress, equalTo(0.minutes))
            assertThat(state.pomodorosAmount, equalTo(1))
        }
    }

    @Test fun `do several pomodoros`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(atMinute(0))
            assertThat(state.type, equalTo(RUN))

            onTimer(atMinute(2))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(1))

            onTimer(atMinute(4))
            onUserSwitchToNextState(atMinute(4))
            assertThat(state.type, equalTo(RUN))

            onTimer(atMinute(6))
            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(2))
        }
    }

    @Test fun `stop during pomodoro`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(0.minutes))
            assertThat(state.pomodorosAmount, equalTo(0))

            onUserSwitchToNextState(atMinute(0))
            assertThat(state.type, equalTo(RUN))

            onTimer(atMinute(1))
            assertThat(state.progress, equalTo(1.minutes))

            onUserSwitchToNextState(atMinute(2))
            assertThat(state.type, equalTo(STOP))
            assertThat(state.pomodorosAmount, equalTo(0))
        }
    }

    @Test fun `auto stop after break`() {
        PomodoroModel(settings(1, 2), PomodoroState()).apply {
            assertThat(state.type, equalTo(STOP))
            onUserSwitchToNextState(atMinute(0))

            onTimer(atMinute(1))
            assertThat(state.type, equalTo(BREAK))

            onTimer(atMinute(2))
            assertThat(state.progress, equalTo(1.minutes))

            onTimer(atMinute(3))
            assertThat(state.type, equalTo(STOP))
            assertThat(state.progress, equalTo(2.minutes))
        }
    }

    @Test fun `after idea restart continue from saved state and finish pomodoro`() {
        val settings = Settings(25.minutes)
        PomodoroModel(settings, PomodoroState(RUN, RUN, atMinute(-24), atMinute(-1))).apply {
            onIdeStartup(atMinute(0))

            assertThat(state.type, equalTo(RUN))
            assertTrue(state.progress > 0.minutes)
            assertThat(state.pomodorosAmount, equalTo(0))

            onTimer(atMinute(1))

            assertThat(state.type, equalTo(BREAK))
            assertThat(state.pomodorosAmount, equalTo(1))
        }
    }

    @Test fun `after idea restart should not continue from last state if a lot of time has passed`() {
        // last state was RUN
        var modelState = PomodoroState(RUN, RUN, atMinute(-60), atMinute(-20))
        var model = PomodoroModel(settings(25, 2), modelState)
        model.onIdeStartup(atMinute(0))

        assertThat(model.state.type, equalTo(STOP))
        assertThat(model.state.progress, equalTo(0.minutes))
        assertThat(model.state.pomodorosAmount, equalTo(0))

        // last state was BREAK
        modelState = PomodoroState(BREAK, RUN, atMinute(-60), atMinute(-20))
        model = PomodoroModel(settings(25, 2), modelState)
        model.onIdeStartup(atMinute(0))

        assertThat(model.state.type, equalTo(STOP))
        assertThat(model.state.progress, equalTo(0.minutes))
        assertThat(model.state.pomodorosAmount, equalTo(0))
    }

    @Test fun `save pomodoro model state`() {
        val pomodoroStartTime = atMinute(-2)
        val modelState = PomodoroState(RUN, RUN, pomodoroStartTime, atMinute(0))
        val model = PomodoroModel(settings(2, 2), modelState)

        assertThat(model.state.type, equalTo(RUN))

        model.onTimer(atMinute(1))

        assertThat(modelState.type, equalTo(BREAK))
        assertTrue("model state was updated", modelState.startTime > pomodoroStartTime)
    }

    companion object {
        private fun atMinute(n: Long) = Instant.ofEpochMilli(MINUTES.toMillis(n))

        private fun settings(pomodoroDuration: Long, breakDuration: Long) = Settings(
            pomodoroDuration = pomodoroDuration.minutes,
            breakDuration = breakDuration.minutes
        )
    }
}

