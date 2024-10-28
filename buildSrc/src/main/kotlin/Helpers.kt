import org.gradle.api.*
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.invoke

fun Project.subprojectsOf(
  vararg parentProjects: String,
  action: Action<Project>,
): Unit = subprojects.filter { parentProjects.contains(it.parent?.name) }.forEach { action(it) }

fun Collection<Project>.of(
  vararg parentProjects: String,
  except: List<String> = emptyList(),
  action: Action<Project>,
): Unit = this.filter {
  parentProjects.contains(it.parent?.name) && !except.contains(it.name)
}.forEach { action(it) }

fun Collection<Project>.of(
  vararg parentProjects: String,
  except: List<String> = emptyList(),
): List<Project> = this.filter {
  parentProjects.contains(it.parent?.name) && !except.contains(it.name)
}

infix fun <T> Property<T>.by(value: T) {
  set(value)
}
