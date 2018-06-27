package pt.simdea.verifier

import org.gradle.api.Action
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleConfig
import pt.simdea.verifier.cpd.CpdConfig
import pt.simdea.verifier.detekt.DetektConfig
import pt.simdea.verifier.ktlint.KtLintConfig
import pt.simdea.verifier.lint.LintConfig
import pt.simdea.verifier.pmd.PmdConfig
import pt.simdea.verifier.spotbugs.SpotBugsConfig

class CheckExtension {

    static final String NAME = 'check'

    private final Project project

    CheckstyleConfig checkstyle

    void checkstyle(Action<CheckstyleConfig> action) { action.execute(checkstyle) }

    SpotBugsConfig spotbugs

    void spotbugs(Action<SpotBugsConfig> action) { action.execute(spotbugs) }

    PmdConfig pmd

    void pmd(Action<PmdConfig> action) { action.execute(pmd) }

    CpdConfig cpd

    void cpd(Action<CpdConfig> action) { action.execute(cpd) }

    LintConfig lint

    void lint(Action<LintConfig> action) { action.execute(lint) }

    KtLintConfig ktLint

    void ktLint(Action<KtLintConfig> action) { action.execute(ktLint) }

    DetektConfig detekt

    void detekt(Action<DetektConfig> action) { action.execute(detekt) }

    //ErrorProneConfig errorProne

    //void errorProne(Action<ErrorProneConfig> action) { action.execute(errorProne) }

    CheckExtension(Project project) {
        this.project = project
        checkstyle = new CheckstyleConfig(project)
        spotbugs = new SpotBugsConfig(project)
        pmd = new PmdConfig(project)
        cpd = new CpdConfig(project)
        lint = new LintConfig(project)
        ktLint = new KtLintConfig(project)
        detekt = new DetektConfig(project)
        //errorProne = new ErrorProneConfig(project)
    }

    boolean skip = false

    void skip(boolean skip) { this.skip = skip }

    boolean abortOnError = true

    void abortOnError(boolean abortOnError) { this.abortOnError = abortOnError }

}
