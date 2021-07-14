# gretl-ng

```
./gradlew clean build publishPluginMavenPublicationToMavenLocal publishGretlPluginPluginMarkerMavenPublicationToMavenLocal -x test
```

```
gradle fubar validateData --no-daemon -Dorg.gradle.jvmargs=-Xmx2G
```

## todo
- Use TaskExecutionException instead of GradleException?
- Wie Tasks automatisch dokumentieren? javadoc? Reicht das? Verweis in User Manual auf Javadoc?
- ili2pg Import:
  * 


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

- devdoc:
  * https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html
  * https://docs.gradle.org/current/userguide/lazy_configuration.html#lazy_configuration_reference
  * https://docs.gradle.org/current/userguide/custom_tasks.html
  * https://docs.gradle.org/current/userguide/more_about_tasks.html#sec:up_to_date_checks
  * https://docs.gradle.org/current/userguide/custom_gradle_types.html