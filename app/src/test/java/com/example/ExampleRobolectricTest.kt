package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    val navTasks = context.getString(R.string.nav_tasks)
    assertEquals("TileTrack", appName)
    assertEquals("Tasks", navTasks)
  }

  @Test
  fun `editor catalogs remain evenly divisible for symmetric grids`() {
    assertEquals(0, com.example.ui.theme.HabitColorPalette.size % 6)
    assertEquals(0, com.example.ui.components.HabitIconCatalog.options.size % 6)
    assertEquals(0, com.example.domain.HabitCategoryDefaults.presets.size % 3)
  }
}
