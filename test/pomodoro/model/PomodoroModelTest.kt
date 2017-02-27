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
import org.junit.Test
import pomodoro.model.PomodoroState.Type.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.MINUTES

class PomodoroModelTest {

    @Test fun `do one pomodoro`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertState(STOP, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            assertThat(progressMax, equalTo(2.minutes))
            assertState(RUN, progress = 0.minutes, pomodoros = 0)

            onTimer(atMinute(1))
            assertState(RUN, progress = 1.minutes, pomodoros = 0)

            onTimer(atMinute(2))
            assertThat(progressMax, equalTo(1.minutes))
            assertState(BREAK, progress = 0.minutes, pomodoros = 1)
        }
    }


    @Test fun `do two pomodoros`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertState(STOP, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            assertState(RUN, progress = 0.minutes, pomodoros = 0)

            onTimer(atMinute(2))
            assertState(BREAK, progress = 0.minutes, pomodoros = 1)

            onUserSwitchToNextState(atMinute(4))
            assertState(RUN, progress = 0.minutes, pomodoros = 1)

            onTimer(atMinute(6))
            assertState(BREAK, progress = 0.minutes, pomodoros = 2)
        }
    }

    @Test fun `stop during pomodoro`() {
        PomodoroModel(settings(5, 1), PomodoroState()).apply {
            assertState(STOP, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            assertState(RUN, progress = 0.minutes, pomodoros = 0)

            onTimer(atMinute(1))
            assertState(RUN, progress = 1.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(2))
            assertState(STOP, progress = 2.minutes, pomodoros = 0)
        }
    }

    @Test fun `auto stop after break`() {
        PomodoroModel(settings(1, 2), PomodoroState()).apply {
            assertState(STOP, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))

            onTimer(atMinute(1))
            assertState(BREAK, progress = 0.minutes, pomodoros = 1)

            onTimer(atMinute(2))
            assertState(BREAK, progress = 1.minutes, pomodoros = 1)

            onTimer(atMinute(3))
            assertState(STOP, progress = 2.minutes, pomodoros = 1)
        }
    }

    @Test fun `after idea restart continue from saved state and finish pomodoro`() {
        val settings = Settings(25.minutes)
        PomodoroModel(settings, PomodoroState(RUN, RUN, atMinute(-20), atMinute(-1))).apply {
            onIdeStartup(atMinute(0))
            assertState(RUN, progress = 20.minutes, pomodoros = 0)

            onIdeStartup(atMinute(2))
            assertState(RUN, progress = 22.minutes, pomodoros = 0)

            onTimer(atMinute(5))
            assertState(BREAK, progress = 0.minutes, pomodoros = 1)
        }
    }

    @Test fun `after idea restart should not continue from last state if a lot of time has passed`() {
        // last state was RUN
        PomodoroModel(settings(25, 2), PomodoroState(RUN, RUN, atMinute(-60), atMinute(-20))).apply {
            onIdeStartup(atMinute(0))
            assertState(STOP, progress = 0.minutes, pomodoros = 0)
        }

        // last state was BREAK
        PomodoroModel(settings(25, 2), PomodoroState(BREAK, BREAK, atMinute(-60), atMinute(-20))).apply {
            onIdeStartup(atMinute(0))
            assertState(STOP, progress = 0.minutes, pomodoros = 0)
        }
    }

    companion object {
        private fun PomodoroModel.assertState(stateType: PomodoroState.Type, progress: Duration, pomodoros: Int) {
            assertThat(state.type, equalTo(stateType))
            assertThat(state.progress, equalTo(progress))
            assertThat(state.pomodorosAmount, equalTo(pomodoros))
        }

        private fun atMinute(n: Long) = Instant.ofEpochMilli(MINUTES.toMillis(n))

        private fun settings(pomodoroDuration: Long, breakDuration: Long) = Settings(
            pomodoroDuration = pomodoroDuration.minutes,
            breakDuration = breakDuration.minutes
        )
    }
}

