package ch.so.agi.gretl.tasks;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.tasks.impl.AbstractValidatorTask;

//@CacheableTask
public abstract class IliValidator extends AbstractValidatorTask {
    private GretlLogger log;

    @Input
    public abstract Property<String> getDummy();

    
    @TaskAction
    public void validate() {
        
        //getModeldir().convention("gaga");
        
        log = LogEnvironment.getLogger(IliValidator.class);
        
        System.out.println(getModels().get());
        
        System.out.println(this.greeting);
        
        System.out.println(getModeldir().get());
        System.out.println(getModeldir().map(String::toUpperCase).get());
        
        System.out.println(getDummy().get());

    }

}
