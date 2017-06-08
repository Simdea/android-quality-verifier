package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.cpd.CpdCheck
import pt.simdea.verifier.findbugs.FindbugsCheck
import pt.simdea.verifier.pmd.PmdCheck

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        target.extensions.create(CheckExtension.NAME, CheckExtension, target)
        target.check.extensions.create('lint', CheckExtension.Lint)

        target.repositories.add(target.getRepositories().jcenter())
        target.dependencies.add("compile", "io.realm:realm-annotations:0.0.1")
        target.dependencies.add("annotationProcessor", "pt.simdea.verifier.annotations:verifier-annotations:0.0.1")

        new FindbugsCheck().apply(target)
        new PmdCheck().apply(target)
        new CpdCheck().apply(target)
        addLint(target, target.check)
        target.subprojects { subProject ->
            afterEvaluate {
                def extension = target.check

                addLint(subProject, extension)
            }
        }
        new CheckstyleCheck().apply(target)
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
