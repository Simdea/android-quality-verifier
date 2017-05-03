package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.findbugs.FindbugsCheck
import pt.simdea.verifier.pmd.PmdCheck

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        target.extensions.create(CheckExtension.NAME, CheckExtension)
        target.check.extensions.create('lint', CheckExtension.Lint)

        def extension = target.check

        new CheckstyleCheck().apply(target)
        new FindbugsCheck().apply(target)
        new PmdCheck().apply(target)

        addLint(target, target.check)

    }

    static boolean addLint(final Project subProject, final CheckExtension extension) {
        if (!shouldIgnore(subProject, extension) && !extension.skip && isAndroidProject(subProject)) {
            subProject.android.lintOptions {
                abortOnError extension.abortOnError != null ? extension.abortOnError : true
                absolutePaths extension.lint.absolutePaths != null ? extension.lint.absolutePaths : true
                checkAllWarnings = extension.lint.checkAllWarnings != null ? extension.lint.checkAllWarnings : false
                checkReleaseBuilds extension.lint.checkReleaseBuilds != null ? extension.lint.checkReleaseBuilds : false
                ignoreWarnings extension.lint.ignoreWarnings != null ? extension.lint.ignoreWarnings : false
                showAll extension.lint.showAll != null ? extension.lint.showAll : false
                warningsAsErrors extension.lint.warningsAsErrors != null ? extension.lint.warningsAsErrors : false
                lintConfig extension.lint.lintConfig != null ? extension.lint.lintConfig : resolveConfigFile("lint")
            }

            if (extension.lint.htmlReport != null) {
                subProject.android.lintOptions {
                    htmlReport extension.lint.htmlReport
                    htmlOutput extension.lint.htmlOutput
                }
            }

            if (extension.lint.xmlReport != null) {
                subProject.android.lintOptions {
                    xmlReport extension.lint.xmlReport
                    xmlOutput extension.lint.xmlOutput
                }
            }

            subProject.check.dependsOn 'lint'

            return true
        }

        return false
    }

    File resolveConfigFile(String code) {
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

    private static boolean shouldIgnore(final Project project, final CheckExtension extension) {
        return extension.ignoreProjects?.contains(project.name)
    }
}
