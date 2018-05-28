package pt.simdea.verifier.lint

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class LintConfig extends CommonConfig {

    LintConfig(Project project) { super(project) }

    boolean absolutePaths

    boolean checkAllWarnings

    Set<String> disable

    boolean checkReleaseBuilds

    boolean ignoreWarnings

    boolean showAll

    boolean warningsAsErrors

    @Override
    boolean shouldResolveErrors() {
        return false
    }
}
