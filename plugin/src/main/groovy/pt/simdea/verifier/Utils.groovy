package pt.simdea.verifier

import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile

final class Utils {

    private Utils() { throw new UnsupportedOperationException() }

    static String getResource(Project project, String resourcePath) {
        Set<File> files = new HashSet<>()
        files += project.buildscript.configurations.classpath.resolve()
        files += project.rootProject.buildscript.configurations.classpath.resolve()
        File file = files.find { new JarFile(it).getJarEntry(resourcePath) }
        if (file == null) {
            return null
        } else {
            JarFile jarFile = new JarFile(file)
            JarEntry jarEntry = jarFile.getJarEntry(resourcePath)
            return jarFile.getInputStream(jarEntry).text
        }
    }

    static List<File> getAndroidSources(Project project) {
        project.android.sourceSets.inject([]) {
            dirs, sourceSet -> dirs + sourceSet.java.srcDirs
        }
    }

    static boolean isKotlinProject(Project project) {
        final boolean isKotlin = project.plugins.hasPlugin('kotlin')
        final boolean isKotlinAndroid = project.plugins.hasPlugin('kotlin-android')
        final boolean isKotlinPlatformCommon = project.plugins.hasPlugin('kotlin-platform-common')
        final boolean isKotlinPlatformJvm = project.plugins.hasPlugin('kotlin-platform-jvm')
        final boolean isKotlinPlatformJs = project.plugins.hasPlugin('kotlin-platform-js')
        return isKotlin || isKotlinAndroid || isKotlinPlatformCommon || isKotlinPlatformJvm || isKotlinPlatformJs
    }

    static boolean isJavaProject(Project project) {
        final boolean isJava = project.plugins.hasPlugin('java')
        final boolean isJavaLibrary = project.plugins.hasPlugin('java-library')
        final boolean isJavaGradlePlugin = project.plugins.hasPlugin('java-gradle-plugin')
        return isJava || isJavaLibrary || isJavaGradlePlugin
    }

    static boolean isAndroidProject(Project project) {
        final boolean isAndroidLibrary = project.plugins.hasPlugin('com.android.library')
        final boolean isAndroidApp = project.plugins.hasPlugin('com.android.application')
        final boolean isAndroidTest = project.plugins.hasPlugin('com.android.test')
        final boolean isAndroidFeature = project.plugins.hasPlugin('com.android.feature')
        final boolean isAndroidInstantApp = project.plugins.hasPlugin('com.android.instantapp')
        return isAndroidLibrary || isAndroidApp || isAndroidTest || isAndroidFeature || isAndroidInstantApp
    }

}
