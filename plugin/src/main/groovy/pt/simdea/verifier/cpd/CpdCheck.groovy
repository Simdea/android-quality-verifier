package pt.simdea.verifier.cpd

import groovy.util.slurpersupport.GPathResult
import net.sourceforge.pmd.cpd.CPDTask
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.CommonConfig

class CpdCheck extends CommonCheck {

    CpdCheck() { super('cpd', 'androidCpd', 'Runs Android CPD') }

    @Override
    protected CommonConfig getConfig(CheckExtension extension) { return extension.cpd }

    @Override
    protected void performCheck(Project project, List<File> sources, File configFile, File xmlReportFile) {
        CPDTask cpdTask = new CPDTask()

        cpdTask.project = project.ant.antProject

        sources.findAll { it.exists() }.each {
            cpdTask.addFileset(project.ant.fileset(dir: it))
        }

        cpdTask.perform()
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
