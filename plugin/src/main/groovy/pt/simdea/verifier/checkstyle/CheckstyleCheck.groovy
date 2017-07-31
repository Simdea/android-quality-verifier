package pt.simdea.verifier.checkstyle

import com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask
import com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask.Formatter
import com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask.FormatterType
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.CommonConfig
import pt.simdea.verifier.Utils

class CheckstyleCheck extends CommonCheck {

    CheckstyleCheck() { super('checkstyle', 'androidCheckstyle', 'Runs Android Checkstyle') }

    @Override
    protected CommonConfig getConfig(CheckExtension extension) { return extension.checkstyle }

    @Override
    protected void performCheck(Project project, List<File> sources, File configFile, File xmlReportFile) {
        CheckstyleAntTask checkStyleTask = new CheckstyleAntTask()

        checkStyleTask.project = project.ant.antProject
        checkStyleTask.setConfigUrl(configFile.toURI().toURL())
        checkStyleTask.addFormatter(new Formatter(type: new FormatterType(value: 'xml'), tofile: xmlReportFile))
        File file = new File(project.buildDir, "tmp/android-check/checkstyle-suppress.xml")
        file.parentFile.mkdirs()
        file.delete()
        file << Utils.getResource(project, "checkstyle/suppressions.xml")
        checkStyleTask.properties = file

        checkStyleTask.failOnViolation = false

        sources.findAll { it.exists() }.each {
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
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount Checkstyle rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
