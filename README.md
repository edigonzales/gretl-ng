# gretl-ng

```
./gradlew clean build publishPluginMavenPublicationToMavenLocal publishGretlPluginPluginMarkerMavenPublicationToMavenLocal -x test

./gradlew clean gretl:build gretl:publishPluginMavenPublicationToMavenLocal gretl:publishGretlPluginPluginMarkerMavenPublicationToMavenLocal -x gretl:test
```

```
gradle  validateData --no-daemon -Dorg.gradle.jvmargs=-Xmx2G --init-script init.gradle
```

```
../gradlew tasks --all --init-script init.gradle -i
```

```
docker buildx build --platform linux/amd64,linux/arm64  -t edigonzales/gretl-runtime -f Dockerfile.alpine .
```

```
export GUGUS=$(./gradlew properties --no-daemon --console=plain -q | grep "^version:" | awk '{printf $2}')
```

## todo
- Für Integrationtest neue oder alte Syntax. Jar würde sicher mit neuer funktionieren. Wie sieht es mit dem Dockerimage aus? Da müsste ich ja vorher von lokal das Plugin in das Image (nicht Container) deployen. -> müsste das Image komplett anders builden. ma guckn.
- Use TaskExecutionException instead of GradleException?
- Wie Tasks automatisch dokumentieren? javadoc? Reicht das? Verweis in User Manual auf Javadoc?
- ili2pg Import:
  * 
- move connector from api to ... utils? dito transferset (we'll see)
- Taskname output wenn geloggt wird (siehe db2db step)
- sogis: 
  * entweder leeres settings.gradle oder (deprecated) --settings-file
  * im init-file kommt das Plugin-Management (plugin und repo)


## docs
- ilivalidator: 
  * Ohne Output (z.B. logFile) wird das File nicht validiert ("up-to-date"), falls es zu einem Vorlauf identisch ist. Wird noch etwas am File geändert (eine Ziffer), läuft der Task.
  * Plugins noch nicht implementiert
  * Anderes Logoutput-Verhalten mit `--no-daemon`. Hier dünkt es mich i.O. Ohne `--no-daemon` erscheint mit `-i` alles doppelt. Anderereits scheint es mir sehr unlogisch, das `--no-daemon` im Prinzip nur den Daemon anschliessend wieder killt (aber trotzdem ein Daemon startet)
- ili2pg
  * DatasetSubstring: hat nur Auswirkung, falls FileTree verwendet wird. Ansonsten wird angenommen, dass die korrekten Datasetnamen verwendet werden.
  * Spezialfall ili2dbexception dokumentieren. Warum?
  * Locale.setDefault(new Locale("de_CH")); Ist das nur ein ili1-Problem? Nur ein macOS-Problem? Kommt mir irgendwie bekannt vor.
- db2db:
  * falls nicht vorhanden: geom columns...
- alte vs. neue plugin syntax. Beispiele für beide Varianten.
- Dataset bei ili2pg muss String sein. Oder man führt noch if/else für Integer etc ein und casted.
- Beispiel mit Copy-Task und implizit dependency. 2 Lösungen (entweder explizite Dependency oder via Variable und nicht Property) -> Ah nein. Die Ursache war, dass es nun sehr viele Importtasks gab, die vom selben "into"-Directory Daten lesen. Das verwirrt Gradle. -> einfachste Lösung: Subdirs im temp-Dir.

Für die eigenen Tasks kann man es eventuell umgehen (noch zu testen), wenn man input und output properties mit @Internal annotiert?! Scheint jedenfalls beim ilivalidator-Task genau so zu sein ("InputFiles" -> Internal); Mmmh und jetzt scheinen auch die Copy-Tasks zu gehen, wenn man dependsOn macht?

```
gradle  copyItf1 copyItf2 validateData --no-daemon -Dorg.gradle.jvmargs=-Xmx2G --init-script init.gradle -i
```

```
Execution optimizations have been disabled for task ':validateData' to ensure correctness due to the following reasons:
  - Gradle detected a problem with the following location: '/Users/stefan/Downloads'. Reason: Task ':validateData' uses this output of task ':copyItf1' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed. Please refer to https://docs.gradle.org/7.1/userguide/validation_problems.html#implicit_dependency for more details about this problem.
  - Gradle detected a problem with the following location: '/Users/stefan/Downloads'. Reason: Task ':validateData' uses this output of task ':copyItf2' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed. Please refer to https://docs.gradle.org/7.1/userguide/validation_problems.html#implicit_dependency for more details about this problem.
Invalidating VFS because task ':validateData' failed validation
Caching disabled for task ':validateData' because:
  Build cache is disabled
Task ':validateData' is not up-to-date because:
  Task has not declared any outputs despite executing actions.
/Users/stefan/Downloads/2524.ch.so.agi.av.dm01_ch.itf
/Users/stefan/Downloads/2523.ch.so.agi.av.dm01_ch.itf
ilivalidator-1.11.10-61c230a3331fd24f2c1dc841ee9519e191915440
```


- devdoc:
  * In das Image werden alle Jars (und nur die Jars) in eine flaches Verzeichnis (keine Maven-Repo-Struktur) kopiert. Die geschieht mittels eigenem build.gradle-File, das als Abhängigkeit das GRETL-Plugin und allenfalls weitere 3rd Party Plugins als einfache Abhängigkeit (nicht Plugin-DSL-Syntax) definiert. Damit ist das Docker Image, was die Runtime-Bibliotheken betrifft, offline fähig und ein geschlossenes System. 
  * Es ist möglich mit dem Docker Image die Plugin-DSL-Syntax zu verwenden (also der plugin {} Block). Damit das funktioniert, muss im init.gradle eine weitere Konfiguration für das Plugin-Handling geführt werden. Jedes Plugin im Plugin-Block erwartet ein spezielles Marker-POM-File, das im flachen Verzeichnis nun fehlt. Dies kann mittels speziellen Mapping (Plugin -> Jar) umgangen werden. Dieses Mapping muss für jedes Plugin gemacht werden. D.h. es muss nachgeführt werden, wenn neue Plugins reinkopiert werden.
  * Docker image: Probleme mit libssl und älteren Distro auf Apple Silicon siehe: https://docs.docker.com/docker-for-mac/release-notes/
  * isPresent() für ListProperties lifert immer true zurück?


  * https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html
  * https://docs.gradle.org/current/userguide/lazy_configuration.html#lazy_configuration_reference
  * https://docs.gradle.org/current/userguide/custom_tasks.html
  * https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks
  * https://docs.gradle.org/current/userguide/custom_gradle_types.html
  * https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
  * https://docs.gradle.org/current/userguide/tutorial_using_tasks.html
  * https://docs.gradle.org/7.1/userguide/validation_problems.html#implicit_dependency
  * https://discuss.gradle.org/t/gradle-7-0-seems-to-take-an-overzealous-approach-to-inter-task-dependencies/39656/3