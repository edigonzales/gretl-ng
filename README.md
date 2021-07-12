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
  * Soll hier dataset etc. unterstützt werden? Mir wäre lieber nicht. Dafür gibt es ja den Replace-Task. Dann müsste man konsequenterweise aber den Support aus der Mutterklasse löschen, was wiederum zu Verdopplungen führt (in delete etc.).


## docs
- ilivalidator: 
  * Ohne Output (z.B. logFile) wird das File nicht validiert ("up-to-date"), falls es zu einem Vorlauf identisch ist. Wird noch etwas am File geändert (eine Ziffer), läuft der Task.
  * Plugins noch nicht implementiert
  * Anderes Logoutput-Verhalten mit `--no-daemon`. Hier dünkt es mich i.O. Ohne `--no-daemon` erscheint mit `-i` alles doppelt. Anderereits scheint es mir sehr unlogisch, das `--no-daemon` im Prinzip nur den Daemon anschliessend wieder killt (aber trotzdem ein Daemon startet)
- ili2pg
  * Import: Falls kein Dataset-Support, muss dokumentiert werden, dass Properties ignoriert werden.