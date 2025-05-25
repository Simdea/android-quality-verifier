package pt.simdea.verifier.lint

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class LintConfig extends CommonConfig {

    // Existing properties - ensure they are public or have public setters
    boolean absolutePaths = true // Defaulting to true as per common AGP behavior
    boolean checkAllWarnings = false
    Set<String> disable = new HashSet<>() // Initialize to avoid NPE
    boolean checkReleaseBuilds = true // Defaulting to true as per common AGP behavior
    File lintConfig = null // Handled by resolveConfigFile in CommonConfig if not set explicitly
    boolean ignoreWarnings = false
    boolean showAll = false
    boolean warningsAsErrors = false
    // Add any other lintOptions properties you want to make configurable

    LintConfig(Project project) { 
        super(project)
        // Default config file name for Lint (used by CommonConfig.resolveConfigFile if lintConfig is null)
        this.defaultConfigFile = "lint.xml" 
    }

    @Override
    boolean shouldResolveErrors() {
        return true // Enable error reporting for Lint
    }

    // Setter methods to allow configuration from build.gradle DSL
    void absolutePaths(boolean value) { this.absolutePaths = value }
    void checkAllWarnings(boolean value) { this.checkAllWarnings = value }
    void disable(Set<String> value) { this.disable = value != null ? new HashSet<>(value) : new HashSet<>() }
    void disable(String... issues) { this.disable = issues != null ? new HashSet<>(Arrays.asList(issues)) : new HashSet<>() }
    void checkReleaseBuilds(boolean value) { this.checkReleaseBuilds = value }
    void lintConfig(File value) { this.lintConfig = value }
    void lintConfig(String path) { this.lintConfig = project.file(path) } // project is from CommonConfig
    void ignoreWarnings(boolean value) { this.ignoreWarnings = value }
    void showAll(boolean value) { this.showAll = value }
    void warningsAsErrors(boolean value) { this.warningsAsErrors = value }

    // Override resolveConfigFile to prioritize explicit lintConfig property
    @Override
    File resolveConfigFile(String taskCode /* taskCode is 'lint' */) {
        if (this.lintConfig != null) {
            if (this.lintConfig.exists()) {
                project.logger.info("Using custom Lint config file (from lintConfig property): ${this.lintConfig.absolutePath}")
                return this.lintConfig
            } else {
                project.logger.warn("Custom Lint config file (from lintConfig property) specified but not found: ${this.lintConfig.absolutePath}. Falling back to default search.")
            }
        }
        // Fallback to CommonConfig's default search logic (config/lint.xml, etc.)
        return super.resolveConfigFile(taskCode)
    }
}
