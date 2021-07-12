package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.interlis2.validator.Validator;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;

//@CacheableTask
public abstract class IliValidator extends AbstractValidatorTask {
    private GretlLogger log;

    @TaskAction
    public void validate() {
        log = LogEnvironment.getLogger(IliValidator.class);
        
        Object dataFiles = getDataFiles().get();
        FileCollection dataFilesCollection = null;
        if (dataFiles instanceof FileCollection) {
            dataFilesCollection = (FileCollection)dataFiles;
        } else {
            dataFilesCollection = getProject().files(dataFiles);
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<String>();
        for (File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
            System.out.println(fileName);
        }

        Settings settings = new Settings();
        initSettings(settings);

        validationOk = new Validator().validate(files.toArray(new String[files.size()]), settings);
        if (!validationOk && failOnError.get()) {
            throw new TaskExecutionException(this, new Exception("validation failed"));
        }        
    }
}
