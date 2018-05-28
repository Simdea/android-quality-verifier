package pt.simdea.verifier.checkstyle


import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class CheckstyleCheck extends CommonCheck<CheckstyleConfig> {

    CheckstyleCheck() { super('checkstyle', 'androidCheckstyle', 'Runs Android Checkstyle') }

    @Override
    protected CheckstyleConfig getConfig(CheckExtension extension) { return extension.checkstyle }

    @Override
    void run(Project project, Project rootProject, CheckstyleConfig config) {
        project.plugins.apply(taskCode)

        project.checkstyle {
            configFile config.resolveConfigFile(taskCode)
        }

        project.task(taskName, type: Checkstyle) {
            description = taskDescription

            source = project.fileTree(config.getAndroidSources())
            include['**/*.java']
            exclude['**/gen/**']
            failOnViolation = false

            File file = new File(project.buildDir, "tmp/android-check/checkstyle-suppress.xml")
            file.parentFile.mkdirs()
            file.delete()
            file << Utils.getResource(project, "checkstyle/suppressions.xml")
            properties = file

            classpath = project.files()

            reports {
                html.enabled = htmlReportFile
                xml.enabled = xmlReportFile
            }
        }
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        GPathResult xml = new XmlSlurper().parseText(xmlReportFile.text)
        return xml.file.inject(0) { count, file -> count + file.error.size() }
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isJavaProject(project) || Utils.isAndroidProject(project)
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount Checkstyle rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
