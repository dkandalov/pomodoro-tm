## Pomodoro Timer plugin for IntelliJ IDEs
This is a [pomodoro timer](https://en.wikipedia.org/wiki/Pomodoro_Technique) plugin for IntelliJ IDEs:
 - Choose a task to be accomplished and start pomodoro timer (`ctrl+shift+P` or `cmd+shift+P`).
 - Work on the task until the timer rings. If distracted during pomodoro, restart the timer.
 - Take a break until the timer rings again.

## Why?
Pomodoro timer is great for keeping yourself focused on a particular task and avoiding distractions for a period of time.
It's also useful to make sure that you do take breaks.
Finally, it can be used for tracking how much time is spent on each task.

## Screenshots
![widget](https://raw.githubusercontent.com/dkandalov/pomodoro-tm/master/widget.png)

![settings](https://raw.githubusercontent.com/dkandalov/pomodoro-tm/master/settings.png)

## Credits
The original version of the plugin was written by [Alex Ivanov](https://twitter.com/alexMq0) sometime in 2010.

Widget icons are reused from the pomodoro timer app for OSX (which is no longer available) by [Ugo Landini](https://twitter.com/ugolandini).

## Contributing
All contributions are welcome. You can build the project using [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin),
e.g. `gradle buildPlugin` or `gradle runIde`.