package example.server

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Route
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("browser")
class RecordBrowserTest {
    @Test
    fun exportedPagesSupportSearchErrorsAndDirectNavigation() {
        val site = Path.of(System.getProperty("example.site"))
        check(Files.isRegularFile(site.resolve("index.html"))) {
            "Export the static site before browserTest"
        }
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val requested = site.resolve(exchange.requestURI.path.removePrefix("/")).normalize()
            val file =
                when {
                    exchange.requestURI.path.startsWith("/api/") -> null
                    !requested.startsWith(site) -> null
                    Files.isRegularFile(requested) -> requested
                    Files.isRegularFile(Path.of("$requested.html")) -> Path.of("$requested.html")
                    "." in requested.fileName.toString() -> null
                    else -> site.resolve("index.html")
                }
            if (file == null) {
                exchange.sendResponseHeaders(404, -1)
            } else {
                val type =
                    when {
                        file.toString().endsWith(".js") -> "application/javascript"
                        file.toString().endsWith(".css") -> "text/css"
                        else -> "text/html"
                    }
                exchange.responseHeaders.add("Content-Type", type)
                val bytes = Files.readAllBytes(file)
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            exchange.close()
        }
        server.start()
        try {
            val origin = "http://127.0.0.1:${server.address.port}"
            Playwright.create().use { playwright ->
                playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true)).use {
                    browser ->
                    browser.newContext(Browser.NewContextOptions().setViewportSize(390, 844)).use {
                        context ->
                        val page = context.newPage()
                        var fail = false
                        page.route("**/api/v1/records?*") { route ->
                            route.fulfill(
                                Route.FulfillOptions()
                                    .setStatus(if (fail) 401 else 200)
                                    .setContentType("application/json")
                                    .setBody(
                                        if (fail) "[]"
                                        else
                                            """[{"id":1,"externalReference":"REC-1","displayName":"Ada","category":"Standard"}]"""
                                    )
                            )
                        }
                        page.navigate(origin)
                        val input = page.getByLabel("Search records")
                        input.fill("Ada & Co")
                        val submitted =
                            page.waitForResponse("**/api/v1/records?query=Ada+%26+Co&limit=100") {
                                input.press("Enter")
                            }
                        assertThat(
                                page.getByRole(
                                    AriaRole.LINK,
                                    Page.GetByRoleOptions().setName("Ada"),
                                )
                            )
                            .isVisible()
                        assertTrue(URI(submitted.url()).rawQuery.contains("query=Ada+%26+Co"))
                        assertEquals(200, submitted.status())
                        assertEquals(
                            true,
                            page.evaluate(
                                "document.documentElement.scrollWidth <= window.innerWidth"
                            ),
                        )
                        input.press("Tab")
                        assertThat(
                                page.getByRole(
                                    AriaRole.BUTTON,
                                    Page.GetByRoleOptions().setName("Search").setExact(true),
                                )
                            )
                            .isFocused()
                        page.keyboard().press("Shift+Tab")
                        assertThat(input).isFocused()
                        fail = true
                        page
                            .getByRole(
                                AriaRole.BUTTON,
                                Page.GetByRoleOptions().setName("Search").setExact(true),
                            )
                            .click()
                        assertThat(page.getByRole(AriaRole.ALERT))
                            .hasText("Could not load records. Try again.")
                        assertThat(
                                page.getByRole(
                                    AriaRole.LINK,
                                    Page.GetByRoleOptions().setName("Ada"),
                                )
                            )
                            .isVisible()
                        fail = false
                        input.press("Enter")
                        assertThat(page.getByRole(AriaRole.ALERT)).hasCount(0)
                        page
                            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Ada"))
                            .click()
                        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Record 1")
                        page.reload()
                        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Record 1")
                        page.goBack()
                        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Record browser")
                        page.goForward()
                        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Record 1")
                        page.navigate("$origin/records/invalid")
                        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Invalid record ID")
                        assertEquals(404, page.request().get("$origin/api/missing").status())
                        assertEquals(404, page.request().get("$origin/missing.js").status())
                    }
                }
            }
        } finally {
            server.stop(0)
        }
    }
}
