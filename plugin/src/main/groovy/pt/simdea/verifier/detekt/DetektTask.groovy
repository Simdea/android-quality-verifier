package pt.simdea.verifier.detekt

import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

public class DetektTask extends JavaExec implements Task {

    @TaskAction
    void perfom(File configFile, String taskDescription, File output) {
        main = 'io.gitlab.arturbosch.detekt.cli.Main'
        inputs.files(project.fileTree(dir: "src", include: "**/*.kt"), configFile)
        outputs.dir(output.toString())
        description = taskDescription
        classpath = project.configurations.detektCheck
        args = [
                "--config", configFile,
                "--input", project.file("."),
                "--output", output
        ]
        execute()
    }

}
