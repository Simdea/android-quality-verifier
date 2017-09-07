package pt.simdea.verifier.errorprone

import com.google.errorprone.ErrorProneAntCompilerAdapter
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.CommonConfig

class ErrorProneCheck extends CommonCheck {

    ErrorProneCheck() { super('cpd', 'androidCpd', 'Runs Android CPD') }

    @Override
    protected CommonConfig getConfig(CheckExtension extension) { return extension.cpd }

    @Override
    protected void performCheck(Project project, List<File> sources, File configFile, File xmlReportFile) {
        ErrorProneAntCompilerAdapter adapter = new ErrorProneAntCompilerAdapter()

        adapter.execute()
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        GPathResult xml = new XmlSlurper().parseText(xmlReportFile.text)
        return xml.file.inject(0) { count, file -> count + file.violation.size() }
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount CPD rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
