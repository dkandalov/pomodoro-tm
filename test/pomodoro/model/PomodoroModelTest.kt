package pomodoro.model

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import pomodoro.model.time.minutes

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
            assertState(STOP, progress = 0.minutes, pomodoros = 0)
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
            assertState(STOP, progress = 0.minutes, pomodoros = 1)
        }
    }

    @Test fun `long break after four pomodoros`() {
        PomodoroModel(settings(2, 1), PomodoroState()).apply {
            assertState(STOP, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            onTimer(atMinute(2))
            assertState(BREAK, progress = 0.minutes, pomodoros = 1)

            onUserSwitchToNextState(atMinute(3))
            onTimer(atMinute(5))
            assertState(BREAK, progress = 0.minutes, pomodoros = 2)

            onUserSwitchToNextState(atMinute(6))
            onTimer(atMinute(8))
            assertState(BREAK, progress = 0.minutes, pomodoros = 3)

            onUserSwitchToNextState(atMinute(9))
            onTimer(atMinute(11))
            assertState(BREAK, progress = 0.minutes, pomodoros = 4)
            onTimer(atMinute(12))
            assertState(BREAK, progress = 1.minutes, pomodoros = 4)
            onTimer(atMinute(13))
            assertState(BREAK, progress = 2.minutes, pomodoros = 4)
            onTimer(atMinute(14))
            assertState(BREAK, progress = 3.minutes, pomodoros = 4)
        }
    }

    @Test fun `after idea restart continue from saved state and finish pomodoro`() {
        PomodoroModel(settings(25, 2), PomodoroState(RUN, RUN, atMinute(-20), atMinute(-1))).apply {
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
        private fun PomodoroModel.assertState(stateMode: PomodoroState.Mode, progress: Duration, pomodoros: Int) {
            assertThat(state.mode, equalTo(stateMode))
            assertThat(state.progress, equalTo(progress))
            assertThat(state.pomodorosAmount, equalTo(pomodoros))
        }

        private fun atMinute(n: Long) = Time.ZERO + Duration(minutes = n)

        private fun settings(pomodoroDuration: Long, breakDuration: Long) = Settings(
            pomodoroDuration = pomodoroDuration.minutes,
            breakDuration = breakDuration.minutes
        )
    }
}

