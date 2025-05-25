package pt.simdea.verifier.ktlint

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class KtLintConfig extends CommonConfig {

    KtLintConfig(Project project) {
        super(project)
    }

    @Override
    boolean shouldResolveErrors() {
        return true // Enable error reporting
    }

}
