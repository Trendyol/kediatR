package com.trendyol.kediatr

/**
 * Internal provider class for creating pipeline behavior instances with dependency injection support.
 *
 * This class acts as a factory for pipeline behaviors, using the dependency provider
 * to resolve and instantiate behavior instances. It's used internally by the Registry
 * to manage the lifecycle and creation of pipeline behaviors that provide cross-cutting
 * concerns for queries and commands.
 *
 * @param H The type of pipeline behavior that extends PipelineBehavior
 * @param dependencyProvider The dependency provider used to resolve behavior instances
 * @param type The class type of the behavior to create
 * @see PipelineBehavior
 * @see DependencyProvider
 * @see Registry
 */
internal class PipelineProvider<H : PipelineBehavior>(
  private val dependencyProvider: DependencyProvider,
  private val type: Class<H>
) {
  /**
   * Creates and returns a new instance of the pipeline behavior.
   *
   * @return A new instance of the pipeline behavior resolved through the dependency provider
   * @throws Exception if the behavior cannot be instantiated or resolved
   */
  fun get(): H = dependencyProvider.getSingleInstanceOf(type)
}
