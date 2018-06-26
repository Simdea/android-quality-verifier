package pt.simdea.verifier.cpd

import groovy.util.slurpersupport.GPathResult
import net.sourceforge.pmd.cpd.CPDTask
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class CpdCheck extends CommonCheck<CpdConfig> {

    CpdCheck() { super('cpd', 'androidCpd', 'Runs Android CPD') }

    @Override
    protected CpdConfig getConfig(CheckExtension extension) { return extension.cpd }

    @Override
    void run(Project project, Project rootProject, CpdConfig config) {
        CPDTask cpdTask = new CPDTask()

        cpdTask.project = project.ant.antProject
        cpdTask.outputFile = xmlReportFile
        cpdTask.language = config.language
        cpdTask.minimumTokenCount = config.minimumTokenCount
        cpdTask.encoding = 'UTF-8'
        cpdTask.format = CPDTask.FormatAttribute.getInstance(CPDTask.FormatAttribute, "xml")

        config.getAndroidSources().findAll { it.exists() }.each {
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
    protected boolean isSupported(Project project) {
        return Utils.isJavaProject(project) || Utils.isAndroidProject(project)
    }

    @Override
    protected boolean isTask() {
        return true
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount CPD rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
