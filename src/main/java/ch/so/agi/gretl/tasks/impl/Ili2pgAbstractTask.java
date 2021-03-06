package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.Database;
import ch.so.agi.gretl.util.TaskUtil;
import groovy.lang.Range;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class Ili2pgAbstractTask extends DefaultTask {
    protected GretlLogger log;

    @Nested
    public abstract Database getDatabase();
    
    @Input
    @Optional
    public abstract Property<String> getDbschema();
    
    @Input
    @Optional
    public abstract Property<String> getProxy();

    @Input
    @Optional
    public abstract Property<Integer> getProxyPort();
    
    @Input
    @Optional
    public abstract Property<String> getModels();
    
    @Input
    @Optional
    public abstract Property<String> getModeldir();

    @Input
    @Optional
    public abstract Property<Object> getDataset();
    
    @Input
    @Optional
    public abstract Property<String> getBaskets();
    
    @Input
    @Optional
    public abstract Property<String> getTopics();
    
    @Input
    @Optional
    public Property<Boolean> importTid = getProject().getObjects().property(Boolean.class).convention(false);
    
    public Property<Boolean> getImportTid() {
        return importTid;
    }

    public Property<Boolean> importBid = getProject().getObjects().property(Boolean.class).convention(false);
    
    @Input
    @Optional    
    public Property<Boolean> getImportBid() {
        return importBid;
    }

    @InputFile
    @Optional
    public abstract RegularFileProperty getPreScript();
    
    @InputFile
    @Optional
    public abstract RegularFileProperty getPostScript();
    
    public Property<Boolean> deleteData = getProject().getObjects().property(Boolean.class).convention(false);
    
    @Input
    @Optional
    public Property<Boolean> getDeleteData() {
        return deleteData;
    }

    @OutputFile
    @Optional
    public abstract RegularFileProperty getLogFile();
    
    public Property<Boolean> trace = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getTrace() {
        return trace;
    }

    @InputFile
    @Optional
    public abstract RegularFileProperty getValidConfigFile();

    public Property<Boolean> disableValidation = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getDisableValidation() {
        return disableValidation;
    }

    public Property<Boolean> disableAreaValidation = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getDisableAreaValidation() {
        return disableAreaValidation;
    }

    public Property<Boolean> forceTypeValidation = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getForceTypeValidation() {
        return forceTypeValidation;
    }

    public Property<Boolean> strokeArcs = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getStrokeArcs() {
        return strokeArcs;
    }

    public Property<Boolean> skipPolygonBuilding = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getSkipPolygonBuilding() {
        return skipPolygonBuilding;
    }
    
    public Property<Boolean> skipGeometryErrors = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getSkipGeometryErrors() {
        return skipGeometryErrors;
    }

    public Property<Boolean> iligml20 = getProject().getObjects().property(Boolean.class).convention(false);

    @Input
    @Optional
    public Property<Boolean> getIligml20() {
        return iligml20;
    }

    public Property<Boolean> disableRounding = getProject().getObjects().property(Boolean.class).convention(false);
    
    @Input
    @Optional    
    public Property<Boolean> getDisableRounding() {
        return disableRounding;
    }

    public Property<Boolean> failOnException = getProject().getObjects().property(Boolean.class).convention(true);
    
    @Input
    @Optional
    public Property<Boolean> getFailOnException() {
        return failOnException;
    }

//    @Input
//    @Optional
//    public abstract Property<Range<Integer>> getDatasetSubstring();
    
    protected void run(int function, Config settings) {
        log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

        if (getDatabase() == null) {
            throw new IllegalArgumentException("database must not be null");
        }
        
        settings.setFunction(function);

        if (getProxy().isPresent()) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, getProxy().get());
        }
        if (getProxyPort().isPresent()) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, getProxyPort().get().toString());
        }
        if (getDbschema().isPresent()) {
            settings.setDbschema(getDbschema().get());
        }
        if (getModeldir().isPresent()) {
            settings.setModeldir(getModeldir().get());
        }
        if (getModels().isPresent()) {
            settings.setModels(getModels().get());
        }
        if (getBaskets().isPresent()) {
            settings.setBaskets(getBaskets().get());
        }
        if (getTopics().isPresent()) {
            settings.setTopics(getTopics().get());
        }
        if (importTid.get()) {
            settings.setImportTid(true);
        }
        if (getPreScript().isPresent()) {
            settings.setPreScript(getPreScript().get().getAsFile().getAbsolutePath());
        }
        if (getPostScript().isPresent()) {
            settings.setPreScript(getPostScript().get().getAsFile().getAbsolutePath());
        }
        if (deleteData.get()) {
            settings.setDeleteMode(Config.DELETE_DATA);
        }
        if(function!=Config.FC_IMPORT && function!=Config.FC_UPDATE && function!=Config.FC_REPLACE) {
            if (getLogFile().isPresent()) {
                settings.setLogfile(getLogFile().get().getAsFile().getAbsolutePath());
            }
        }
        if (trace.get()) {
            EhiLogger.getInstance().setTraceFilter(false);
        }
        if (getValidConfigFile().isPresent()) {
            settings.setValidConfigFile(getValidConfigFile().get().getAsFile().getAbsolutePath());
        }
        if (disableValidation.get()) {
            settings.setValidation(false);
        }
        if (disableAreaValidation.get()) {
            settings.setDisableAreaValidation(true);
        }
        if (forceTypeValidation.get()) {
            settings.setOnlyMultiplicityReduction(true);
        }
        if (strokeArcs.get()) {
            Config.setStrokeArcs(settings, Config.STROKE_ARCS_ENABLE);
        }
        if (skipPolygonBuilding.get()) {
            Ili2db.setSkipPolygonBuilding(settings);
        }
        if (skipGeometryErrors.get()) {
            settings.setSkipGeometryErrors(true);
        }
        if (iligml20.get()) {
            settings.setTransferFileFormat(Config.ILIGML20);
        }
        if (disableRounding.get()) {
            settings.setDisableRounding(true);;
        }        
        
        
        Connector database = new Connector(getDatabase().getUri().getOrNull(), 
                getDatabase().getUser().getOrNull(), 
                getDatabase().getPassword().getOrNull());
        try {
            Connection conn = database.connect();
            if (conn == null) {
                throw new IllegalArgumentException("connection must not be null");
            }
            settings.setJdbcConnection(conn);
            Ili2db.readSettingsFromDb(settings);
            Ili2db.run(settings, null);
            conn.commit();
            database.close();
        } catch (Exception e) {
            // TODO: Spezialfall dokumentieren!
            if (e instanceof Ili2dbException && !failOnException.get()) {
                log.lifecycle(e.getMessage());
                return;
            }
            log.error("failed to run ili2pg", e);
            throw new TaskExecutionException(this, e);
        } finally {
            if (!database.isClosed()) {
                try {
                    database.connect().rollback();
                } catch (SQLException e) {
                    log.error("failed to rollback", e);
                } finally {
                    try {
                        database.close();
                    } catch (SQLException e) {
                        log.error("failed to close", e);
                    }
                }
            }
        }
        
    }
    
    public void database(Action<Database> configAction) {
        configAction.execute(getDatabase());
    }

    protected Config createConfig() {
        Config settings = new Config();
        new ch.ehi.ili2pg.PgMain().initConfig(settings);
        return settings;
    }
}
