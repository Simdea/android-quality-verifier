package pt.simdea.verifier.cpd

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class CpdConfig extends CommonConfig {

    int minimumTokenCount = 100
    String language = 'java' // Default, can be overridden in build.gradle

    CpdConfig(Project project) { 
        super(project) 
    }

    @Override
    boolean shouldResolveErrors() {
        return true // Enable error reporting for CPD
    }

    // Setter for minimumTokenCount to allow configuration from build.gradle
    void minimumTokenCount(int count) {
        this.minimumTokenCount = count
    }

    // Setter for language to allow configuration from build.gradle
    void language(String lang) {
        this.language = lang
    }
}
