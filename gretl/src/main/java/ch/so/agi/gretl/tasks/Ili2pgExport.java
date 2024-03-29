package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgExport extends Ili2pgAbstractTask {
        
    @Internal
    public abstract Property<Boolean> getExport3();
        
    @Internal
    public abstract Property<Boolean> getExportTid();

    @Internal
    public abstract Property<Object> getDataFile();

    @TaskAction
    public void exportData() {
        Locale locale = Locale.getDefault();        
        Locale.setDefault(new Locale("de_CH"));
        
        Config settings = createConfig();
        int function = Config.FC_EXPORT;
        if (!getDataFile().isPresent()) {
            return;
        }
        if (getExport3().isPresent() && getExport3().get()) {
            settings.setVer3_export(true);
        }
        if (getExportTid().isPresent() && getExportTid().get()) {
            settings.setExportTid(true);
        }
        FileCollection dataFilesCollection = null;
        if(getDataFile().get() instanceof FileCollection) {
            dataFilesCollection = (FileCollection)getDataFile().get();
        } else {
            dataFilesCollection = getProject().files(getDataFile().get());
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<String>();
        for (java.io.File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }
        List<String> datasetNames=null;
        if (getDataset().isPresent()) {
            if (getDataset().get() instanceof String) {
                datasetNames = new ArrayList<String>();
                datasetNames.add((String)getDataset().get());
            } else {
                datasetNames = (List)getDataset().get();
            }
            if (files.size() != datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
        }
        
        int i=0;
        for(String xtfFilename:files) {
            if (Ili2db.isItfFilename(xtfFilename)) {
                settings.setItfTransferfile(true);
            }else {
                settings.setItfTransferfile(false);
            }
            if(datasetNames!=null) {
                settings.setDatasetName(datasetNames.get(i));
            }
            settings.setXtffile(xtfFilename);
            run(function, settings);            
            i++;
        }
        Locale.setDefault(locale);
    }
}
