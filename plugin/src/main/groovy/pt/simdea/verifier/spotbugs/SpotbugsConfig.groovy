package pt.simdea.verifier.spotbugs

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class SpotbugsConfig extends CommonConfig {

    SpotbugsConfig(Project project) { super(project) }

    @Override
    List<File> getAndroidSources() {
        def buildDirIncludes = new ArrayList()

        if (isAndroidProject(project)) {
            buildDirIncludes.add("intermediates/classes/debug/**")
            buildDirIncludes.add("intermediates/packaged-classes/debug/**") // AGP 3.2.0 and higher.
        } else {
            if (isKotlinProject(project)) {
                buildDirIncludes.add("classes/kotlin/main/**")
            }

            if (isJavaProject(project)) {
                buildDirIncludes.add("classes/java/main/**")
            }
        }
        return buildDirIncludes
    }

    private static boolean isKotlinProject(final Project project) {
        final boolean isKotlin = project.plugins.hasPlugin('kotlin')
        final boolean isKotlinAndroid = project.plugins.hasPlugin('kotlin-android')
        final boolean isKotlinPlatformCommon = project.plugins.hasPlugin('kotlin-platform-common')
        final boolean isKotlinPlatformJvm = project.plugins.hasPlugin('kotlin-platform-jvm')
        final boolean isKotlinPlatformJs = project.plugins.hasPlugin('kotlin-platform-js')
        return isKotlin || isKotlinAndroid || isKotlinPlatformCommon || isKotlinPlatformJvm || isKotlinPlatformJs
    }

    private static boolean isJavaProject(final Project project) {
        final boolean isJava = project.plugins.hasPlugin('java')
        final boolean isJavaLibrary = project.plugins.hasPlugin('java-library')
        final boolean isJavaGradlePlugin = project.plugins.hasPlugin('java-gradle-plugin')
        return isJava || isJavaLibrary || isJavaGradlePlugin
    }

    private static boolean isAndroidProject(final Project project) {
        final boolean isAndroidLibrary = project.plugins.hasPlugin('com.android.library')
        final boolean isAndroidApp = project.plugins.hasPlugin('com.android.application')
        final boolean isAndroidTest = project.plugins.hasPlugin('com.android.test')
        final boolean isAndroidFeature = project.plugins.hasPlugin('com.android.feature')
        final boolean isAndroidInstantApp = project.plugins.hasPlugin('com.android.instantapp')
        return isAndroidLibrary || isAndroidApp || isAndroidTest || isAndroidFeature || isAndroidInstantApp
    }

}
