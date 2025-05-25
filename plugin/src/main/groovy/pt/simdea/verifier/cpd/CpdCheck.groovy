package pt.simdea.verifier.cpd

import groovy.xml.XmlParser
import net.sourceforge.pmd.cpd.CPDTask
import org.apache.tools.ant.types.FileSet
import org.apache.tools.ant.Project as AntProject
import org.gradle.api.Project
import org.gradle.api.file.FileCollection // For source sets
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class CpdCheck extends CommonCheck<CpdConfig> {

    CpdCheck() {
        super('cpd', 'androidCpd', 'Runs CPD (Copy/Paste Detector) analysis')
        this.defaultConfigFile = null // CPD doesn't use a ruleset config file
    }

    @Override
    protected CpdConfig getConfig(CheckExtension extension) {
        if (extension.cpd == null) {
            if (this.project != null) {
                extension.cpd = new CpdConfig(this.project)
            } else {
                throw new IllegalStateException("Project reference not available in CpdCheck for creating CpdConfig.")
            }
        }
        return extension.cpd
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isJavaProject(project) || Utils.isAndroidProject(project) || Utils.isKotlinProject(project)
    }

    @Override
    protected void runCheckLogic(Project project, CpdConfig config) {
        CPDTask cpdTask = new CPDTask()
        AntProject antProjectInstance = project.getAnt().getAntProject()
        project.getAnt().getReferences().each { key, value ->
            antProjectInstance.addReference(key.toString(), value)
        }
        cpdTask.setProject(antProjectInstance)

        cpdTask.setMinimumTokenCount(config.minimumTokenCount)
        cpdTask.setLanguage(config.language) // language property from CpdConfig
        cpdTask.setEncoding(config.encoding ?: "UTF-8") // encoding from CommonConfig
        
        cpdTask.setFormat(CPDTask.FormatAttribute.XML)
        cpdTask.setOutputFile(this.xmlReportFile)

        // Collect all relevant source directories (Java and Kotlin)
        List<File> sourceDirsToAnalyze = new ArrayList<>()
        if (Utils.isAndroidProject(project)) {
            project.android.sourceSets.each { sourceSet ->
                // Typically analyze 'main' variant related sources or specific build variant sources.
                // This might need refinement if a specific variantName is passed and should be exclusively used.
                sourceDirsToAnalyze.addAll(sourceSet.java.getSrcDirs())
                if (sourceSet.hasProperty('kotlin')) { // Check if kotlin property exists
                     FileCollection kotlinDirs = sourceSet.kotlin.getSrcDirs()
                     if(kotlinDirs != null) sourcesToAnalyze.addAll(kotlinDirs.getFiles())
                }
            }
        } else if (Utils.isJavaProject(project)) { // For pure Java projects
            project.sourceSets.main.allJava.getSrcDirs().each { sourceDirsToAnalyze.add(it) }
            if (project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") || project.plugins.hasPlugin("kotlin")) {
                 project.sourceSets.main.kotlin.getSrcDirs().each { sourceDirsToAnalyze.add(it) }
            }
        }
        
        sourceDirsToAnalyze = sourceDirsToAnalyze.unique().findAll { it != null && it.exists() }

        if (sourceDirsToAnalyze.isEmpty()) {
            project.logger.warn("No source directories found for CPD analysis in project ${project.name}. Skipping execution.")
            this.xmlReportFile.text = '<?xml version="1.0" encoding="UTF-8"?><pmd-cpd></pmd-cpd>'
            return
        }

        sourceDirsToAnalyze.each { File srcDir ->
            FileSet antFileSet = new FileSet()
            antFileSet.setProject(antProjectInstance)
            antFileSet.setDir(srcDir)
            // Include patterns based on configured language or common types
            if ("java".equalsIgnoreCase(config.language) || "kotlin".equalsIgnoreCase(config.language) || "jsp".equalsIgnoreCase(config.language)) {
                 antFileSet.setIncludes("**/*.java, **/*.kt, **/*.jsp") // Adjust as per supported languages by CPD
            } else {
                 // Add more language patterns here or make it configurable
                 antFileSet.setIncludes("**/*.java, **/*.kt") 
            }
            cpdTask.addFileset(antFileSet)
        }

        project.logger.lifecycle("Executing CPD analysis for project ${project.name}...")
        try {
            cpdTask.execute() // CPDTask uses execute()
        } catch (Exception e) {
            project.logger.error("CPD execution failed for project ${project.name} with an exception: ${e.message}", e)
            if (!this.xmlReportFile.exists() || this.xmlReportFile.length() == 0) {
                this.xmlReportFile.text = '<?xml version="1.0" encoding="UTF-8"?><pmd-cpd><error message="CPD execution failed."/></pmd-cpd>'
            }
        }
    }

    @Override
    protected int getErrorCount(File report) {
        if (report == null || !report.exists() || report.length() == 0) {
            project.logger.warn("CPD XML report not found or empty: ${report?.absolutePath}. Assuming 0 duplications.")
            return 0
        }
        try {
            def parsedReport = new XmlParser().parse(report)
            int duplicationCount = parsedReport.duplication.size()
            project.logger.lifecycle("CPD XML report parsed. Duplication count: ${duplicationCount}")
            return duplicationCount 
        } catch (Exception e) {
            project.logger.error("Error parsing CPD XML report: ${e.getMessage()}", e)
            return -1 
        }
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReport) { 
        String reportPath = (htmlReport != null && htmlReport.exists()) ?
                            "file://${htmlReport.absolutePath}" :
                            "HTML report not available. Check XML report: file://${xmlReportFile?.absolutePath}";
        return "$errorCount code duplications found by CPD. See the report at: $reportPath"
    }
}
