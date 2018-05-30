package pt.simdea.verifier.errorprone


import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class ErrorProneCheck extends CommonCheck<ErrorProneConfig> {

    ErrorProneCheck() { super('errorProne', 'androidErrorProne', 'Runs Android CPD') }

    @Override
    protected ErrorProneConfig getConfig(CheckExtension extension) { return extension.errorProne }

    @Override
    protected void run(Project project, Project rootProject, ErrorProneConfig config) {
        project.plugins.apply('net.ltgt.errorprone')
        project.configurations.errorprone {
            resolutionStrategy.force "com.google.errorprone:error_prone_core:2.3.1"
        }
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        GPathResult xml = new XmlSlurper().parseText(xmlReportFile.text)
        return xml.file.inject(0) { count, file -> count + file.violation.size() }
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isJavaProject(project) || Utils.isAndroidProject(project)
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount CPD rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
