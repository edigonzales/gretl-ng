package ch.so.agi.gretl.tasks;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.SqlExecutorStep;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the task which executes the SqlExecutorStep. 
 */
public abstract class SqlExecutor extends DefaultTask {
    private static GretlLogger log = LogEnvironment.getLogger(SqlExecutor.class);

    @Nested
    public abstract Database getDatabase();

    @Internal
    public abstract ListProperty<String> getSqlFiles();

    @Internal
    public abstract Property<Object> getSqlParameters();

    @TaskAction
    public void executeSQLExecutor() {

        String taskName = this.getName();

        if (!getSqlFiles().isPresent()) {
            throw new GradleException("sqlFiles is null");
        }

        List<File> files = convertToFileList(getSqlFiles().get());

        if (!getDatabase().getUri().isPresent()) {
            IllegalArgumentException e = new IllegalArgumentException("database url must not be null");
            throw new TaskExecutionException(this, e);
        }

        Connector database = new Connector(getDatabase().getUri().get(), 
                getDatabase().getUser().getOrNull(), 
                getDatabase().getPassword().getOrNull());
        
        try {
            SqlExecutorStep step = new SqlExecutorStep(taskName);
            if (!getSqlParameters().isPresent()) {
                step.execute(database, files, null);
            } else if(getSqlParameters().get() instanceof Map) {
                step.execute(database, files, (Map<String,String>)getSqlParameters().get());
            } else {
                List<Map<String,String>> paramList=(List<Map<String,String>>)getSqlParameters().get();
                for(Map<String,String> sqlParams : paramList) {
                    step.execute(database, files, sqlParams);
                }
                
            }
            log.info("Task start");
        } catch (Exception e) {
            log.error("Exception in creating / invoking SqlExecutorStep.", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }

    private List<File> convertToFileList(List<String> filePaths) {
        List<File> files = new ArrayList<>();

        for (String filePath : filePaths) {
            if (filePath == null || filePath.length() == 0)
                throw new IllegalArgumentException("Filepaths must not be null or empty");

            File absolute = TaskUtil.createAbsolutePath(filePath, ((Task) this).getProject());
            files.add(absolute);
        }

        return files;
    }
    
    public void database(Action<Database> configAction) {
        configAction.execute(getDatabase());
    }
}
