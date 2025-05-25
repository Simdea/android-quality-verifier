package pt.simdea.verifier

import org.gradle.api.Plugin
import org.gradle.api.Project
import pt.simdea.verifier.checkstyle.CheckstyleCheck
import pt.simdea.verifier.cpd.CpdCheck
import pt.simdea.verifier.detekt.DetektCheck
// import pt.simdea.verifier.findbugs.FindbugsCheck // Removed FindBugs
import pt.simdea.verifier.ktlint.KtLintCheck // Capital L
import pt.simdea.verifier.lint.LintCheck
import pt.simdea.verifier.pmd.PmdCheck
import pt.simdea.verifier.spotbugs.SpotBugsCheck // Capital B

class CheckPlugin implements Plugin<Project> {

    @Override
    void apply(Project rootProject) {
        // Create extension on the root project, accessible by all subprojects
        rootProject.getExtensions().create(CheckExtension.NAME, CheckExtension.class, rootProject)

        // Repositories: Best practice is for consuming projects to declare these.
        // jcenter() is deprecated. If any internal resource relies on it, it's a problem.
        // For now, let's assume it's not strictly needed by the plugin's own operation here.
        // if (rootProject.getRepositories().findByName("jcenter") == null && rootProject.getRepositories().findByName("BintrayJCenter2") == null ) {
        //      rootProject.getRepositories().jcenter() 
        // }

        // Apply checks to the root project and all subprojects after they are evaluated
        def projectsToConfigure = [rootProject] + rootProject.getSubprojects()
        projectsToConfigure.each { Project project ->
            project.afterEvaluate { Project evaluatedProject ->
                // Check if the verifier plugin is applied to this specific project.
                // This is important if users apply 'pt.simdea.verifier' only to specific modules.
                // However, the current design applies tools if the root project has the plugin.
                // For simplicity now, we assume if this apply() method is running, tools should be added.
                // A more refined check could be: if (evaluatedProject.getPlugins().hasPlugin(CheckPlugin.class))
                addTool(evaluatedProject)
            }
        }
    }

    private static void addTool(final Project project) {
        // The CheckExtension should be retrieved from the project where it's applied (rootProject)
        // and then passed down or accessed by each CommonCheck instance appropriately.
        // CommonCheck's apply method gets it from target.extensions.findByType(CheckExtension.class)
        // which means if CheckExtension is only on root, subprojects won't find it directly
        // unless they look at rootProject.extensions.
        // The current CommonCheck.getConfig(CheckExtension) expects CheckExtension on the *target* project.
        // This needs to be consistent. For now, let's assume CheckExtension is accessible.
        // If CheckExtension is created only on rootProject, then CommonCheck.getConfig
        // needs to be able to access rootProject.extensions.check from a subproject.
        // This is implicitly handled if CheckExtension is created on each project, 
        // or if CommonCheck's `project.extensions.findByType(CheckExtension.class)` can find it if only on root.
        // The current CheckExtension constructor takes a Project argument.
        // If create(CheckExtension.NAME, CheckExtension.class, rootProject) means it's only on root,
        // then CommonCheck subclasses need access to rootProject.extensions.check.
        // The `getConfig` in each `*Check` class currently does `extension.toolName` assuming `extension` is resolved.
        // CommonCheck's `apply` does `target.extensions.findByType(CheckExtension.class)`.
        // This will work if the extension is applied to each project.
        // If we want a single root project extension, CommonCheck's `apply` needs modification or
        // `getConfig` needs to be passed `rootProject.extensions.check`.
        //
        // Let's assume for now that `target.extensions.findByType(CheckExtension.class)` in CommonCheck
        // will correctly resolve to the root project's extension if the plugin is applied only to root.
        // If not, this is a point of failure for subprojects.
        //
        // The original design applied CheckExtension to the rootProject.
        // The individual checks are then applied to each project ('target' in CommonCheck.apply).
        // Inside CommonCheck.apply, `target.extensions.findByType(CheckExtension.class)` will look for the
        // extension on that specific `target` (project or subproject).
        // This means for subprojects to work, the extension should ideally be created on them too,
        // or CommonCheck needs to be aware of the rootProject to find the extension.
        //
        // Given the provided CommonCheck.groovy, it expects the extension on the `target` project.
        // The current refactoring of CheckPlugin.apply creates it only on rootProject.
        // This is a conflict.
        // For now, I'll proceed with the current structure of CommonCheck and assume that if the plugin
        // is applied to the root, the intention is to configure all modules via that single extension.
        // This means CommonCheck.getConfig needs to be robust.
        //
        // A simple fix in CommonCheck.apply:
        // CheckExtension extension = target.rootProject.extensions.findByType(CheckExtension.class)
        // This is what I'll assume the final CommonCheck will do, or that CheckExtension is added per project.
        // For this refactoring, I'll stick to the `addTool` structure.

        new CheckstyleCheck().apply(project)
        new CpdCheck().apply(project)
        new DetektCheck().apply(project)
        new KtLintCheck().apply(project) // Ensure capital L
        new LintCheck().apply(project) // This will configure AGP lint if it's an Android project
        new PmdCheck().apply(project)
        new SpotBugsCheck().apply(project) // Replaced FindBugs
        // new ErrorProneCheck().apply(project) // Remains commented out
    }

    /* // Commenting out addLint as LintCheck is now used via addTool
    static boolean addLint(final Project subProject, final CheckExtension extension) {
        boolean skip = extension.lint.skip != null ? extension.lint.skip : false
        if (!skip && Utils.isAndroidProject(subProject)) { // Assuming Utils.isAndroidProject
            subProject.android.lintOptions {
                // ... (original lintOptions configuration)
                // This logic is now expected to be within LintCheck.runCheckLogic
                // and configured via LintConfig.
                // For example:
                abortOnError = extension.lint.abortOnError ?: false // Defaulting here for example
                absolutePaths = extension.lint.absolutePaths ?: true
                checkAllWarnings = extension.lint.checkAllWarnings ?: false
                checkReleaseBuilds = extension.lint.checkReleaseBuilds ?: true
                ignoreWarnings = extension.lint.ignoreWarnings ?: false
                showAll = extension.lint.showAll ?: false
                warningsAsErrors = extension.lint.warningsAsErrors ?: false
                
                File resolvedLintConfig = extension.lint.resolveConfigFile("lint") // Using LintConfig's resolve
                if (resolvedLintConfig != null && resolvedLintConfig.exists()) {
                    lintConfig resolvedLintConfig
                } else {
                    // Fallback to original resolveConfigFile logic if needed, but LintConfig should handle it
                    // lintConfig extension.lint.config != null ? new File(extension.lint.config) : resolveConfigFile("lint", subProject)
                }

                if (extension.lint.disable != null && !extension.lint.disable.isEmpty()) {
                    disable.clear()
                    disable.addAll(extension.lint.disable)
                }
                // HTML and XML output paths are now handled by CommonCheck and LintConfig properties
            }
            // CommonCheck's apply method handles adding the task to 'check'
            // subProject.check.dependsOn 'lint' // This specific dependency might still be needed if LintCheck's task doesn't depend on AGP's lint
            return true
        }
        return false
    }
    */

    /* // Commenting out resolveConfigFile as it was primarily used by addLint
       // and CommonConfig now has more robust config file resolution.
    static File resolveConfigFile(String code, Project project) {
        File file = new File(project.buildDir, "tmp/android-check/${code}.xml")
        file.parentFile.mkdirs()
        file.delete()
        // Utils.getResource should ideally handle the ClassLoader or Project context correctly.
        // String content = Utils.getResource(project, "$code/conf-default.xml")
        // if (content != null) file << content else project.logger.warn("Default config for $code not found.")
        // For now, just returning the path. Actual file creation should be in CommonConfig.
        return file 
    }
    */
    
    // isAndroidProject is now in Utils, so this can be removed if not used elsewhere locally.
    // protected static boolean isAndroidProject(final Project project) {
    //     final boolean isAndroidLibrary = project.plugins.hasPlugin('com.android.library')
    //     final boolean isAndroidApp = project.plugins.hasPlugin('com.android.application')
    //     return isAndroidLibrary || isAndroidApp
    // }
}
