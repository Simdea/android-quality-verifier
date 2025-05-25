package pt.simdea.verifier.spotbugs

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig
import pt.simdea.verifier.Utils

class SpotBugsConfig extends CommonConfig {

    SpotBugsConfig(Project project) { super(project) }

    List<String> getAndroidClasses() {
        def buildDirIncludes = new ArrayList()

        if (Utils.isAndroidProject(project)) {
            buildDirIncludes.add("intermediates/classes/debug/**")
            buildDirIncludes.add("intermediates/packaged-classes/debug/**") // AGP 3.2.0 and higher.
        } else {
            if (Utils.isKotlinProject(project)) {
                buildDirIncludes.add("classes/kotlin/main/**")
            }

            if (Utils.isJavaProject(project)) {
                buildDirIncludes.add("classes/java/main/**")
            }
        }
        return buildDirIncludes
    }
}
