package com.solution

import java.net.URL

import com.solution.core.actors.Crawler.{ScrapingError, ScrapingFinished}
import com.solution.core.actors.Scraper

class ScraperSpec extends UnitSpec {

  "Scraper" must {
    "scrape link content properly" in {
      import com.solution.core.actors.Scraper._

      val scraper = system.actorOf(Scraper.props)
      scraper ! Scrap(new URL("http://www.example.com/"))

      expectMsgPF() {
        case ScrapingFinished(url, c) =>
          c.text should not be ('empty)
          c.text should include("Example Domain")

          // there is one link on this URL but it's of a different domain, so ignored
          c.links.size shouldBe 0
      }
    }

    "get error on inaccessible url" in {
      import com.solution.core.actors.Scraper._

      val scraper = system.actorOf(Scraper.props)
      scraper ! Scrap(new URL("http://locahost"))

      expectMsgPF() {
        case ScrapingError(e) =>
          println(e)
      }
    }
  }

}
