package org.stoyicker.androidcheck.lint

import com.android.build.gradle.internal.dsl.LintOptions
import com.android.build.gradle.tasks.Lint
import org.gradle.api.Project
import org.stoyicker.androidcheck.CheckExtension
import org.stoyicker.androidcheck.CommonCheck
import org.stoyicker.androidcheck.CommonConfig

class LintCheck extends CommonCheck {

    LintCheck(String taskCode, String taskName, String taskDescription) {
        super('lint', 'androidLint', 'Runs Android Lint')
    }

    @Override
    protected CommonConfig getConfig(CheckExtension extension) {
        return extension.lint
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        return 0
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return null
    }

    @Override
    protected void performCheck(Project project, List sources, File configFile, File xmlReportFile) {
        Lint lintTask = new Lint()

        LintOptions lintOptions = lintTask.lintOptions
    }

}
