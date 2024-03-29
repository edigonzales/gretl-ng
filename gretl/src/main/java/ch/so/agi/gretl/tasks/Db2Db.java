package ch.so.agi.gretl.tasks;

import ch.ehi.basics.settings.Settings;
import ch.so.agi.gretl.api.TransferSet;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.Db2DbStep;
import ch.so.agi.gretl.util.Connector;
import ch.so.agi.gretl.util.TaskUtil;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class Db2Db extends DefaultTask {
    private static GretlLogger log = LogEnvironment.getLogger(Db2Db.class);;

    @Nested
    public abstract Database getSourceDb();
    
    @Nested
    public abstract Database getTargetDb();
    
    // Es kann @Input verwendet werden, da die Dateien nicht als File referenziert
    // werden, sondern nur als String. Entsprechend würde Task nicht ausgeführt
    // werden, wenn sich das File zwar ändert aber der Pfad zum File (also der
    // String) nicht.
    @Internal
    public abstract ListProperty<TransferSet> getTransferSets();
    
    @Internal
    public abstract Property<Integer> getBatchSize();
    
    @Internal
    public abstract Property<Integer> getFetchSize();
    
    @Internal
    public abstract Property<Object> getSqlParameters();

    @TaskAction
    public void executeTask() throws Exception {
        String taskName = ((Task) this).getName();
        
        if (!getSourceDb().getUri().isPresent()) {
            IllegalArgumentException e = new IllegalArgumentException("source database url must not be null");
            throw new TaskExecutionException(this, e);
        }
        
        if (!getTargetDb().getUri().isPresent()) {
            IllegalArgumentException e = new IllegalArgumentException("target database url must not be null");
            throw new TaskExecutionException(this, e);
        }
        
        convertToAbsolutePaths(getTransferSets().get());

        log.info(String.format("Start Db2DbTask(Name: %s SourceDb: %s TargetDb: %s Transfers: %s)", taskName, getSourceDb(),
                getTargetDb(), getTransferSets().get()));
        
        Connector sourceConnector = new Connector(getSourceDb().getUri().getOrNull(), 
                getSourceDb().getUser().getOrNull(), 
                getSourceDb().getPassword().getOrNull());

        Connector targetConnector = new Connector(getTargetDb().getUri().getOrNull(), 
                getTargetDb().getUser().getOrNull(), 
                getTargetDb().getPassword().getOrNull());

        Settings settings = new Settings();
        if (getBatchSize().isPresent()) {
            settings.setValue(Db2DbStep.SETTING_BATCH_SIZE, getBatchSize().get().toString());
        }
        if (getFetchSize().isPresent()) {
            settings.setValue(Db2DbStep.SETTING_FETCH_SIZE, getFetchSize().get().toString());
        }
        try {
            Db2DbStep step = new Db2DbStep(taskName);
            if (!getSqlParameters().isPresent()) {
                step.processAllTransferSets(sourceConnector, targetConnector, getTransferSets().get(), settings, null);
            } else if(getSqlParameters().get() instanceof Map) {
                step.processAllTransferSets(sourceConnector, targetConnector, getTransferSets().get(), settings, (Map<String,String>)getSqlParameters().get());
            } else {
                List<Map<String,String>> paramList = (List<Map<String,String>>)getSqlParameters().get();
                for (Map<String,String> sqlParams : paramList) {
                    step.processAllTransferSets(sourceConnector, targetConnector, getTransferSets().get(), settings, sqlParams);
                }
            }
        } catch (Exception e) {
            log.error("Exception in creating / invoking Db2DbStep in Db2DbTask", e);
            throw new TaskExecutionException(this, e);
        }
    }

    private void convertToAbsolutePaths(List<TransferSet> transferSets) {
        for (TransferSet ts : transferSets) {
            File configured = ts.getInputSqlFile();
            File absolutePath = TaskUtil.createAbsolutePath(configured, ((Task) this).getProject());
            ts.setInputSqlFile(absolutePath);
        }
    }
    
    public void sourceDb(Action<Database> configAction) {
        configAction.execute(getSourceDb());
    }

    
    public void targetDb(Action<Database> configAction) {
        configAction.execute(getTargetDb());
    }
}
