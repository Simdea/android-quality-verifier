package org.stoyicker.androidcheck

import org.stoyicker.androidcheck.checkstyle.CheckstyleCheck
import org.stoyicker.androidcheck.findbugs.FindbugsCheck
import org.stoyicker.androidcheck.lint.LintCheck
import org.stoyicker.androidcheck.pmd.PmdCheck
import org.gradle.api.Plugin
import org.gradle.api.Project

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        target.extensions.create(CheckExtension.NAME, CheckExtension, target)
        target.check.extensions.create('lint', CheckExtension.Lint)

        new CheckstyleCheck().apply(target)
        new FindbugsCheck().apply(target)
        new PmdCheck().apply(target)
        target.subprojects { subProject ->
            afterEvaluate {
                def extension = target.check

                if (!shouldIgnore(subProject, extension)) {
                    addLint(subProject, extension)
                }
            }
        }
    }

    protected static boolean addLint(final Project subProject, final CheckExtension extension) {
        if (!shouldIgnore(subProject, extension) && !extension.skip && isAndroidProject(subProject)) {
            subProject.android.lintOptions {
                abortOnError extension.abortOnError != null ? extension.abortOnError : true
                absolutePaths extension.lint.absolutePaths != null ? extension.lint.absolutePaths : true
                checkReleaseBuilds extension.lint.checkReleaseBuilds != null ? extension.lint.checkReleaseBuilds : false
                warningsAsErrors extension.lint.warningsAsErrors != null ? extension.lint.warningsAsErrors : false
                checkAllWarnings = extension.lint.checkAllWarnings != null ? extension.lint.checkAllWarnings : false
            }

            if (extension.lint.textReport != null) {
                subProject.android.lintOptions {
                    textReport extension.lint.textReport
                    textOutput extension.lint.textOutput
                }
            }

            subProject.check.dependsOn 'lint'

            return true
        }

        return false
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
