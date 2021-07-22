package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.Database;
import ch.so.agi.gretl.util.Connector;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskExecutionException;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Ili2pgAbstractTask extends DefaultTask {
    protected GretlLogger log;

    @Nested
    public abstract Database getDatabase();
    
    @Internal
    public abstract Property<String> getDbschema();
    
    @Internal
    public abstract Property<String> getProxy();

    @Internal
    public abstract Property<Integer> getProxyPort();
        
    @Internal
    public abstract Property<String> getModels();
    
    @Internal
    public abstract Property<String> getModeldir();

    @Internal
    public abstract Property<Object> getDataset();
    
    @Internal
    public abstract Property<String> getBaskets();
    
    @Internal
    public abstract Property<String> getTopics();
        
    @Internal
    public abstract Property<Boolean> getImportTid();
    
    @Internal
    public abstract Property<Boolean> getImportBid();

    @Internal
    public abstract RegularFileProperty getPreScript();
    
    @Internal
    public abstract RegularFileProperty getPostScript();
        
    @Internal
    public abstract Property<Boolean> getDeleteData();

    @Internal
    public abstract RegularFileProperty getLogFile();
    
    @Internal
    public abstract Property<Boolean> getTrace();

    @Internal
    public abstract RegularFileProperty getValidConfigFile();

    @Internal
    public abstract Property<Boolean> getDisableValidation();

    @Internal
    public abstract Property<Boolean> getDisableAreaValidation();

    @Internal
    public abstract Property<Boolean> getForceTypeValidation();

    @Internal
    public abstract Property<Boolean> getStrokeArcs();

    @Internal
    public abstract Property<Boolean> getSkipPolygonBuilding();
    
    @Internal
    public abstract Property<Boolean> getSkipGeometryErrors();

    @Internal
    public abstract Property<Boolean> getIligml20();
    
    @Internal
    public abstract Property<Boolean> getDisableRounding();
    
    @Internal
    public abstract Property<Boolean> getFailOnException();

    // see: https://discuss.gradle.org/t/proper-way-of-using-a-range-property/40478
    @Internal
    public abstract ListProperty<Integer> getDatasetSubstring();
    
    protected void run(int function, Config settings) {
        log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

        if (getDatabase() == null && function != Config.FC_SCRIPT) {
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
        if (getImportTid().isPresent() && getImportTid().get()) {
            settings.setImportTid(true);
        }
        if (getImportBid().isPresent() && getImportBid().get()) {
            settings.setImportBid(true);
        }
        if (getPreScript().isPresent()) {
            settings.setPreScript(getPreScript().get().getAsFile().getAbsolutePath());
        }
        if (getPostScript().isPresent()) {
            settings.setPreScript(getPostScript().get().getAsFile().getAbsolutePath());
        }
        if (getDeleteData().isPresent() && getDeleteData().get()) {
            settings.setDeleteMode(Config.DELETE_DATA);
        }
        if(function!=Config.FC_IMPORT && function!=Config.FC_UPDATE && function!=Config.FC_REPLACE) {
            if (getLogFile().isPresent()) {
                settings.setLogfile(getLogFile().get().getAsFile().getAbsolutePath());
            }
        }
        if (getTrace().isPresent() && getTrace().get()) {
            EhiLogger.getInstance().setTraceFilter(false);
        }
        if (getValidConfigFile().isPresent()) {
            settings.setValidConfigFile(getValidConfigFile().get().getAsFile().getAbsolutePath());
        }
        if (getDisableValidation().isPresent() && getDisableValidation().get()) {
            settings.setValidation(false);
        }
        if (getDisableAreaValidation().isPresent() && getDisableAreaValidation().get()) {
            settings.setDisableAreaValidation(true);
        }
        if (getForceTypeValidation().isPresent() && getForceTypeValidation().get()) {
            settings.setOnlyMultiplicityReduction(true);
        }
        if (getStrokeArcs().isPresent() && getStrokeArcs().get()) {
            Config.setStrokeArcs(settings, Config.STROKE_ARCS_ENABLE);
        }
        if (getSkipPolygonBuilding().isPresent() && getSkipPolygonBuilding().get()) {
            Ili2db.setSkipPolygonBuilding(settings);
        }
        if (getSkipGeometryErrors().isPresent() && getSkipGeometryErrors().get()) {
            settings.setSkipGeometryErrors(true);
        }
        if (getIligml20().isPresent() && getIligml20().get()) {
            settings.setTransferFileFormat(Config.ILIGML20);
        }
        if (getDisableRounding().isPresent() && getDisableRounding().get()) {
            settings.setDisableRounding(true);
        }     
        if (!getFailOnException().isPresent()) {
            getFailOnException().set(true);
        }
        
        if (function == Config.FC_SCRIPT) {
            try {
                Ili2db.run(settings, null);
            } catch (Ili2dbException e) {
                log.error(e.getMessage(), e);
                throw new TaskExecutionException(this, e);
            }
        } else {
            Connector database = new Connector(getDatabase().getUri().get(), 
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
                if (e instanceof Ili2dbException && !getFailOnException().get()) {
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
