package pt.simdea.verifier.ktlint

import groovy.xml.XmlParser
import org.gradle.api.Project
import org.gradle.api.Task
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils
import org.jlleitschuh.gradle.ktlint.tasks.KtlintCheckTask // Required for task configuration

class KtLintCheck extends CommonCheck<KtLintConfig> {

    private static final String KTLINT_PLUGIN_ID = 'org.jlleitschuh.gradle.ktlint'
    private static final String KTLINT_EXTENSION_NAME = 'ktlint'
    private static final String KTLINT_CHECK_TASK_NAME = 'ktlintCheck' // Aggregate task by Ktlint plugin
    // This should match the version in plugin/build.gradle for the Ktlint *tool* itself
    // The Ktlint Gradle Plugin version is separate (e.g., 12.1.0)
    private static final String KTLINT_TOOL_VERSION = '0.50.0'

    KtLintCheck() {
        // taskCode, taskName (for CommonCheck's created task), taskDescription
        super('ktlint', 'androidKtlint', 'Run Ktlint source code style analysis for Kotlin.')
        // Ktlint primarily uses .editorconfig, so a specific config file for rules isn't typical like Checkstyle/PMD.
        // CommonCheck handles configFile resolution, but for Ktlint, it's less critical if .editorconfig is at root.
        // We still provide a conventional name for CommonCheck's structure.
        this.defaultConfigFile = '.editorconfig' // Or a path like 'config/ktlint/.editorconfig'
    }

    @Override
    protected KtLintConfig getConfig(CheckExtension extension) {
        return extension.ktLint // Assumes CheckExtension has a 'ktLint' property of type KtLintConfig
    }

    @Override
    protected void run(Project project, Project rootProject, KtLintConfig config) {
        // Apply the Ktlint plugin
        if (!project.getPlugins().hasPlugin(KTLINT_PLUGIN_ID)) {
            project.apply plugin: KTLINT_PLUGIN_ID
        }

        // Configure the ktlint extension
        project.extensions.getByType(org.jlleitschuh.gradle.ktlint.KtlintExtension.class).with {
            version.set(KTLINT_TOOL_VERSION)
            android.set(Utils.isAndroidProject(project)) // Enable Android mode for Android projects
            ignoreFailures.set(true) // Verifier plugin will handle build failure based on error count
            
            // Configure reporters globally for the extension if possible,
            // otherwise, this might need to be on individual tasks.
            // For recent versions of the plugin, reporters are configured this way.
            reporters {
                reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
                reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
                // reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN) // Optional
            }
        }

        // Configure all tasks of type KtlintCheckTask to set their specific report output locations.
        // This is crucial for CommonCheck to find the reports.
        project.getTasks().withType(KtlintCheckTask.class).configureEach { task ->
            // The plugin generates reports per source set, e.g., ktlintMainSourceSetCheck, ktlintTestSourceSetCheck.
            // CommonCheck expects a single xmlReportFile and htmlReportFile.
            // This means we might need to pick one (e.g., from 'main' source set) or aggregate.
            // For simplicity, let's assume the 'ktlintMainSourceSetCheck' is the primary one,
            // or that the aggregate 'ktlintCheck' task's report can be configured this way.
            // The plugin names reports like: build/reports/ktlint/${task.name}/ktlint-${reporterType}.${reporterFileExtension}
            // We need to make it write to xmlReportFile and htmlReportFile directly.

            // This sets the output for each KtlintCheckTask. If there are multiple (e.g. main, test),
            // they might overwrite each other if xmlReportFile/htmlReportFile are fixed paths.
            // For now, we'll assume a single, overarching report or that the last one wins,
            // which might be acceptable if we only care about 'main' sources primarily.
            // A more robust solution would involve custom aggregation if multiple source sets are checked
            // and their reports need to be distinct yet funneled through CommonCheck's single file mechanism.
            
            // This might not work as expected if the plugin doesn't allow overriding output paths this way for all reporters.
            // The plugin typically creates reports in its own directory structure.
            // Let's try to ensure the main aggregate task `ktlintCheck` has its reports configured.
            // However, KtlintCheckTask is per source set.
            // The plugin will generate reports into build/reports/ktlint/ktlintMainSourceSetCheck/ktlint-checkstyle.xml etc.
            // CommonCheck's xmlReportFile and htmlReportFile are singular.
            // We will rely on the default paths the Ktlint plugin uses and ensure CommonCheck's
            // xmlReportFile and htmlReportFile point to the 'main' source set's report or a known aggregate.
            // For `org.jlleitschuh.gradle.ktlint` plugin, the aggregate task `ktlintCheck`
            // doesn't produce its own reports but runs other KtlintCheckTask tasks.
            // We'll target the main Kotlin source set for report paths.
            if (task.getName().toLowerCase().contains("mainsourcesetcheck")) {
                 task.getReports().getByName("checkstyle").getOutputLocation().set(xmlReportFile)
                 task.getReports().getByName("html").getOutputLocation().set(htmlReportFile)
            } else {
                // For other tasks (e.g. test), disable reports or put them elsewhere if not needed by CommonCheck
                 task.getReports().getByName("checkstyle").getOutputLocation().set(project.layout.buildDirectory.file("reports/ktlint/${task.getName()}/ktlint.xml"))
                 task.getReports().getByName("html").getOutputLocation().set(project.layout.buildDirectory.file("reports/ktlint/${task.getName()}/ktlint.html"))
            }
        }
        
        project.getLogger().lifecycle("Ktlint configured. The '${KTLINT_CHECK_TASK_NAME}' task should run as part of the build lifecycle.")
        // The CommonCheck framework should ensure that KTLINT_CHECK_TASK_NAME (or rather, the task `androidKtlint`
        // which calls this run method) depends on the necessary Ktlint tasks from the plugin.
        // For ktlint-gradle, the main checking task is 'ktlintCheck'.
        // So, 'androidKtlint' should be made to depend on 'ktlintCheck'.
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        if (!xmlReportFile.exists()) {
            project.getLogger().warn("Ktlint XML (checkstyle) report not found at ${xmlReportFile.absolutePath}. Assuming 0 errors.")
            return 0
        }
        try {
            def report = new XmlParser().parse(xmlReportFile)
            int errorCount = 0
            // Checkstyle format: <checkstyle> <file name="path"> <error ... /> </file> </checkstyle>
            report.file.each { fileNode ->
                errorCount += fileNode.error.size()
            }
            project.getLogger().lifecycle("Ktlint XML report parsed. Error count: $errorCount")
            return errorCount
        } catch (Exception e) {
            project.getLogger().error("Error parsing Ktlint XML (checkstyle) report: ${e.getMessage()}", e)
            return -1 // Indicate an error in parsing
        }
    }

    @Override
    protected boolean isSupported(Project project) {
        // Ktlint is for Kotlin projects
        return Utils.isKotlinProject(project) || Utils.isKotlinMultiplatformProject(project)
    }

    @Override
    protected boolean isTask() {
        // This check should run as a task
        return true
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        if (htmlReportFile != null && htmlReportFile.exists()) {
            return "Ktlint found $errorCount issues. See the report at: file://${htmlReportFile.absolutePath}"
        } else {
            // If HTML report path isn't standard or reliably set, provide path to XML.
            File xmlFile = getXmlReportFile() // Method from CommonCheck
            String reportPath = xmlFile != null && xmlFile.exists() ? "file://${xmlFile.absolutePath}" : "XML report not found.";
            return "Ktlint found $errorCount issues. HTML report not available. Check XML report: ${reportPath}"
        }
    }
}
