package com.zachgrosser.wiki

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttributionTest {

    @Test
    fun `article addresses match Wikipedia's`() {
        assertEquals("en.wikipedia.org/wiki/Light_Phone_III", Attribution.articleUrl("Light Phone III"))
        assertEquals("en.wikipedia.org/wiki/C++", Attribution.articleUrl("C++"))
        assertEquals("en.wikipedia.org/wiki/What%3F_(film)", Attribution.articleUrl("What? (film)"))
        assertEquals("en.wikipedia.org/wiki/100%25_Love", Attribution.articleUrl("100% Love"))
    }

    @Test
    fun `the article credit names the source, license and changes`() {
        val credit = Attribution.forArticle("Light Phone")
        assertTrue("“Light Phone”" in credit)
        assertTrue("en.wikipedia.org/wiki/Light_Phone" in credit)
        assertTrue("CC BY-SA 4.0" in credit)
        assertTrue("creativecommons.org/licenses/by-sa/4.0" in credit)
        assertTrue("Changed for this screen" in credit)
    }
}
