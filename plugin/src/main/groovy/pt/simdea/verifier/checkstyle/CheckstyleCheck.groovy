package pt.simdea.verifier.checkstyle

import com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask
import com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask.Formatter
import com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask.FormatterType
import groovy.util.slurpersupport.GPathResult
import org.apache.tools.ant.types.Path
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class CheckstyleCheck extends CommonCheck<CheckstyleConfig> {

    CheckstyleCheck() { super('checkstyle', 'androidCheckstyle', 'Runs Android Checkstyle') }

    @Override
    protected CheckstyleConfig getConfig(CheckExtension extension) { return extension.checkstyle }

    @Override
    void run(Project project, Project rootProject, CheckstyleConfig config) {
        CheckstyleAntTask checkStyleTask = new CheckstyleAntTask()

        checkStyleTask.project = project.ant.antProject
        checkStyleTask.config = config.resolveConfigFile(taskCode).toURI().toURL()
        checkStyleTask.addFormatter(new Formatter(type: new FormatterType(value: 'xml'), tofile: xmlReportFile))
        File file = new File(project.buildDir, "tmp/android-check/checkstyle-suppress.xml")
        file.parentFile.mkdirs()
        file.delete()
        file << Utils.getResource(project, "checkstyle/suppressions.xml")
        checkStyleTask.properties = file

        checkStyleTask.failOnViolation = false
        Path classpath = checkStyleTask.createClasspath()
        project.rootProject.buildscript.configurations.classpath.resolve().each {
            classpath.createPathElement().location = it
        }
        project.buildscript.configurations.classpath.resolve().each {
            classpath.createPathElement().location = it
        }

        config.getAndroidSources().findAll { it.exists() }.each {
            checkStyleTask.addFileset(project.ant.fileset(dir: it))
        }

        checkStyleTask.perform()
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
    protected boolean isTask() {
        return true
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount Checkstyle rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
