package pt.simdea.verifier.detekt

import org.gradle.api.Project
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class DetektCheck extends CommonCheck<DetektConfig> {
    DetektCheck() {
        super('detekt', 'androidDetekt', 'Android kotlin detekt')
    }

    @Override
    protected DetektConfig getConfig(CheckExtension extension) {
        return extension.detekt
    }

    @Override
    protected void run(Project project, Project rootProject, DetektConfig config) {

        def output = new File(project.buildDir, "reports/detekt/")

        def configFile = config.resolveConfigFileYml(taskCode)
        inputs.files(project.fileTree(dir: "src", include: "**/*.kt"), configFile)
        outputs.dir(output.toString())
        description = 'Runs detekt.'
        main = 'io.gitlab.arturbosch.detekt.cli.Main'
        classpath = project.configurations.detektCheck
        args = [
                "--config", configFile,
                "--input", project.file("."),
                "--output", output
        ]

    }

    @Override
    protected int getErrorCount(File xmlReportFile) {
        return 0
    }

    @Override
    protected boolean isSupported(Project project) {
        return Utils.isKotlinProject(project)
    }

    @Override
    protected boolean isTask() {
        return true
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return null
    }
}
