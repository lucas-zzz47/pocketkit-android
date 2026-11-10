package com.pocketkit.app
import org.junit.Assert.assertEquals
import org.junit.Test
class TextToolsTest {
    @Test fun cleanNormalizesSpacesAndBlankLines() {
        assertEquals("你好 世界\n\n下一行", TextTools.clean("  你好   世界  \r\n\r\n\r\n 下一行 "))
    }
    @Test fun cleanHandlesEmptyText() { assertEquals("", TextTools.clean(" \n \n")) }
}
