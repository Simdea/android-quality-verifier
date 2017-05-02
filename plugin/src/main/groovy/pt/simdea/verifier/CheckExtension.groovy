package pt.simdea.verifier

import org.gradle.api.Action
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleConfig
import pt.simdea.verifier.findbugs.FindbugsConfig
import pt.simdea.verifier.pmd.PmdConfig

class CheckExtension {

    static final String NAME = 'verifier'

    private final Project project

    CheckstyleConfig checkstyle

    void checkstyle(Action<CheckstyleConfig> action) { action.execute(checkstyle) }

    FindbugsConfig findbugs

    void findbugs(Action<FindbugsConfig> action) { action.execute(findbugs) }

    PmdConfig pmd

    void pmd(Action<PmdConfig> action) { action.execute(pmd) }

    CheckExtension(Project project) {
        this.project = project
        this.checkstyle = new CheckstyleConfig(project)
        this.findbugs = new FindbugsConfig(project)
        this.pmd = new PmdConfig(project)
    }

    boolean skip = false

    void skip(boolean skip) { this.skip = skip }

    boolean abortOnError = true

    void abortOnError(boolean abortOnError) { this.abortOnError = abortOnError }

    static class Lint {
        boolean absolutePaths

        boolean checkAllWarnings

        boolean checkReleaseBuilds

        File htmlOutput

        boolean htmlReport

        boolean ignoreWarnings

        File lintConfig

        boolean showAll

        boolean warningsAsErrors

        File xmlOutput

        boolean xmlReport
    }

}
