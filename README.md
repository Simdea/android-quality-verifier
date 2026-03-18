Android Quality Verifier
===============
[![Build Status][11]][13] [![Download][14]][15] [![gitcheese.com][16]][17]

Static code analysis plugin for Android and Kotlin projects.
This is a fork of [the original android-check plugin][1], which implements a really useful concept.

Build status
------------

| Master   | [![Build Status][11]][13] |
|----------|-------------|
| **Dev** | [![dev][12]][13] |

Supported Tools
---------------
The plugin integrates the following static analysis tools:

*   **Checkstyle:** (v10.12.7) For Java code style checking.
*   **PMD:** (v6.55.0) For identifying potential bugs, dead code, suboptimal code, etc., in Java.
*   **SpotBugs:** (v4.8.3) Successor to FindBugs, for static bytecode analysis to find bugs in Java code. (Replaces FindBugs).
*   **CPD:** (Part of PMD) For finding duplicated code in Java.
*   **Detekt:** (v1.23.6) Static analysis for Kotlin code, focusing on code smells, complexity, and style issues.
*   **Ktlint:** (v0.50.0 tool, v12.1.0 Gradle plugin) An anti-bikeshedding Kotlin linter with built-in formatter.
*   **ErrorProne:** (v2.26.1) Catches common Java programming mistakes at compile-time (via plugin dependency).
*   **Android Lint:** Integrated via the Android Gradle Plugin.

Usage
-----
This plugin is available in jCenter. It attaches itself to the `check` task if it finds it (that is, you don't use the `plugins` block and you apply either the application or library Android plugins first) - otherwise you'll need to execute the corresponding tasks manually when desired: `androidCheckstyle` for [CheckStyle][3], `androidSpotbugs` for [SpotBugs][4], `CPDTask` for [CPD][5], `androidPmd` for [PMD][6], `androidDetekt` for [Detekt][detekt-link], and `androidKtlint` for [Ktlint][ktlint-link].

Alternatively, you can execute the specific tasks for each tool:
*   `runCheckstyle` for Checkstyle
*   `runSpotbugs` for SpotBugs (formerly `runFindbugs`)
*   `runPmd` for PMD
*   `runCpd` for CPD
*   `runDetekt` for Detekt (Kotlin)
*   `runKtlint` for Ktlint (Kotlin)

The plugin also configures Android Lint if an Android plugin is applied.

Configuration
-------------

### Install

##### Add to main build.gradle:
```gradle
buildscript {
    ...
    dependencies {
        ...
        classpath 'pt.simdea.verifier:verifier:4.0.0-alpha1'
        ...
    }
    ...
}
```

##### add to app build.gradle:
```gradle
apply plugin: 'pt.simdea.verifier'
apply plugin: 'com.android.application'
...
dependencies {
    ...
    implementation 'pt.simdea.verifier:verifier-annotations:0.0.3'
    ...
}

```
### Recommended

The default one.

### Customized

```groovy
// Configuration is completely optional, defaults will be used if not present
check {
  // Do absolutely nothing, default: false
  skip true/false
  // Fails build if a violation is found, default: true
  abortOnError true/false //Ignored if all per-tool confs are set to abortOnError false (see below)
  
  // Checkstyle configuration
  checkstyle {
    // Completely skip CheckStyle, default: false
    skip true/false

    // Fails build if CheckStyle rule violation is found, default: false
    abortOnError true/false

    // Configuration file for CheckStyle, default: <project_path>/config/checkstyle.xml, if non-existent then <project_path>/<module_path>/config/checkstyle.xml, if non-existent then plugin/src/main/resources/checkstyle/conf-default.xml
    config 'path/to/checkstyle.xml'

    // Output file for XML reports, default: new File(project.buildDir, 'outputs/checkstyle/checkstyle.xml')
    reportXML new File(project.buildDir, 'path/where/you/want/checkstyle.xml')

    // Output file for HTML reports, default: not generated
    reportHTML new File(project.buildDir, 'path/where/you/want/checkstyle.html')
  }
  
  // Cpd configuration
  cpd {
    // Same options as Checkstyle
  }
  
  // SpotBugs configuration (replaces FindBugs)
  spotbugs {
    // Completely skip SpotBugs, default: false
    skip true/false

    // Fails build if SpotBugs rule violation is found, default: false
    abortOnError true/false

    // Configuration file for SpotBugs (e.g., exclude filter), default: <project_path>/config/spotbugs.xml (or findbugs.xml for backward compatibility), if non-existent then plugin/src/main/resources/findbugs/conf-default.xml
    config 'path/to/spotbugs-exclude.xml'

    // Output file for XML reports, default: new File(project.buildDir, 'outputs/spotbugs/spotbugs.xml')
    reportXML new File(project.buildDir, 'path/where/you/want/spotbugs.xml')

    // Output file for HTML reports, default: not generated
    reportHTML new File(project.buildDir, 'path/where/you/want/spotbugs.html')
  }

  // PMD configuration
  pmd {
    // Same options as Checkstyle (refer to Checkstyle section for available options like skip, abortOnError, config, reportXML, reportHTML)
  }

  // Detekt configuration (for Kotlin)
  detekt {
    // Completely skip Detekt, default: false
    skip true/false

    // Fails build if Detekt rule violation is found, default: false
    abortOnError true/false

    // Configuration file for Detekt, default: <project_path>/detekt-config.yml or <module_path>/detekt-config.yml, if non-existent then plugin/src/main/resources/detekt/conf-default.yml
    config 'path/to/detekt-config.yml'

    // Output file for XML reports, default: new File(project.buildDir, 'outputs/detekt/detekt.xml')
    reportXML new File(project.buildDir, 'path/where/you/want/detekt.xml')

    // Output file for HTML reports, default: new File(project.buildDir, 'outputs/detekt/detekt.html')
    reportHTML new File(project.buildDir, 'path/where/you/want/detekt.html')
  }

  // Ktlint configuration (for Kotlin)
  ktlint {
    // Completely skip Ktlint, default: false
    skip true/false

    // Fails build if Ktlint rule violation is found, default: false
    abortOnError true/false

    // Ktlint uses .editorconfig for rules. The plugin typically picks up .editorconfig from the project root.
    // The 'config' property here is for consistency but Ktlint itself doesn't use a single XML config file like Checkstyle.
    // If you have a specific .editorconfig and want to ensure it's recognized (though usually automatic):
    // config 'path/to/.editorconfig' // This is more a pointer to your .editorconfig if not at root.

    // Output file for XML (checkstyle format) reports, default: new File(project.buildDir, 'outputs/ktlint/ktlint-checkstyle-report.xml')
    reportXML new File(project.buildDir, 'path/where/you/want/ktlint-checkstyle.xml')

    // Output file for HTML reports, default: new File(project.buildDir, 'outputs/ktlint/ktlint-html-report.html')
    reportHTML new File(project.buildDir, 'path/where/you/want/ktlint.html')
  }
  
  // Lint configuration
  lint {
    // Same vars of android lint options
  }
  
  // Detekt configuration (for Kotlin)
  detekt {
    // Completely skip Detekt, default: false
    skip true/false

    // Fails build if Detekt rule violation is found, default: true
    abortOnError true/false

    // Configuration file for Detekt (YML). 
    // Default lookup: <project_path>/config/detekt.yml, 
    // then <project_path>/<module_path>/config/detekt.yml, 
    // then plugin's internal 'conf-default.yml'.
    // To use Detekt's own defaults, ensure no custom file is found and 
    // the plugin's 'conf-default.yml' is removed or made inaccessible.
    config 'path/to/your/detekt.yml'

    // Output file for XML reports (Checkstyle format).
    // Default: build/outputs/detekt/detekt.xml in the module.
    reportXML new File(project.buildDir, 'path/where/you/want/detekt.xml')

    // Output file for HTML reports.
    // Default: build/outputs/detekt/detekt.html in the module.
    reportHTML new File(project.buildDir, 'path/where/you/want/detekt.html')
  }
  
  // Ktlint configuration (for Kotlin)
  ktlint {
    // Completely skip Ktlint, default: false
    skip true/false

    // Fails build if Ktlint rule violation is found, default: true
    abortOnError true/false

    // Ktlint uses .editorconfig files for rule settings, discovered automatically 
    // from the project structure. This 'config' property is for compatibility 
    // with the plugin's structure but is not the primary way to configure Ktlint rules.
    // config 'path/to/.editorconfig' // Generally not needed.

    // Output file for XML reports (Checkstyle format).
    // Default: build/outputs/ktlint/ktlint.xml (points to the main source set report).
    reportXML new File(project.buildDir, 'path/where/you/want/ktlint-main.xml')

    // Output file for HTML reports.
    // Default: build/outputs/ktlint/ktlint.html (points to the main source set report).
    reportHTML new File(project.buildDir, 'path/where/you/want/ktlint-main.html')
  }
}
```

Developed By
============

The original version of this plugin was developed by:

  - [Jorge Antonio Diaz-Benito Soriano][9]

This fork is owned and maintained by [Simdea][2].

License
=======

See [LICENSE.txt][7].

Original work licensed under [MIT license][8].

[1]: https://github.com/stoyicker/android-check-2
[2]: http://simdea.pt/
[3]: http://checkstyle.sourceforge.net/
[4]: https://spotbugs.github.io
[5]: https://pmd.github.io/pmd-5.7.0/usage/cpd-usage.html
[6]: https://pmd.github.io/
[7]: LICENSE.txt
[8]: https://github.com/noveogroup/android-check/blob/master/LICENSE.txt
[9]: https://www.linkedin.com/in/jorgediazbenitosoriano
[10]: https://spotbugs.github.io
[11]: https://travis-ci.org/Simdea/android-quality-verifier.svg?branch=master
[12]: https://travis-ci.org/Simdea/android-quality-verifier.svg?branch=dev
[13]: https://travis-ci.org/Simdea/android-quality-verifier
[14]: https://api.bintray.com/packages/simdea/android-quality-verifier/pt.simdea.verifier/images/download.svg
[15]: https://bintray.com/simdea/android-quality-verifier/pt.simdea.verifier/4.0.0-alpha1/link
[16]: https://s3.amazonaws.com/gitcheese-ui-master/images/badge.svg
[17]: https://www.gitcheese.com/donate/users/1757083/repos/87924699
[detekt-link]: https://detekt.dev/
[ktlint-link]: https://ktlint.github.io/

[SpotBugs]: https://spotbugs.github.io/
[Detekt]: https://detekt.dev/
[Ktlint]: https://ktlint.github.io/