package pt.simdea.verifier.cpd

import org.gradle.api.Project
import pt.simdea.verifier.CommonConfig

class CpdConfig extends CommonConfig {

    CpdConfig(Project project) { super(project) }

    int minimumTokenCount = 100
    String language = 'java'

}
