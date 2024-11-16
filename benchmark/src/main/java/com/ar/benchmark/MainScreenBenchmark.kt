package com.ar.benchmark

import android.graphics.Point
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.Metric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import kotlinx.coroutines.delay
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
@OptIn (ExperimentalMetricApi:: class)
class MainScreenBenchmark: AbstractBenchmark(StartupMode.COLD) {
    @Test
    fun accelerateHeavyScreenCompilationFull() : Unit = benchmark(CompilationMode.Full())
    override val metrics: List<Metric>
        get() = listOf(
            FrameTimingMetric(),
            TraceSectionMetric("HomeScreen", TraceSectionMetric.Mode.Sum),
            TraceSectionMetric("overLayDraw", TraceSectionMetric.Mode.Sum),
            TraceSectionMetric("BrowserView", TraceSectionMetric.Mode.Sum),
            TraceSectionMetric("TabsScreen", TraceSectionMetric.Mode.Sum),
            TraceSectionMetric("ImagePlaceholder", TraceSectionMetric.Mode.Sum),
            TraceSectionMetric("TabItemTag", TraceSectionMetric.Mode.Sum)
        )

    override fun MacrobenchmarkScope.measureBlock() {
        pressHome()
        startTaskActivity("Home")
        device.waitForIdle()

        repeat(1){
            val tabButton = device.wait(Until.findObject(By.res("TabButtonBox")), 2000)
            tabButton?.click() ?: throw AssertionError("Tab button not found or null")
            device.waitForIdle()
            Thread.sleep(1000)

            val newTabButton = device.wait(Until.findObject(By.text("New Tab")), 2000)
            newTabButton?.click() ?: throw AssertionError("New Tab button not found or null")
            device.waitForIdle()
            Thread.sleep(1000)

        }

        val tabButton = device.wait(Until.findObject(By.res("TabButtonBox")), 2000)
        tabButton?.click() ?: throw AssertionError("Tab button not found or null")

        device.waitForIdle()
        Thread.sleep(1000)

        device.wait(Until.hasObject(By.res("Grid_of_tabs")), 5_000)

        val feed = device.findObject(By.res("Grid_of_tabs"))
        feed.setGestureMargin(device.displayWidth / 5)

        repeat(2) {
            feed.drag(Point(feed.visibleCenter.x, feed.visibleBounds.top))
            Thread.sleep(500)
        }
    }
}