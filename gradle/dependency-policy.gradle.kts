import org.gradle.api.artifacts.dsl.LockMode

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

tasks.register("resolveAllDependencies") {
    group = "verification"
    description = "Resolves every resolvable project configuration to validate or refresh dependency state"
    notCompatibleWithConfigurationCache("Inspects project configurations at execution time")
    val projectPath = project.path
    doLast {
        configurations.filter { it.isCanBeResolved }.sortedBy { it.name }.forEach {
            logger.lifecycle("Resolving $projectPath:${it.name}")
            it.resolve()
        }
    }
}
