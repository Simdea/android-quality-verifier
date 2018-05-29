package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.cpd.CpdCheck
import pt.simdea.verifier.detekt.DetektCheck
import pt.simdea.verifier.errorprone.ErrorProneCheck
import pt.simdea.verifier.ktlint.KtLintCheck
import pt.simdea.verifier.lint.LintCheck
import pt.simdea.verifier.pmd.PmdCheck
import pt.simdea.verifier.spotbugs.SpotBugsCheck

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project rootProject) {
        rootProject.extensions.create(CheckExtension.NAME, CheckExtension, rootProject)
        rootProject.repositories.add(rootProject.getRepositories().jcenter())
        rootProject.dependencies.add("api", "pt.simdea.verifier.annotations:verifier-annotations:0.0.3")
        rootProject.dependencies.add("annotationProcessor", "pt.simdea.verifier.annotations:verifier-annotations:0.0.3")

        def hasSubProjects = rootProject.subprojects.size() > 0

        if (hasSubProjects) {
            rootProject.subprojects { subProject ->
                afterEvaluate {
                    addTool(subProject, rootProject)
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
        new ErrorProneCheck().apply(project, rootProject)
        new LintCheck().apply(project, rootProject)
        new SpotBugsCheck().apply(project, rootProject)
    }

}
