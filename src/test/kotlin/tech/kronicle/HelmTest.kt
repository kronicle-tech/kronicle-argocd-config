package tech.kronicle

import com.lordcodes.turtle.shellRun
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.createDirectories
import kotlin.streams.toList

class HelmTest {

    @TestFactory
    fun helmChartsShouldProduceExpectedOutput(): Stream<DynamicTest> {
        return getHelmChartDirs()
                .map {
                    dynamicTest("Helm chart ${it.fileName} should produce expected output") {
                        val expectedTestOutputDir = it.resolve("expected-test-output")
                        val actualTestOutputDirPath = "build/actual-test-output"
                        val actualTestOutputDir = it.resolve(actualTestOutputDirPath)
                        recreateDir(actualTestOutputDir)
                        val chartName = it.fileName.toString()
                        shellRun("helm",
                            listOf(
                                "dependency",
                                "update",
                                "."
                            ),
                            it.toFile()
                        )
                        shellRun(
                            "helm",
                            listOf(
                                "template",
                                chartName,
                                ".",
                                "-n",
                                "test-namespace",
                                "-f",
                                "values.yaml",
                                "-f",
                                "test-values.yaml",
                                "--output-dir",
                                actualTestOutputDirPath
                            ),
                            it.toFile()
                        )
                        if (UPDATE_EXPECTED_TEST_OUTPUTS) {
                            recreateDir(expectedTestOutputDir)
                            copyDirRecursively(actualTestOutputDir, expectedTestOutputDir)
                        }
                        compareDirs(expectedTestOutputDir, actualTestOutputDir)
                    }
                }
    }

    private fun recreateDir(dir: Path) {
        dir.toFile().deleteRecursively()
        dir.createDirectories()
    }

    private fun copyDirRecursively(sourceDir: Path, targetDir: Path) {
        sourceDir.toFile().copyRecursively(targetDir.toFile())
    }

    private fun getHelmChartDirs(): Stream<Path> {
        return Files.list(Path.of("."))
                .filter { Files.isDirectory(it) && Files.exists(it.resolve("Chart.yaml")) }
    }

    private fun compareDirs(expectedDir: Path, actualDir: Path) {
        val expectedFiles = findAllFiles(expectedDir)
        val actualFiles = findAllFiles(actualDir)
        assertThat(actualFiles).containsExactlyInAnyOrderElementsOf(expectedFiles)
        compareFiles(expectedDir, actualDir, expectedFiles)
    }

    private fun findAllFiles(dir: Path): List<Path> {
        return Files.find(dir, Int.MAX_VALUE, { _, attributes -> attributes.isRegularFile })
                .map { dir.relativize(it) }
                .toList()
    }

    private fun compareFiles(expectedDir: Path, actualDir: Path, files: List<Path>) {
        files.stream()
                .forEach { assertThat(actualDir.resolve(it)).hasSameTextualContentAs(expectedDir.resolve(it)) }
    }

    companion object {
        @JvmStatic
        private val UPDATE_EXPECTED_TEST_OUTPUTS = System.getenv("UPDATE_EXPECTED_TEST_OUTPUTS") == "true"
    }
}