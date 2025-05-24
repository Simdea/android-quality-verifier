package pt.simdea.verifier.ktlint

import groovy.xml.XmlParser
import org.gradle.api.Project
import org.gradle.api.Task
import pt.simdea.verifier.Check
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class KtlintCheck extends CommonCheck {

    // Ktlint doesn't use a specific config file like checkstyle.xml or detekt.yml by default.
    // It relies on .editorconfig files for rule configuration.
    // We'll define a conventional name if the user wants to provide one at the project root.
    private static final String KTLINT_EDITORCONFIG_FILE_NAME = '.editorconfig'
    // We won't provide a default .editorconfig in resources, as Ktlint has built-in defaults.
    // If a project has an .editorconfig, the plugin should pick it up automatically.
    // For the CommonCheck constructor, we can pass null or a conventional path.
    // Let's use a conventional path for consistency, even if we don't ship a default file.
    private static final String DEFAULT_KTLINT_CONFIG_FILE_PATH = 'ktlint/' + KTLINT_EDITORCONFIG_FILE_NAME

    private static final String KTLINT_REPORTS_FOLDER_NAME = 'ktlint'
    private static final String KTLINT_REPORT_XML_FILE_NAME = 'ktlint-checkstyle-report.xml' // Output from checkstyle reporter
    private static final String KTLINT_REPORT_HTML_FILE_NAME = 'ktlint-html-report.html'   // Output from html reporter

    private static final String KTLINT_CHECK_TASK_NAME = 'ktlintCheck' // Standard task from the plugin
    private static final String KTLINT_FORMAT_TASK_NAME = 'ktlintFormat'
    private static final String KTLINT_PLUGIN_ID = 'org.jlleitschuh.gradle.ktlint'
    private static final String KTLINT_EXTENSION_NAME = 'ktlint'
    private static final String KTLINT_TOOL_VERSION = '0.50.0' // Version of the Ktlint tool itself

    KtlintCheck() {
        super(Check.KTLINT, KTLINT_EDITORCONFIG_FILE_NAME, DEFAULT_KTLINT_CONFIG_FILE_PATH,
                KTLINT_REPORTS_FOLDER_NAME, KTLINT_REPORT_XML_FILE_NAME, KTLINT_REPORT_HTML_FILE_NAME)
    }

    @Override
    protected KtlintConfig getConfig(final Project project) {
        return new KtlintConfig(project)
    }

    @Override
    protected void performCheck(final Project project, final KtlintConfig config) {
        project.getLogger().lifecycle("Applying Ktlint plugin...")
        project.apply plugin: KTLINT_PLUGIN_ID

        // The user's .editorconfig at the project root will be picked up automatically by Ktlint.
        // If `configFile` (from CommonCheck, resolving to KTLINT_EDITORCONFIG_FILE_NAME) exists
        // at the expected location (e.g., project.rootDir/ktlint/.editorconfig or project.rootDir/.editorconfig),
        // the Ktlint plugin should use it if configured correctly.
        // The `org.jlleitschuh.gradle.ktlint` plugin uses a top-level .editorconfig by default.

        project.extensions.findByName(KTLINT_EXTENSION_NAME).with {
            version = KTLINT_TOOL_VERSION
            android = true // Enable Android mode if it's an Android project (sensible default)
            ignoreFailures = config.ignoreFailures // Let verifier handle failure based on error count
            reporters {
                reporter "checkstyle" // For XML report
                reporter "html"     // For HTML report
            }
            // Output files are typically configured via the reporters.
            // The plugin defaults reports to build/reports/ktlint/
            // We need to ensure our xmlReportFile and htmlReportFile paths are used.
            // The plugin names tasks e.g. ktlintMainSourceSetCheck, ktlintTestSourceSetCheck
            // The aggregate task `ktlintCheck` runs all of them.
        }

        // Configure output paths for reports on the specific tasks if the extension doesn't directly allow it.
        // The KtlintGradle plugin names its report files like `ktlint<SourceSet>CheckstyleReport.xml`.
        // We need a single report file for `getErrorCount`.
        // The `ktlintCheck` task is an aggregate. We need to ensure reports are enabled.
        // The plugin should place reports in `build/reports/ktlint/`.
        // Let's try to configure the output file for the checkstyle reporter globally if possible,
        // or ensure the default location is what we expect.

        // The plugin version 11+ uses `project.tasks.withType(org.jlleitschuh.gradle.ktlint.tasks.KtlintCheckTask)`
        // to configure all check tasks.
        project.tasks.withType(org.jlleitschuh.gradle.ktlint.tasks.KtlintCheckTask.class) { task ->
            task.reports.set([
                "checkstyle": project.file(xmlReportFile.absolutePath), // Use our defined path
                "html": project.file(htmlReportFile.absolutePath)    // Use our defined path
            ])
        }
        
        // The main check task we are interested in is KTLINT_CHECK_TASK_NAME.
        // It should be executed. The CheckPlugin will add it as a dependency to the main 'check' task.
        project.getLogger().lifecycle("Ktlint configured. The '$KTLINT_CHECK_TASK_NAME' task will run as part of the build lifecycle.")

        // If a specific .editorconfig is provided via verifier's config, attempt to set it.
        // However, KtlintGradle plugin primarily looks for .editorconfig at rootDir.
        // Forcing a different one can be tricky and might require setting system properties
        // or using features of the Ktlint tool itself if the plugin doesn't directly support it.
        // For now, we assume the standard .editorconfig discovery.
        if (configFile.exists() && configFile.name == KTLINT_EDITORCONFIG_FILE_NAME) {
             project.getLogger().lifecycle("An .editorconfig file exists at ${configFile.absolutePath}. Ktlint should pick it up if it's in a standard location.")
        } else {
             project.getLogger().lifecycle("No custom .editorconfig specified through verifier config, or file not found at expected CommonCheck path. Ktlint will use its defaults or project's root .editorconfig.")
        }
    }

    @Override
    protected int getErrorCount(final Project project, final KtlintConfig config) {
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
    protected String getErrorMessage(final Project project, final KtlintConfig config, final int errorCount) {
        return "Ktlint found $errorCount issues. See the HTML report at ${Utils.getRelativePath(project, htmlReportFile)} or the XML report at ${Utils.getRelativePath(project, xmlReportFile)}"
    }
}
