package pomodoro.model

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import pomodoro.model.PomodoroSnapshot.Status.Completed
import pomodoro.model.PomodoroSnapshot.Status.Failed
import pomodoro.model.PomodoroState.Mode.*
import pomodoro.model.time.Duration
import pomodoro.model.time.Time
import pomodoro.model.time.days
import pomodoro.model.time.minutes
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class PomodoroModelTest {

    @Test fun `do one pomodoro`() {
        PomodoroModel(settings(pomodoroDuration = 2, breakDuration = 1), PomodoroState()).apply {
            val startTime = state.startTime
            assertState(Stop, progress = 0.minutes, pomodoros = 0, history = listOf())

            onUserSwitchToNextState(atMinute(0))
            assertThat(progressMax, equalTo(2.minutes))
            assertState(Run, progress = 0.minutes, pomodoros = 0, history = listOf())

            onTimer(atMinute(1))
            assertState(Run, progress = 1.minutes, pomodoros = 0, history = listOf())

            onTimer(atMinute(2))
            assertThat(progressMax, equalTo(1.minutes))
            assertState(Break, progress = 0.minutes, pomodoros = 1,
                    history = listOf(PomodoroSnapshot(startTime = startTime,
                            endTime = startTime.plus(Duration(2)), status = Completed)))
        }
    }

    @Test fun `do two pomodoros`() {
        PomodoroModel(settings(pomodoroDuration = 2, breakDuration = 1), PomodoroState()).apply {
            val startTime = state.startTime
            assertState(Stop, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            assertThat(progressMax, equalTo(2.minutes))
            assertState(Run, progress = 0.minutes, pomodoros = 0)

            onTimer(atMinute(1))
            assertState(Run, progress = 1.minutes, pomodoros = 0)

            onTimer(atMinute(2))
            assertThat(progressMax, equalTo(1.minutes))
            assertState(Break, progress = 0.minutes, pomodoros = 1,
                    history = listOf(PomodoroSnapshot(startTime = startTime,
                    endTime = startTime.plus(Duration(2)), status = Completed)))

            onUserSwitchToNextState(atMinute(3))
            assertThat(progressMax, equalTo(2.minutes))
            assertState(Run, progress = 0.minutes, pomodoros = 1,
                    history = listOf(PomodoroSnapshot(startTime = startTime,
                            endTime = startTime.plus(Duration(2)), status = Completed)))

            onTimer(atMinute(4))
            assertState(Run, progress = 1.minutes, pomodoros = 1)

            onTimer(atMinute(5))
            assertThat(progressMax, equalTo(1.minutes))
            assertState(Break, progress = 0.minutes, pomodoros = 2,
                    history = listOf(
                            PomodoroSnapshot(startTime = startTime, endTime = startTime.plus(Duration(2)),
                                    status = Completed),
                            PomodoroSnapshot(startTime = startTime.plus(Duration(3)),
                                    endTime = startTime.plus(Duration(5)), status = Completed)))
        }
    }

    @Test fun `stop during pomodoro`() {
        PomodoroModel(settings(pomodoroDuration = 5, breakDuration = 1), PomodoroState()).apply {
            val startTime = state.startTime
            assertState(Stop, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            assertState(Run, progress = 0.minutes, pomodoros = 0)

            onTimer(atMinute(1))
            assertState(Run, progress = 1.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(2))
            assertState(Stop, progress = 0.minutes, pomodoros = 0,
                    history = listOf(PomodoroSnapshot(startTime = startTime,
                    endTime = startTime.plus(Duration(2)), status = Failed)))
        }
    }

    @Test fun `timer automatically stops after break`() {
        PomodoroModel(settings(pomodoroDuration = 1, breakDuration = 2), PomodoroState()).apply {
            assertState(Stop, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))

            onTimer(atMinute(1))
            assertState(Break, progress = 0.minutes, pomodoros = 1)
            onTimer(atMinute(2))
            assertState(Break, progress = 1.minutes, pomodoros = 1)

            onTimer(atMinute(3))
            assertState(Stop, progress = 0.minutes, pomodoros = 1)
        }
    }

    @Test fun `new pomodoro is automatically started after break`() {
        val settings = settings(pomodoroDuration = 1, breakDuration = 2).apply {
            startNewPomodoroAfterBreak = true
        }
        PomodoroModel(settings, PomodoroState()).apply {
            assertState(Stop, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))

            onTimer(atMinute(1))
            assertState(Break, progress = 0.minutes, pomodoros = 1)
            onTimer(atMinute(2))
            assertState(Break, progress = 1.minutes, pomodoros = 1)

            onTimer(atMinute(3))
            assertState(Run, progress = 1.minutes, pomodoros = 1)
        }
    }

    @Test fun `long break after four pomodoros`() {
        PomodoroModel(settings(pomodoroDuration = 2, breakDuration = 1), PomodoroState()).apply {
            assertState(Stop, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            onTimer(atMinute(2))
            assertState(Break, progress = 0.minutes, pomodoros = 1)

            onUserSwitchToNextState(atMinute(3))
            onTimer(atMinute(5))
            assertState(Break, progress = 0.minutes, pomodoros = 2)

            onUserSwitchToNextState(atMinute(6))
            onTimer(atMinute(8))
            assertState(Break, progress = 0.minutes, pomodoros = 3)

            onUserSwitchToNextState(atMinute(9))
            onTimer(atMinute(11))
            assertState(Break, progress = 0.minutes, pomodoros = 4)
            onTimer(atMinute(12))
            assertState(Break, progress = 1.minutes, pomodoros = 4)
            onTimer(atMinute(13))
            assertState(Break, progress = 2.minutes, pomodoros = 4)
            onTimer(atMinute(14))
            assertState(Break, progress = 3.minutes, pomodoros = 4)
        }
    }

    @Test fun `after idea restart continue from saved state and finish pomodoro`() {
        val settings = settings(pomodoroDuration = 25, breakDuration = 2)
        PomodoroModel(settings, PomodoroState(Run, Run, atMinute(-20), atMinute(-1))).apply {
            onIdeStartup(atMinute(0))
            assertState(Run, progress = 20.minutes, pomodoros = 0)

            onIdeStartup(atMinute(2))
            assertState(Run, progress = 22.minutes, pomodoros = 0)

            onTimer(atMinute(5))
            assertState(Break, progress = 0.minutes, pomodoros = 1)
        }
    }

    @Test fun `after idea restart should not continue from last state if a lot of time has passed`() {
        val settings = settings(pomodoroDuration = 25, breakDuration = 2)

        // last state was Run
        PomodoroModel(settings, PomodoroState(Run, Run, atMinute(-60), atMinute(-20))).apply {
            onIdeStartup(atMinute(0))
            assertState(Stop, progress = 0.minutes, pomodoros = 0)
        }

        // last state was Break
        PomodoroModel(settings, PomodoroState(Break, Break, atMinute(-60), atMinute(-20))).apply {
            onIdeStartup(atMinute(0))
            assertState(Stop, progress = 0.minutes, pomodoros = 0)
        }
    }

    @Test fun `when settings change, apply them only after the end of pomodoro`() {
        val settings = settings(pomodoroDuration = 2, breakDuration = 1)
        val newSettings = settings(pomodoroDuration = 3, breakDuration = 1)

        PomodoroModel(settings, PomodoroState()).apply {
            assertState(Stop, progress = 0.minutes, pomodoros = 0)

            onUserSwitchToNextState(atMinute(0))
            assertState(Run, progress = 0.minutes, pomodoros = 0)
            settings.loadState(newSettings) // <-- update settings during first pomodoro
            onTimer(atMinute(1))
            assertState(Run, progress = 1.minutes, pomodoros = 0)

            onTimer(atMinute(2))
            assertState(Break, progress = 0.minutes, pomodoros = 1)
            onTimer(atMinute(3))
            assertState(Stop, progress = 0.minutes, pomodoros = 1)

            onUserSwitchToNextState(atMinute(4))
            assertState(Run, progress = 0.minutes, pomodoros = 1)
            onTimer(atMinute(5))
            assertState(Run, progress = 1.minutes, pomodoros = 1)
            onTimer(atMinute(6))
            assertState(Run, progress = 2.minutes, pomodoros = 1)

            onTimer(atMinute(7))
            assertState(Break, progress = 0.minutes, pomodoros = 2)
        }
    }

    @Test fun `compute statistics`() {
        val reportTime = fixedTimeAt("2023-11-05T15:15:30.00Z")
        val threeDaysAgo = reportTime - 3.days
        val tenDaysAgo = reportTime - 10.days
        val fortyDaysAgo = reportTime - 40.days
        val inTheFuture = reportTime + 1.minutes
        val history = listOf(
                PomodoroSnapshot.completed(startTime = reportTime - 2.minutes, endTime = reportTime - 1.minutes),
                PomodoroSnapshot.failed(startTime = reportTime - 4.minutes, endTime = reportTime - 3.minutes),
                PomodoroSnapshot.completed(startTime = reportTime - 6.minutes, endTime = reportTime - 5.minutes),

                PomodoroSnapshot.completed(startTime = threeDaysAgo, endTime = threeDaysAgo + 1.minutes),
                PomodoroSnapshot.completed(startTime = threeDaysAgo + 2.minutes, endTime = threeDaysAgo + 3.minutes),

                PomodoroSnapshot.completed(startTime = tenDaysAgo, endTime = tenDaysAgo + 1.minutes),
                PomodoroSnapshot.failed(startTime = tenDaysAgo + 2.minutes, endTime = tenDaysAgo + 3.minutes),

                PomodoroSnapshot.completed(startTime = fortyDaysAgo, endTime = fortyDaysAgo + 1.minutes),
                PomodoroSnapshot.failed(startTime = fortyDaysAgo + 2.minutes, endTime = fortyDaysAgo + 3.minutes),

                PomodoroSnapshot.completed(startTime = inTheFuture, endTime = inTheFuture + 1.minutes),
        )

        val statistics = PomodoroSnapshot.statisticsAt(reportTime, history)

        assertThat(statistics, equalTo(PomodoroStatistics(
                today = PomodoroCounts(2, 1),
                week = PomodoroCounts(4, 1),
                month = PomodoroCounts(5, 2))))
    }


    companion object {
        private fun PomodoroModel.assertState(stateMode: PomodoroState.Mode, progress: Duration, pomodoros: Int) {
            assertThat(state.mode, equalTo(stateMode))
            assertThat(state.progress, equalTo(progress))
            assertThat(state.pomodorosAmount, equalTo(pomodoros))
        }

        private fun PomodoroModel.assertState(stateMode: PomodoroState.Mode, progress: Duration, pomodoros: Int,
                                              history: List<PomodoroSnapshot>) {
            assertState(stateMode, progress, pomodoros)
            assertThat(state.history, equalTo(history))
        }

        private fun fixedTimeAt(utcDateTime: String) =
                Time(Instant.now(Clock.fixed(Instant.parse(utcDateTime), ZoneOffset.UTC)))

        private fun atMinute(n: Long) = Time.zero + Duration(minutes = n)

        private fun settings(pomodoroDuration: Long, breakDuration: Long) =
            Settings(
                pomodoroDuration = pomodoroDuration.minutes,
                breakDuration = breakDuration.minutes
            )
    }
}

