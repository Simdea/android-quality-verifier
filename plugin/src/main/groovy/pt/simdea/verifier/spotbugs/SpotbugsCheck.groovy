package pt.simdea.verifier.spotbugs

import edu.umd.cs.findbugs.anttask.FindBugsTask as SpotBugsTask
import groovy.util.slurpersupport.GPathResult
import org.apache.tools.ant.types.FileSet
import org.apache.tools.ant.types.Path
import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.CommonConfig

class SpotbugsCheck extends CommonCheck {

    SpotbugsCheck() { super('spotbugs', 'androidSpotbugs', 'Runs Android SpotBugs') }

    @Override
    protected Set<String> getDependencies() { ['assemble'] }

    @Override
    protected CommonConfig getConfig(CheckExtension extension) { return extension.spotbugs }

    @Override
    protected void performCheck(Project project, List<File> sources, File configFile, File xmlReportFile) {

        SpotBugsTask spotBugsTask = new SpotBugsTask()
        project.ant.lifecycleLogLevel = "VERBOSE"
        spotBugsTask.project = project.ant.antProject
        spotBugsTask.workHard = true
        spotBugsTask.excludeFilter = configFile
        spotBugsTask.output = "xml:withMessages"
        spotBugsTask.outputFile = xmlReportFile
        spotBugsTask.failOnError = true
        spotBugsTask.debug = true

        Path sourcePath = spotBugsTask.createSourcePath()
        sources.findAll { it.exists() }.each {
            sourcePath.addFileset(project.ant.fileset(dir: it))
        }

        Path classpath = spotBugsTask.createClasspath()
        project.rootProject.buildscript.configurations.classpath.resolve().each {
            classpath.createPathElement().location = it
        }
        project.buildscript.configurations.classpath.resolve().each {
            classpath.createPathElement().location = it
        }

        Set<String> includes = []
        sources.findAll { it.exists() }.each { File directory ->
            FileSet fileSet = project.ant.fileset(dir: directory)
            Path path = project.ant.path()
            path.addFileset(fileSet)

            path.each {
                String includePath = new File(it.toString()).absolutePath - directory.absolutePath
                includes.add("**${includePath.replaceAll('\\.java$', '')}*")
            }
        }


        spotBugsTask.addFileset()

        spotBugsTask.perform()
    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        GPathResult xml = new XmlSlurper().parseText(xmlReportFile.text)
        return xml.FindBugsSummary.getProperty('@total_bugs').text() as int
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return "$errorCount FindBugs rule violations were found. See the report at: ${htmlReportFile.toURI()}"
    }

}
