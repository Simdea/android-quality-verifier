package pt.simdea.verifier.pmd

import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class PmdCheck extends CommonCheck<PmdConfig> {

    PmdCheck() { super('pmd', 'androidPmd', 'Runs Android PMD') }

    @Override
    protected PmdConfig getConfig(CheckExtension extension) { return extension.pmd }

    @Override
    void run(Project project, Project rootProject, PmdConfig config) {
        project.plugins.apply(taskCode)

        project.pmd {
            ruleSetFiles = config.resolveConfigFile(taskCode)
        }

        project.task(taskName, type: Pmd) {
            description = taskDescription

            ruleSets = []

            failOnError = false
            failOnRuleViolation = false

            source = project.fileTree(config.getAndroidSources())
            include config.include
            exclude config.exclude

            reports {
                html.enabled = htmlReportFile
                xml.enabled = xmlReportFile
            }
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
        return "$errorCount PMD rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
