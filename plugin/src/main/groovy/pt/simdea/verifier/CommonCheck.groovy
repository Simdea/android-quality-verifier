package pt.simdea.verifier

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.awt.*

abstract class CommonCheck<Config extends CommonConfig> {

    final String taskCode
    final String taskName
    final String taskDescription
    final File htmlReportFile
    final File xmlReportFile

    CommonCheck(String taskCode, String taskName, String taskDescription) {
        this.taskCode = taskCode
        this.taskName = taskName
        this.taskDescription = taskDescription

        xmlReportFile = getConfig().resolveXmlReportFile(taskCode)
        xmlReportFile.parentFile.mkdirs()
        htmlReportFile = getConfig().resolveHtmlReportFile(taskCode)
        htmlReportFile.parentFile.mkdirs()
    }

    protected Set<String> getDependencies() { [] }

    protected abstract Config getConfig(CheckExtension extension)

    protected abstract void run(Project project, Project rootProject, Config config)

    protected abstract int getErrorCount(File xmlReportFile)

    protected abstract boolean isSupported(Project project)

    protected abstract String getErrorMessage(int errorCount, File htmlReportFile)

    protected void reformatReport(Project project, File styleFile, File xmlReportFile, File htmlReportFile) {
        project.ant.xslt(in: xmlReportFile, out: htmlReportFile) {
            style { string(styleFile.text) }
        }
    }

    void apply(Project target, Project rootProject) {
        CheckExtension extension = target.extensions.findByType(CheckExtension)
        Config config = getConfig(extension)

        def isNotIgnored = !config.skip

        if (isNotIgnored && isSupported()) {

            run(target, rootProject, config)

            reformatAndReportErrors(target, config)

            if (target.tasks.find({ it.name == 'check' }) != null) {
                target.tasks.getByName('check').dependsOn taskName
            } else {
                target.logger.warn "task check not found in" +
                        " project $target.name. You may need to run the plugin tasks manually"
            }
            dependencies.each { target.tasks.getByName(taskName).dependsOn it }
        }
    }

    protected void reformatAndReportErrors(Project project, Config config) {
        if (config.shouldResolveErrors()) {
            reformatReport(project, config.resolveStyleFile(taskCode), xmlReportFile, htmlReportFile)
            CheckExtension extension = project.extensions.findByType(CheckExtension)

            int errorCount = getErrorCount(xmlReportFile)
            if (errorCount) {
                String errorMessage = getErrorMessage(errorCount, htmlReportFile)
                if (config.resolveAbortOnError(extension.abortOnError)) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI("file://" + htmlReportFile.absolutePath))
                    } else {
                        project.logger.warn "Your system does not support java.awt.Desktop. " +
                                "Not opening report automatically."
                    }
                    throw new GradleException(errorMessage)
                } else {
                    project.logger.warn errorMessage
                }
            }
        }
    }
}
