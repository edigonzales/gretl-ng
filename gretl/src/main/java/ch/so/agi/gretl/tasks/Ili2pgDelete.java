package ch.so.agi.gretl.tasks;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.tasks.TaskAction;

import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

public abstract class Ili2pgDelete extends Ili2pgAbstractTask {

    @TaskAction
    public void deleteData() {
        Config settings = createConfig();
        int function = Config.FC_DELETE;
        if (!getDataset().isPresent()) {
            return;
        }
        List<String> datasetNames=null;
        if (getDataset().get() instanceof String) {
            datasetNames = new ArrayList<String>();
            datasetNames.add((String)getDataset().get());
        } else {
            datasetNames=(List)getDataset().get();
        }
        settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        
        for (String datasetName : datasetNames) {
            settings.setDatasetName(datasetName);
            run(function, settings);
        }
    }
}
