package ch.so.agi.gretl.tasks;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public interface Database {
    @Input
    public abstract Property<String> getUri();
    
    @Input
    @Optional
    public abstract Property<String> getUser();
    
    @Input
    @Optional
    public abstract Property<String> getPassword();
}
