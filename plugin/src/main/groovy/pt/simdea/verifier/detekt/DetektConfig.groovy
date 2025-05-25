package pt.simdea.verifier.detekt

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class DetektConfig extends CommonConfig {
    DetektConfig(Project project) {
        super(project)
    }

    @Override
    boolean shouldResolveErrors() {
        return true // Enable error reporting
    }
}
