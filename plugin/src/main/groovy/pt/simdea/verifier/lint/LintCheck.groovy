package pt.simdea.verifier.lint

import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class LintCheck extends CommonCheck<LintConfig> {

    LintCheck() {
        super('lint', 'lint', 'Android Lint')
    }

    @Override
    protected LintConfig getConfig(CheckExtension extension) {
        return null
    }

    @Override
    protected void run(Project project, Project rootProject, LintConfig config) {
        project.android.lintOptions {
            abortOnError config.abortOnError != null ? config.abortOnError : false
            absolutePaths config.absolutePaths != null ? config.absolutePaths : true
            checkAllWarnings = config.checkAllWarnings != null ? config.checkAllWarnings : false
            checkReleaseBuilds config.checkReleaseBuilds != null ? config.checkReleaseBuilds : false
            ignoreWarnings config.ignoreWarnings != null ? config.ignoreWarnings : false
            showAll config.showAll != null ? config.showAll : false
            warningsAsErrors config.warningsAsErrors != null ? config.warningsAsErrors : false
        }

        if (config.checkAllWarnings != null) {
            project.android.lintOptions {
                checkAllWarnings = config.checkAllWarnings
            }
        }

        if (config.absolutePaths != null) {
            project.android.lintOptions {
                absolutePaths = config.absolutePaths
            }
        }

        if (config.lintConfig != null) {
            project.android.lintOptions {
                lintConfig config.config
            }
        }

        if (config.checkReleaseBuilds != null) {
            project.android.lintOptions {
                checkReleaseBuilds config.checkReleaseBuilds
            }
        }

        if (config.reportHTML != null) {
            project.android.lintOptions {
                htmlReport true
                htmlOutput config.reportHTML
            }
        }

        if (config.reportXML != null) {
            project.android.lintOptions {
                xmlReport true
                xmlOutput config.reportXML
            }
        }
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        return 0
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isAndroidProject(project)
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return null
    }
}
