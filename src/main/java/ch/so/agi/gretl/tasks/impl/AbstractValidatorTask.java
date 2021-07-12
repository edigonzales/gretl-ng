package ch.so.agi.gretl.tasks.impl;

import ch.ehi.basics.settings.Settings;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.interlis2.validator.Validator;

public abstract class AbstractValidatorTask extends DefaultTask {    
    @InputFiles
    public abstract Property<Object> getDataFiles();
    
    @Input
    @Optional
    public abstract Property<String> getModels();
    
    @Input
    @Optional
    public abstract Property<String> getModeldir();
    
    @InputFile
    @Optional
    public abstract RegularFileProperty getConfigFile();
    
    public Property<Boolean> forceTypeValidation = getProject().getObjects().property(Boolean.class).convention(false); 
    
    @Input
    @Optional
    public Property<Boolean> getForceTypeValidation() {
        return forceTypeValidation;
    }

    public Property<Boolean> disableAreaValidation = getProject().getObjects().property(Boolean.class).convention(false); 

    @Input
    @Optional
    public Property<Boolean> getDisableAreaValidation() {
        return disableAreaValidation;
    }

    public Property<Boolean> multiplicityOff = getProject().getObjects().property(Boolean.class).convention(false); 

    @Input
    @Optional
    public Property<Boolean> getMultiplicityOff() {
        return multiplicityOff;
    }
    
    public Property<Boolean> allObjectsAccessible = getProject().getObjects().property(Boolean.class).convention(false); 

    @Input
    @Optional
    public Property<Boolean> getAllObjectsAccessible() {
        return allObjectsAccessible;
    }
    
    public Property<Boolean> skipPolygonBuilding = getProject().getObjects().property(Boolean.class).convention(false); 

    @Input
    @Optional
    public Property<Boolean> getSkipPolygonBuilding() {
        return skipPolygonBuilding;
    }

    @OutputFile
    @Optional
    public abstract RegularFileProperty getLogFile();
    
    @OutputFile
    @Optional
    public abstract RegularFileProperty getXtfLogFile();

//    @InputDirectory
//    @Optional
//    public Object pluginFolder = null;
    
    @Input
    @Optional
    public abstract Property<String> getProxy();

    @Input
    @Optional
    public abstract Property<Integer> getProxyPort();
    
    public Property<Boolean> failOnError = getProject().getObjects().property(Boolean.class).convention(true); 

    @Input
    @Optional
    public Property<Boolean> getFailOnError() {
        return failOnError;
    }

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
        if (forceTypeValidation.get()) {
            settings.setValue(Validator.SETTING_FORCE_TYPE_VALIDATION, Validator.TRUE);
        }
        if (disableAreaValidation.get()) {
            settings.setValue(Validator.SETTING_DISABLE_AREA_VALIDATION, Validator.TRUE);
        }
        if (multiplicityOff.get()) {
            settings.setValue(Validator.SETTING_MULTIPLICITY_VALIDATION,
                    ch.interlis.iox_j.validator.ValidationConfig.OFF);
        }
        if (allObjectsAccessible.get()) {
            settings.setValue(Validator.SETTING_ALL_OBJECTS_ACCESSIBLE, Validator.TRUE);
        }
        if (skipPolygonBuilding.get()) {
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
