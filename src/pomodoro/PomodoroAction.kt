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
package pomodoro

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import java.lang.System.currentTimeMillis
import java.time.Instant

class PomodoroAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val pomodoroComponent = ApplicationManager.getApplication().getComponent(PomodoroComponent::class.java) ?: return
        pomodoroComponent.model.onUserSwitchToNextState(Instant.now())
    }
}
