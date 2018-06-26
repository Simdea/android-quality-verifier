package pt.simdea.verifier.ktlint

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import pt.simdea.verifier.CheckExtension
import pt.simdea.verifier.CommonCheck
import pt.simdea.verifier.Utils

class KtLintCheck extends CommonCheck<KtLintConfig> {
    KtLintCheck() {
        super('ktlint', 'androidKtlint', 'Android Kotlin Lint')
    }

    @Override
    protected KtLintConfig getConfig(CheckExtension extension) {
        return extension.ktLint
    }

    @Override
    protected void run(Project project, Project rootProject, KtLintConfig config) {
        project.configurations {
            ktlint
        }

        project.dependencies {
            ktlint "com.github.shyiko:ktlint:0.23.1"
        }

        def outputDir = "${project.buildDir}/reports/ktlint/"
        def configurationFiles = rootProject.fileTree(dir: ".", include: "**/.editorconfig")
        def inputFiles = project.fileTree(dir: "src", include: "**/*.kt")

        project.task(taskName, type: JavaExec) {
            inputs.files(inputFiles, configurationFiles)
            outputs.dir(outputDir)
            description = taskDescription
            main = 'com.github.shyiko.ktlint.Main'
            classpath = project.configurations.ktlint
            def outputFile = "${outputDir}ktlint-checkstyle-report.xml"
            args '--reporter=plain', "--reporter=checkstyle,output=${outputFile}", 'src/**/*.kt'
        }

        project.task('ktlintFormat', type: JavaExec) {
            inputs.files(inputFiles, configurationFiles)
            outputs.upToDateWhen { true } // We only need the input as it'll change when we reformat.
            description = "Runs ktlint and autoformats your code."
            main = "com.github.shyiko.ktlint.Main"
            classpath = project.configurations.ktlint
            args "-F", "src/**/*.kt"
        }
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
        return false
    }

    @Override
    protected String getErrorMessage(int errorCount, File htmlReportFile) {
        return null
    }
}
