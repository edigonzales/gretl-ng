package ch.so.agi.gretl.tasks;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;

public interface Database {
    @Internal
    public abstract Property<String> getUri();
    
    @Internal
    public abstract Property<String> getUser();
    
    @Internal
    public abstract Property<String> getPassword();
}
