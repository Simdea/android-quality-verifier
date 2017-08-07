package pt.simdea.verifier

import org.gradle.api.Action
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleConfig
import pt.simdea.verifier.cpd.CpdConfig
import pt.simdea.verifier.findbugs.FindbugsConfig
import pt.simdea.verifier.pmd.PmdConfig
import pt.simdea.verifier.spotbugs.SpotbugsConfig

class CheckExtension {

    static final String NAME = 'check'

    private final Project project

    CheckstyleConfig checkstyle

    void checkstyle(Action<CheckstyleConfig> action) { action.execute(checkstyle) }

    FindbugsConfig findbugs

    void findbugs(Action<FindbugsConfig> action) { action.execute(findbugs) }

    SpotbugsConfig spotbugs

    void spotbugs(Action<SpotbugsConfig> action) { action.execute(spotbugs) }

    PmdConfig pmd

    void pmd(Action<PmdConfig> action) { action.execute(pmd) }

    CpdConfig cpd

    void cpd(Action<CpdConfig> action) { action.execute(cpd) }

    CheckExtension(Project project) {
        this.project = project
        this.checkstyle = new CheckstyleConfig(project)
        this.findbugs = new FindbugsConfig(project)
        this.spotbugs = new SpotbugsConfig(project)
        this.pmd = new PmdConfig(project)
        this.cpd = new CpdConfig(project)
    }

    boolean skip = false

    void skip(boolean skip) { this.skip = skip }

    boolean abortOnError = true

    void abortOnError(boolean abortOnError) { this.abortOnError = abortOnError }

    static class Lint {
        boolean absolutePaths

        boolean checkAllWarnings

        Set<String> disable

        boolean checkReleaseBuilds

        File reportHTML

        boolean ignoreWarnings

        File config

        boolean showAll

        boolean warningsAsErrors

        File reportXML

        boolean skip
    }

}
