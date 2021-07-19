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

- devdoc:
  * In das Image werden alle Jars (und nur die Jars) in eine flaches Verzeichnis (keine Maven-Repo-Struktur) kopiert. Die geschieht mittels eigenem build.gradle-File, das als Abhängigkeit das GRETL-Plugin und allenfalls weitere 3rd Party Plugins als einfache Abhängigkeit (nicht Plugin-DSL-Syntax) definiert. Damit ist das Docker Image, was die Runtime-Bibliotheken betrifft, offline fähig und ein geschlossenes System. 
  * Es ist möglich mit dem Docker Image die Plugin-DSL-Syntax zu verwenden (also der plugin {} Block). Damit das funktioniert, muss im init.gradle eine weitere Konfiguration für das Plugin-Handling geführt werden. Jedes Plugin im Plugin-Block erwartet ein spezielles Marker-POM-File, das im flachen Verzeichnis nun fehlt. Dies kann mittels speziellen Mapping (Plugin -> Jar) umgangen werden. Dieses Mapping muss für jedes Plugin gemacht werden. D.h. es muss nachgeführt werden, wenn neue Plugins reinkopiert werden.
  * Docker image: Probleme mit libssl und älteren Distro auf Apple Silicon siehe: https://docs.docker.com/docker-for-mac/release-notes/


  * https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html
  * https://docs.gradle.org/current/userguide/lazy_configuration.html#lazy_configuration_reference
  * https://docs.gradle.org/current/userguide/custom_tasks.html
  * https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks
  * https://docs.gradle.org/current/userguide/custom_gradle_types.html
  * https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
