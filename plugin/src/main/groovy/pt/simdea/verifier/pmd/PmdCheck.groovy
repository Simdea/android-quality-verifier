package pt.simdea.verifier.pmd

import groovy.util.slurpersupport.GPathResult
import net.sourceforge.pmd.ant.Formatter
import net.sourceforge.pmd.ant.PMDTask
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class PmdCheck extends CommonCheck<PmdConfig> {

    PmdCheck() { super('pmd', 'androidPmd', 'Runs Android PMD') }

    @Override
    protected PmdConfig getConfig(CheckExtension extension) { return extension.pmd }

    @Override
    void run(Project project, Project rootProject, PmdConfig config) {
        PMDTask pmdTask = new PMDTask()

        pmdTask.project = project.ant.antProject
        pmdTask.ruleSetFiles = config.resolveConfigFile(taskCode).toString()
        pmdTask.addFormatter(new Formatter(type: 'xml', toFile: xmlReportFile))

        pmdTask.failOnError = false
        pmdTask.failOnRuleViolation = false

        config.getAndroidSources().findAll { it.exists() }.each {
            pmdTask.addFileset(project.ant.fileset(dir: it))
        }

        pmdTask.perform()

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
        return "$errorCount PMD rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
