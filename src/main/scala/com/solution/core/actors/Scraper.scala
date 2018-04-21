package com.solution.core.actors

import java.net.URL

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.solution.core.actors.Crawler.{ScrapingError, ScrapingFinished}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object Scraper {
  def props = Props(new Scraper)
  def name = "scraper"

  def parse(url: URL): Try[Option[Content]] = {
    val link = url.toString

    lazy val urlValidator = new UrlValidator()

    val response = Try(
      Jsoup.connect(link)
      .ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .execute()
    )

    response.map(res => {
      res.contentType().startsWith("text/html") match {
        case true =>
          val doc = res.parse()
          val text = doc.body().text()
          val links: List[URL] = doc.getElementsByTag("a").asScala.map(e =>
            e.attr("href")
          ).filter(urlValidator.isValid(_))
            .map(new URL(_))
            .filter(_.getHost == url.getHost).toList
          Some(Content(text, links))
        case false => None
      }
    })
  }

  case class Content(text: String, links: List[URL])
  case class Scrap(url: URL)
  case object Cancel
}

class Scraper extends Actor with ActorLogging {
  import Scraper._

  def receive = {
    case Scrap(url) =>
      log.info(s"Came into scrap {}", url)
      parse(url) match {
        case Success(content) =>
          content.map{ c =>
            log.debug(s"Doc text : {}", c.text)
            sender() ! ScrapingFinished(url, c)
          }
        case Failure(e) =>
          sender() ! ScrapingError(s"${e.getClass.getCanonicalName} : ${e.getMessage}")
      }
    case Cancel =>
      sender() ! Crawler.Finished
      self ! PoisonPill
  }

}
