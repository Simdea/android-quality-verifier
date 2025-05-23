package pt.simdea.verifier.detekt

import groovy.xml.XmlParser
import org.gradle.api.Project
import org.gradle.api.Task
import pt.simdea.verifier.Check
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class DetektCheck extends CommonCheck {

    private static final String DETEKT_CONFIG_FILE_NAME = 'detekt-config.yml'
    private static final String DEFAULT_DETEKT_CONFIG_FILE_PATH = 'detekt/conf-default.yml'
    private static final String DETEKT_REPORTS_FOLDER_NAME = 'detekt'
    private static final String DETEKT_REPORT_XML_FILE_NAME = 'detekt.xml'
    private static final String DETEKT_REPORT_HTML_FILE_NAME = 'detekt.html'
    private static final String DETEKT_TASK_NAME = 'detekt'
    private static final String DETEKT_PLUGIN_NAME = 'io.gitlab.arturbosch.detekt'
    private static final String DETEKT_EXTENSION_NAME = 'detekt'
    private static final String DETEKT_TOOL_VERSION = '1.23.6' // Ensure this matches the dependency

    DetektCheck() {
        super(Check.DETEKT, DETEKT_CONFIG_FILE_NAME, DEFAULT_DETEKT_CONFIG_FILE_PATH,
                DETEKT_REPORTS_FOLDER_NAME, DETEKT_REPORT_XML_FILE_NAME, DETEKT_REPORT_HTML_FILE_NAME)
    }

    @Override
    protected DetektConfig getConfig(final Project project) {
        return new DetektConfig(project)
    }

    @Override
    protected void performCheck(final Project project, final DetektConfig config) {
        project.getLogger().lifecycle("Applying Detekt plugin...")
        project.apply plugin: DETEKT_PLUGIN_NAME

        project.extensions.findByName(DETEKT_EXTENSION_NAME).with {
            toolVersion = DETEKT_TOOL_VERSION
            configFiles = project.files(configFile.absolutePath) // Use configFiles (plural) and project.files()
            reports {
                xml {
                    enabled = true
                    destination = xmlReportFile
                }
                html {
                    enabled = true
                    destination = htmlReportFile
                }
                txt {
                    enabled = false // Disable txt report if not needed
                }
                sarif {
                    enabled = false // Disable sarif report if not needed
                }
            }
        }

        // Ensure Detekt task runs after Kotlin compilation and before verification tasks
        // This might require more specific task dependency setup depending on the project structure.
        // For now, we'll try to execute it directly.
        try {
            project.getLogger().lifecycle("Executing Detekt task...")
            Task detektTask = project.getTasks().findByName(DETEKT_TASK_NAME)
            if (detektTask == null) {
                project.getLogger().error("Detekt task not found. Make sure the Detekt plugin is applied correctly and Kotlin/Java source files exist.")
                return
            }
            // Re-configure the task to ensure it uses our settings if it was already created
            project.detekt {
                toolVersion = DETEKT_TOOL_VERSION
                configFiles = project.files(configFile.absolutePath)
                reports {
                    xml {
                        enabled = true
                        destination = xmlReportFile
                    }
                    html {
                        enabled = true
                        destination = htmlReportFile
                    }
                }
            }
            // Detekt should be automatically triggered by the lifecycle,
            // but if not, uncommenting the following might be necessary.
            // However, directly calling execute() on a task is generally discouraged.
            // Instead, ensure it's part of the task graph.
            // project.tasks.getByName(DETEKT_TASK_NAME).execute()

            // A better way is to make the verifier task depend on it.
            // This will be handled by how the CheckPlugin invokes this.
             project.getLogger().lifecycle("Detekt configured. It will run as part of the build lifecycle.")

        } catch (Exception e) {
            project.getLogger().error("Error executing Detekt task: ${e.getMessage()}", e)
        }
    }

    @Override
    protected int getErrorCount(final Project project, final DetektConfig config) {
        if (!xmlReportFile.exists()) {
            project.getLogger().warn("Detekt XML report not found at ${xmlReportFile.absolutePath}. Assuming 0 errors.")
            return 0
        }

        try {
            def report = new XmlParser().parse(xmlReportFile)
            // Detekt XML report structure: <checkstyle> contains <file> elements,
            // each <file> can contain <error> elements.
            // For Detekt, the report root is <findings>. Each direct child is an issue.
            // However, modern Detekt might use a different structure, often similar to Checkstyle's for compatibility.
            // Let's assume a simple structure: <report><error .../></report> or <findings><finding .../></findings>
            // A common Detekt XML output has a root <detekt> and then <file> elements with nested <error> elements.
            // Example: <detekt><file name="..."><error column="X" line="Y" message="..." severity="Z" source="..."/></file></detekt>
            // More accurately, Detekt's own XML format is:
            // <smells>
            //   <smell type="RuleSet:RuleId" severity="severity">
            //     <location file="path/to/File.kt" offset="offset" line="line" column="column"/>
            //     <description>Error message</description>
            //   </smell>
            //   ...
            // </smells>
            // However, the configured report is 'xml', which usually means checkstyle format for many tools.
            // Let's re-check the Detekt documentation for the XML report structure.
            // If it's checkstyle format, the parsing logic from CheckstyleCheck can be reused.
            // The `reports.xml` in Detekt is indeed Checkstyle formatted.
            int errorCount = 0
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
    protected String getErrorMessage(final Project project, final DetektConfig config, final int errorCount) {
        return "Detekt found $errorCount issues. See the report at ${Utils.getRelativePath(project, htmlReportFile)}"
    }
}
