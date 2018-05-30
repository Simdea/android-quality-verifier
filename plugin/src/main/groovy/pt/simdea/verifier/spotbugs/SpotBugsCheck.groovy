package pt.simdea.verifier.spotbugs

import edu.umd.cs.findbugs.FindBugs2
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class SpotBugsCheck extends CommonCheck<SpotBugsConfig> {

    SpotBugsCheck() { super('spotbugs', 'androidSpotbugs', 'Runs Android SpotBugs') }

    @Override
    protected Set<String> getDependencies() { ['assemble'] }

    @Override
    protected SpotBugsConfig getConfig(CheckExtension extension) { return extension.spotbugs }

    @Override
    void run(Project project, Project rootProject, SpotBugsConfig config) {

        project.plugins.apply(taskCode)

        project.findbugs {
            excludeFilter = config.resolveConfigFile(taskCode)
            failOnError = false
            workHard = true
            output = "xml:withMessages"
        }

        project.task(taskCode, type: FindBugs2, dependsOn: 'assemble') {
            description = taskDescription

            classes = project.fileTree(project.buildDir).include(config.getAndroidClasses())
            source = project.fileTree(config.getAndroidSources())
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
        return xml.FindBugsSummary.getProperty('@total_bugs').text() as int
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isJavaProject(project) || Utils.isAndroidProject(project) || Utils.isKotlinProject(project)
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount FindBugs rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
