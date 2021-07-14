package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.settings.Settings;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.interlis2.validator.Validator;

public abstract class AbstractValidatorTask extends DefaultTask {    
    @InputFiles
    public abstract Property<Object> getDataFiles();
    
    @Internal
    public abstract Property<String> getModels();
    
    @Internal
    public abstract Property<String> getModeldir();
    
    @Internal
    public abstract RegularFileProperty getConfigFile();
        
    @Internal
    public abstract Property<Boolean> getForceTypeValidation();

    @Internal
    public abstract Property<Boolean> getDisableAreaValidation();

    @Internal
    public abstract Property<Boolean> getMultiplicityOff();
    
    @Internal
    public abstract Property<Boolean> getAllObjectsAccessible();
    
    @Internal
    public abstract Property<Boolean> getSkipPolygonBuilding();

    @Internal
    public abstract RegularFileProperty getLogFile();
    
    @Internal
    public abstract RegularFileProperty getXtfLogFile();

//    @Internal
//    public Object pluginFolder = null;
    
    @Internal
    public abstract Property<String> getProxy();

    @Internal
    public abstract Property<Integer> getProxyPort();
    
    @Internal
    public abstract Property<Boolean> getFailOnError();

    protected boolean validationOk = true;

    protected void initSettings(Settings settings) {
        settings.setValue(Validator.SETTING_DISABLE_STD_LOGGER, Validator.TRUE);
        if (getModels().isPresent()) {
            settings.setValue(Validator.SETTING_MODELNAMES, getModels().get());
        }
        if (getModeldir().isPresent()) {
            settings.setValue(Validator.SETTING_ILIDIRS, getModeldir().get());
        }
        if (getConfigFile().isPresent()) {
            settings.setValue(Validator.SETTING_CONFIGFILE, getConfigFile().get().getAsFile().getAbsolutePath());
        }
        if (getForceTypeValidation().isPresent() && getForceTypeValidation().get()) {
            settings.setValue(Validator.SETTING_FORCE_TYPE_VALIDATION, Validator.TRUE);
        }
        if (getDisableAreaValidation().isPresent() && getDisableAreaValidation().get()) {
            settings.setValue(Validator.SETTING_DISABLE_AREA_VALIDATION, Validator.TRUE);
        }
        if (getMultiplicityOff().isPresent() && getMultiplicityOff().get()) {
            settings.setValue(Validator.SETTING_MULTIPLICITY_VALIDATION,
                    ch.interlis.iox_j.validator.ValidationConfig.OFF);
        }
        if (getAllObjectsAccessible().isPresent() && getAllObjectsAccessible().get()) {
            settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, Validator.TRUE);
        }
        if (getSkipPolygonBuilding().isPresent() && getSkipPolygonBuilding().get()) {
            settings.setValue(ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES,
                    ch.interlis.iox_j.validator.Validator.CONFIG_DO_ITF_LINETABLES_DO);
        }
        if (getLogFile().isPresent()) {
            settings.setValue(Validator.SETTING_LOGFILE, getLogFile().get().getAsFile().getAbsolutePath());
        }
        if (getXtfLogFile().isPresent()) {
            settings.setValue(Validator.SETTING_XTFLOG, getXtfLogFile().get().getAsFile().getAbsolutePath());
        }
//        if (pluginFolder != null) {
//            settings.setValue(Validator.SETTING_PLUGINFOLDER, this.getProject().file(pluginFolder).getPath());
//        }
        if (getProxy().isPresent()) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, getProxy().get());
        }
        if (getProxyPort().isPresent()) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, getProxyPort().get().toString());
        }
    }
}
