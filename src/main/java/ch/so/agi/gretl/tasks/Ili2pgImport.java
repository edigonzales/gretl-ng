package ch.so.agi.gretl.tasks;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgImport extends Ili2pgAbstractTask {
    @InputFile
    public abstract Property<Object> getDataFile();
    
//    @Nested
//    public abstract Resource getResource(); 

    

    @TaskAction
    public void importData() {
        
        
        System.out.println(getDatabase().getUri().get());
        System.out.println(getDatabase());
        
        Config settings = createConfig();
        int function = Config.FC_IMPORT;
        
        Object dataFile = getDataFile().get();
        FileCollection dataFilesCollection=null;
        if (dataFile instanceof FileCollection) {
            dataFilesCollection = (FileCollection)dataFile;
        }else {
            dataFilesCollection = getProject().files(dataFile);
        }
        if (dataFilesCollection == null || dataFilesCollection.isEmpty()) {
            return;
        }
        List<String> files = new ArrayList<String>();
        for (java.io.File fileObj : dataFilesCollection) {
            String fileName = fileObj.getPath();
            files.add(fileName);
        }
        
        ch.ehi.basics.logging.FileListener fileLogger=null;
        if (getLogFile().isPresent()) {
            // setup logger here, so that multiple file imports result in one logfile
            File logFilepath=this.getProject().file(getLogFile().get().getAsFile());
            fileLogger=new FileLogger(logFilepath);
            EhiLogger.getInstance().addListener(fileLogger);
        }
        try {
            int i=0;
            for(String xtfFilename:files) {
                if (Ili2db.isItfFilename(xtfFilename)) {
                    settings.setItfTransferfile(true);
                } else {
                    settings.setItfTransferfile(false);
                }
                settings.setXtffile(xtfFilename);
                run(function, settings);            
                i++;
            }
        } finally {
            if (fileLogger != null) {
                EhiLogger.getInstance().removeListener(fileLogger);
                fileLogger.close();
                fileLogger=null;
            }
        }
    }

    /*
    public abstract class Resource {
        @Input
        public abstract Property<String> getHostName();
        @Input
        public abstract Property<String> getPath();
    }
    */

}


