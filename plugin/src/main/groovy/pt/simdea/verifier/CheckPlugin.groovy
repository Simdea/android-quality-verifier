package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.cpd.CpdCheck
import pt.simdea.verifier.pmd.PmdCheck
import pt.simdea.verifier.spotbugs.SpotbugsCheck

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project rootProject) {
        rootProject.extensions.create(CheckExtension.NAME, CheckExtension, rootProject)
        rootProject.check.extensions.create('lint', CheckExtension.Lint)
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

    private static void addTool(final Project project, final Project rootProject, final CheckExtension extension) {
        new CheckstyleCheck().apply(rootProject)
        new PmdCheck().apply(rootProject)
        new CpdCheck().apply(rootProject)
        // Those static code tools take the longest hence we'll add them at the end.
        //addLint(project, extension)
        new SpotbugsCheck().apply(rootProject)
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

            if (extension.lint.reportXML != null) {
                subProject.android.lintOptions {
                    xmlReport true
                    xmlOutput extension.lint.reportXML
                }
            }

            subProject.check.dependsOn 'lint'

            return true
        }

        return false
    }

    static File resolveConfigFile(String code, Project project) {
        File file = new File(project.buildDir, "tmp/android-check/${code}.xml")
        file.parentFile.mkdirs()
        file.delete()
        file << Utils.getResource(project, "$code/conf-default.xml")
        return file
    }

    protected static boolean isAndroidProject(final Project project) {
        final boolean isAndroidLibrary = project.plugins.hasPlugin('com.android.library')
        final boolean isAndroidApp = project.plugins.hasPlugin('com.android.application')
        return isAndroidLibrary || isAndroidApp
    }
}
