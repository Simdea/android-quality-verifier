package pt.simdea.verifier.lint

import groovy.xml.XmlParser
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

// import pt.simdea.verifier.lint.LintConfig // Already in same package

class LintCheck extends CommonCheck<LintConfig> {

    LintCheck() {
        super('lint', 'androidLint', 'Configures and checks Android Lint results')
        // Lint uses lint.xml, CommonCheck's defaultConfigFile resolution should find it.
        this.defaultConfigFile = "lint.xml"
    }

    @Override
    protected LintConfig getConfig(CheckExtension extension) {
        if (extension.lint == null) {
            if (this.project != null) { // this.project is from CommonCheck
                extension.lint = new LintConfig(this.project)
            } else {
                throw new IllegalStateException("Project reference not available in LintCheck for creating LintConfig.")
            }
        }
        return extension.lint
    }

    @Override
    protected Set<String> getDependencies() {
        // This task should run after the standard Android 'lint' task has generated its reports.
        // The specific variant lint task (e.g., 'lintDebug') is what generates the report.
        // 'lint' is the lifecycle task that runs all variant lint tasks.
        return ['lint'] as Set 
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isAndroidProject(project)
    }
    
    @Override
    protected boolean isTask() {
        // This class configures AGP's lint, but the error checking is part of the task CommonCheck creates.
        return true 
    }

    @Override
    protected void runCheckLogic(Project project, LintConfig config) {
        // This method configures project.android.lintOptions.
        // The actual Lint execution is done by AGP's 'lint' task (e.g., 'lintDebug', 'lintRelease').
        // CommonCheck's 'apply' method makes this 'androidLint' task depend on 'lint'.
        // So, when 'androidLint' runs, 'lint' should have already completed and generated reports.
        
        if (!Utils.isAndroidProject(project)) {
            project.logger.info("Project ${project.name} is not an Android project. Skipping Lint configuration by Verifier.")
            return
        }

        project.android.lintOptions {
            // Configure all options from LintConfig:
            // CommonCheck's abortOnError is for the verifier task, not AGP's lint task directly.
            // We let AGP's lint run, then CommonCheck decides to fail the build.
            abortOnError = config.abortOnError ?: false // Let AGP lint run, verifier will check errors
            
            // Use the values from LintConfig, providing defaults if they are null in the config.
            // The defaults here should match Android Gradle Plugin's defaults or sensible ones.
            absolutePaths = config.absolutePaths // LintConfig defaults to true
            checkAllWarnings = config.checkAllWarnings // LintConfig defaults to false
            
            if (config.disable != null && !config.disable.isEmpty()) {
                disable.clear() // Clear any existing direct AGP config
                disable.addAll(config.disable)
            }
            
            checkReleaseBuilds = config.checkReleaseBuilds // LintConfig defaults to true
            
            // this.configFile is resolved by CommonCheck.apply() via LintConfig.resolveConfigFile()
            // which prioritizes LintConfig.lintConfig property.
            if (this.configFile != null && this.configFile.exists()) {
                lintConfig = this.configFile
            } else if (config.lintConfig != null) { // If lintConfig property was set but file didn't exist
                project.logger.warn("Custom lintConfig file not found: ${config.lintConfig.absolutePath}. Lint will use its defaults or embedded config.")
            }
            
            ignoreWarnings = config.ignoreWarnings // LintConfig defaults to false
            showAll = config.showAll // LintConfig defaults to false
            warningsAsErrors = config.warningsAsErrors // LintConfig defaults to false

            // Set report paths. These are instance properties from CommonCheck, resolved in its apply()
            xmlOutput = project.file(this.xmlReportFile.absolutePath)
            htmlOutput = project.file(this.htmlReportFile.absolutePath)
            xmlReport = true // Ensure XML report is enabled
            htmlReport = true // Ensure HTML report is enabled
            
            // textReport = false // Optionally disable text report
            // textOutput = project.file("lint-results.txt") // Or configure its output
        }
        project.logger.lifecycle("Android Lint options configured by Verifier plugin. AGP's 'lint' task will generate reports to specified locations.")
    }

    @Override
    protected int getErrorCount(File report) { // report is this.xmlReportFile
        if (report == null || !report.exists() || report.length() == 0) {
            project.logger.warn("Android Lint XML report not found or empty: ${report?.absolutePath}. Assuming 0 errors.")
            return 0
        }
        try {
            def issuesNode = new XmlParser().parse(report)
            // Count <issue> elements
            int issueCount = issuesNode.issue.size()
            project.logger.lifecycle("Android Lint XML report parsed. Issue count: ${issueCount}")
            return issueCount
        } catch (Exception e) {
            project.logger.error("Error parsing Android Lint XML report: ${e.getMessage()}", e)
            return -1 // Indicate error
        }
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReport) { // htmlReport is this.htmlReportFile
        String reportPath = (htmlReport != null && htmlReport.exists()) ?
                            "file://${htmlReport.absolutePath}" :
                            "HTML report not available (expected at ${htmlReport?.absolutePath}). Check XML report: file://${xmlReportFile?.absolutePath}";
        return "Android Lint found $errorCount issues. See the report at: $reportPath"
    }
}
