package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgReplace extends Ili2pgAbstractTask {
    @InputFiles
    public abstract Property<Object> getDataFile();

    @TaskAction
    public void replaceData() {
        Config settings = createConfig();
        int function = Config.FC_REPLACE;
        if (!getDataFile().isPresent()) {
            return;
        }
        FileCollection dataFilesCollection=null;
        if (getDataFile().get() instanceof FileCollection) {
            dataFilesCollection=(FileCollection)getDataFile().get();
        } else {
            dataFilesCollection=getProject().files(getDataFile().get());
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<String>();
        for (java.io.File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }

        List<String> datasetNames = null;
        List<Integer> datasetSubstringList = getDatasetSubstring().get();
        if (getDataset().isPresent()) {            
            if (getDataset().get() instanceof String) {
                datasetNames = new ArrayList<String>();
                datasetNames.add((String)getDataset().get());
            } else if (getDataset().get() instanceof FileCollection) {
                Set<File> datasetFiles = ((FileTree)getDataset().get()).getFiles();
                datasetNames = new ArrayList<String>();                
                for (File datasetFile : datasetFiles) {
                    // isPresent() returns empty list (!?)
                    if (getDatasetSubstring().get().size() > 0) { 
                        datasetNames.add(datasetFile.getName().replaceFirst("[.][^.]+$", "")
                                .substring(datasetSubstringList.get(0), 
                                        datasetSubstringList.get(datasetSubstringList.size()-1)));
                    } else {
                        datasetNames.add(datasetFile.getName().replaceFirst("[.][^.]+$", ""));
                    }
                }
            } else {
                datasetNames = new ArrayList<String>();
                // TODO: liefert isPresent() für ein ListProperty immer true zurück?
                if (getDatasetSubstring().get().size() > 0) {
                    List<String> fileNames = (List)getDataset().get();
                    for (String fileName : fileNames) {
                        datasetNames.add(fileName.replaceFirst("[.][^.]+$", "")
                                .substring(datasetSubstringList.get(0), 
                                        datasetSubstringList.get(datasetSubstringList.size()-1)));
                    }
                } else {
                    datasetNames = (List)getDataset().get();
                }
            }
            if (files.size() != datasetNames.size()) {
                throw new GradleException("number of dataset names ("+datasetNames.size()+") doesn't match number of files ("+files.size()+")");
            }
        }
        
        ch.ehi.basics.logging.FileListener fileLogger = null;
        if(getLogFile().isPresent()){
            // setup logger here, so that multiple file imports result in one logfile
            java.io.File logFilepath = getLogFile().get().getAsFile();
            fileLogger = new FileLogger(logFilepath);
            EhiLogger.getInstance().addListener(fileLogger);
        }
        try {
            int i=0;
            for (String xtfFilename:files) {
                if (Ili2db.isItfFilename(xtfFilename)) {
                    settings.setItfTransferfile(true);
                } else {
                    settings.setItfTransferfile(false);
                }
                if(datasetNames != null) {
                    settings.setDatasetName(datasetNames.get(i));
                }
                settings.setXtffile(xtfFilename);
                settings.setBasketHandling(Config.BASKET_HANDLING_READWRITE);
                run(function, settings);            
                i++;
            }
        } finally {
            if (fileLogger != null){
                EhiLogger.getInstance().removeListener(fileLogger);
                fileLogger.close();
                fileLogger = null;
            }
        }
    }
}
