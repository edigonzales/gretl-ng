package ch.so.agi.gretl.util;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;

/**
 * Utility class with methods used in the steps and tasks.
 * @author agi.so.ch
 * @since 1.0.0
 */
public class TaskUtil {

    /**
     * Used to convert a thrown exception into a GradleException. GradleException
     * must be thrown to halt the execution of the Gradle build.
     *
     * GretlException instances are converted, all other exceptions are wrapped
     * (nested).
     * 
     * @param ex the exception that will be converted
     * @return a GradleException
     */
    public static GradleException toGradleException(Exception ex) {
        GradleException res = null;

        String exClassName = ex.getClass().toString();
        String gretlClassName = GretlException.class.toString();

        if (exClassName.equals(gretlClassName)) { // can't use instanceof as must return false for GretlException
                                                  // subclasses.
            res = new GradleException(ex.getMessage());
        } else {
            res = new GradleException("Inner Exception Message: " + ex.getMessage(), ex);
        }
        return res;
    }

    /**
     * Converts the given path relative to the Gradle project to a absolute path and
     * returns the absolute path.
     *
     * @param filePath the path of a file
     * @param gradleProject the Gradle project
     * @return the absolute path of the file
     */
    public static File createAbsolutePath(Object filePath, Project gradleProject) {
        File absolute = gradleProject.file(filePath);
        return absolute;
    }
}
