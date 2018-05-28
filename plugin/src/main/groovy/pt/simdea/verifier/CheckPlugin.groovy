package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.cpd.CpdCheck
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
                    addTool(subProject, rootProject, rootProject.check)
                }
            }
        } else {
            rootProject.afterEvaluate {
                addTool(rootProject, rootProject, rootProject.check)
            }
        }
    }

    private static void addTool(final Project project, final Project rootProject) {
        new PmdCheck().apply(project, rootProject)
        new CheckstyleCheck().apply(rootProject, rootProject)
        new CpdCheck().apply(rootProject, rootProject)
        // Those static code tools take the longest hence we'll add them at the end.
        addLint(project, extension)
        new SpotBugsCheck().apply(rootProject, rootProject)
    }

}
