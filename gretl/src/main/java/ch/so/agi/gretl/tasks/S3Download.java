package ch.so.agi.gretl.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.steps.S3DownloadStep;
import ch.so.agi.gretl.util.TaskUtil;

public abstract class S3Download extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public abstract Property<String> getAccessKey();
    
    @Internal
    public abstract Property<String> getSecretKey();

    @Internal
    public abstract Property<String> getBucketName();
    
    @Internal 
    public abstract Property<String> getKey();
    
    @Internal
    public abstract DirectoryProperty getDownloadDir();
    
    @Internal
    public abstract Property<String> getEndPoint();
    
    @Internal
    public abstract Property<String> getRegion();
        
    @TaskAction
    public void upload() {
        log = LogEnvironment.getLogger(S3Download.class);

        if (!getAccessKey().isPresent()) {
            throw new IllegalArgumentException("accessKey must not be null");
        }
        if (!getSecretKey().isPresent()) {
            throw new IllegalArgumentException("secretKey must not be null");
        }
        if (!getDownloadDir().isPresent()) {
            throw new IllegalArgumentException("downloadDir must not be null");
        }
        if (!getBucketName().isPresent()) {
            throw new IllegalArgumentException("bucketName must not be null");
        }
        if (!getKey().isPresent()) {
            throw new IllegalArgumentException("key must not be null");
        }
        if(!getEndPoint().isPresent()) {
            getEndPoint().set("https://s3.eu-central-1.amazonaws.com");
        }
        if (!getRegion().isPresent()) {
            getRegion().set("eu-central-1");
        }        
                
        try {
            S3DownloadStep s3DownloadStep = new S3DownloadStep(this.getName());
            s3DownloadStep.execute(getAccessKey().get(), getSecretKey().get(), 
                    getBucketName().get(), getKey().get(), getEndPoint().get(), 
                    getRegion().get(), getDownloadDir().get().getAsFile());
        } catch (Exception e) {
            log.error("Exception in S3Download task.", e);
            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }
    }
}
