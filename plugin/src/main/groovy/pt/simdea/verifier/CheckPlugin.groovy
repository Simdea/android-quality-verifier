package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.cpd.CpdCheck
import pt.simdea.verifier.detekt.DetektCheck
import pt.simdea.verifier.findbugs.FindbugsCheck
import pt.simdea.verifier.ktlint.KtlintCheck
import pt.simdea.verifier.lint.LintCheck
import pt.simdea.verifier.pmd.PmdCheck
import pt.simdea.verifier.spotbugs.SpotBugsCheck

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project rootProject) {
        rootProject.extensions.create(CheckExtension.NAME, CheckExtension, rootProject)
        rootProject.repositories.add(rootProject.getRepositories().jcenter())
        //.dependencies.add("implementation", "pt.simdea.verifier.annotations:verifier-annotations:0.0.3")
        //rootProject.dependencies.add("annotationProcessor", "pt.simdea.verifier.annotations:verifier-annotations:0.0.3")

        def hasSubProjects = rootProject.subprojects.size() > 0

        new FindbugsCheck().apply(target)
        //new SpotbugsCheck().apply(target)
        new PmdCheck().apply(target)
        new CpdCheck().apply(target)
        //addLint(target, target.check)
        target.subprojects { subProject ->
            afterEvaluate {
                def extension = target.check

                addLint(subProject, extension)
            }
        }
        new CheckstyleCheck().apply(target)
        new DetektCheck().apply(target)
        new KtlintCheck().apply(target)
    }

    static boolean addLint(final Project subProject, final CheckExtension extension) {
        boolean skip = extension.lint.skip != null ? extension.lint.skip : false
        if (!skip && isAndroidProject(subProject)) {
            subProject.android.lintOptions {
                abortOnError extension.abortOnError != null ? extension.abortOnError : true
                absolutePaths extension.lint.absolutePaths != null ? extension.lint.absolutePaths : true
                checkAllWarnings = extension.lint.checkAllWarnings != null ? extension.lint.checkAllWarnings : false
                checkReleaseBuilds extension.lint.checkReleaseBuilds != null ? extension.lint.checkReleaseBuilds : false
                ignoreWarnings extension.lint.ignoreWarnings != null ? extension.lint.ignoreWarnings : false
                showAll extension.lint.showAll != null ? extension.lint.showAll : false
                warningsAsErrors extension.lint.warningsAsErrors != null ? extension.lint.warningsAsErrors : false
                lintConfig extension.lint.config != null ? new File(extension.lint.config) : resolveConfigFile("lint", subProject)
                if (extension.lint.disable != null)
                    disable extension.lint.disable
            }

            if (extension.lint.reportHTML != null) {
                subProject.android.lintOptions {
                    htmlReport true
                    htmlOutput extension.lint.reportHTML
                }
            }
        } else {
            rootProject.afterEvaluate {
                addTool(rootProject, rootProject)
            }
        }
    }

    private static void addTool(final Project project, final Project rootProject) {
        new PmdCheck().apply(project, rootProject)
        new CheckstyleCheck().apply(project, rootProject)
        new KtLintCheck().apply(project, rootProject)
        new CpdCheck().apply(project, rootProject)
        new DetektCheck().apply(project, rootProject)
        //new ErrorProneCheck().apply(project, rootProject)
        new LintCheck().apply(project, rootProject)
        new SpotBugsCheck().apply(project, rootProject)
    }

}
