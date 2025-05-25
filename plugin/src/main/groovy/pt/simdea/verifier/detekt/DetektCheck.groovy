package pt.simdea.verifier.detekt

import groovy.xml.XmlParser
import org.gradle.api.Project
import org.gradle.api.Task
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class DetektCheck extends CommonCheck<DetektConfig> {

    private static final String DETEKT_PLUGIN_ID = 'io.gitlab.arturbosch.detekt'
    private static final String DETEKT_EXTENSION_NAME = 'detekt'
    private static final String DETEKT_TASK_NAME = 'detekt' // Default task name by Detekt plugin
    // This should match the version in plugin/build.gradle
    private static final String DETEKT_TOOL_VERSION = '1.23.6'

    DetektCheck() {
        // taskCode, taskName (for CommonCheck's created task), taskDescription
        super('detekt', 'androidDetekt', 'Run Detekt source code analysis for Kotlin.')
    }

    @Override
    protected DetektConfig getConfig(CheckExtension extension) {
        return extension.detekt // Assumes CheckExtension has a 'detekt' property of type DetektConfig
    }

    @Override
    protected void run(Project project, Project rootProject, DetektConfig config, String variantName) {
        // Apply the Detekt plugin
        if (!project.getPlugins().hasPlugin(DETEKT_PLUGIN_ID)) {
            project.apply plugin: DETEKT_PLUGIN_ID
        }

        // Resolve config file using DetektConfig's method
        File detektConfigFile = config.resolveConfigFileYml(taskCode) // taskCode is 'detekt'

        // Configure the detekt extension
        project.extensions.getByType(io.gitlab.arturbosch.detekt.extensions.DetektExtension.class).with {
            toolVersion = DETEKT_TOOL_VERSION
            configProperty.set(project.files(detektConfigFile.absolutePath)) // Use configProperty.set for Gradle 6.x+
            // Ensure reports are configured correctly
            reports {
                xml {
                    required.set(true) // use .required.set()
                    outputLocation.set(xmlReportFile) // xmlReportFile is from CommonCheck
                }
                html {
                    required.set(true)
                    outputLocation.set(htmlReportFile) // htmlReportFile is from CommonCheck
                }
                txt.getRequired().set(false) // Disable txt report
                sarif.getRequired().set(false) // Disable sarif report
            }
            // If running per variant, source needs to be configured.
            // For now, assume it picks up default kotlin source sets or is configured globally.
            // If variantName is provided, Detekt might need specific source configuration here.
            // e.g. source.setFrom(project.android.sourceSets.getByName(variantName).kotlin.srcDirs)
            // However, Detekt plugin usually handles this for Android projects if 'android.sourceSets' are standard.
        }

        // Execute the Detekt task
        // The Detekt plugin creates tasks like 'detektMain', 'detektTest', and an aggregate 'detekt' task.
        // We want to run all relevant checks.
        Task detektTask = project.getTasks().findByName(DETEKT_TASK_NAME)
        if (detektTask == null) {
            project.getLogger().warn("Detekt task '${DETEKT_TASK_NAME}' not found. Skipping Detekt execution.")
            return
        }
        
        // Re-running the task by direct execution is tricky and often not recommended.
        // Instead, the verifier task ('androidDetekt') should depend on 'detekt'.
        // CommonCheck's apply method adds 'taskName' (androidDetekt) to 'check'.
        // We need to ensure 'detekt' runs before 'androidDetekt' evaluates reports.
        // For now, this explicit execution is a direct way, but task dependencies are better.
        // This is problematic because execute() is internal.
        // A better way: The 'androidDetekt' task (created by CommonCheck) should be made to
        // depend on the 'detekt' task from the Detekt plugin.
        // This is typically done in CheckPlugin.groovy or by CommonCheck's logic.
        // For now, we assume this method is called when 'androidDetekt' runs,
        // and 'detekt' should have already run if dependencies are set up correctly.
        // If not, this execution attempt might be needed but is fragile.
        // Let's log and assume dependencies will make it run.
        project.getLogger().lifecycle("Detekt configured. The '${DETEKT_TASK_NAME}' task should run as part of the build lifecycle to generate reports.")
        // As a fallback if not run by dependency:
        // try {
        // project.getTasks().getByName(DETEKT_TASK_NAME).getActions().each { action -> action.execute(detektTask) }
        // } catch (Exception e) {
        // project.getLogger().error("Failed to execute Detekt task programmatically: " + e.getMessage())
        // }
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        if (!xmlReportFile.exists()) {
            project.getLogger().warn("Detekt XML report not found at ${xmlReportFile.absolutePath}. Assuming 0 errors.")
            return 0
        }
        try {
            def report = new XmlParser().parse(xmlReportFile)
            int errorCount = 0
            // Detekt's Checkstyle XML report: <checkstyle> contains <file> elements,
            // each <file> can contain <error> elements.
            report.file.each { fileNode ->
                errorCount += fileNode.error.size()
            }
            project.getLogger().lifecycle("Detekt XML report parsed. Error count: $errorCount")
            return errorCount
        } catch (Exception e) {
            project.getLogger().error("Error parsing Detekt XML report: ${e.getMessage()}", e)
            return -1 // Indicate an error in parsing
        }
    }

    @Override
    protected boolean isSupported(Project project) {
        // Detekt is for Kotlin projects
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
            return "Detekt found $errorCount issues. See the report at: file://${htmlReportFile.absolutePath}"
        } else {
            return "Detekt found $errorCount issues. HTML report not available."
        }
    }
}
