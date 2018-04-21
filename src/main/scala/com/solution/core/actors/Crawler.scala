package com.solution.core.actors

import java.net.URL

import akka.actor.{Actor, ActorLogging, PoisonPill, Props}
import com.solution.core.actors.Indexer.{Index, Query}
import com.solution.core.actors.Scraper.Content
import com.solution.util.ConfigUtil

import scala.collection.mutable
import scala.concurrent.duration._

object Crawler {
  def props = Props(new Crawler)
  def name = "crawler"

  case class Task(urls: List[URL])
  case class ScrapingFinished(url: URL, content: Content)
  case class ScrapingError(error: String)
  case object Start
  case object Finished
  case object Tick
}

class Crawler extends Actor
  with ActorLogging
  with ConfigUtil {
  import Crawler._
  import Scraper._
  import context._

  val tick = context.system.scheduler.schedule(0 millis, 1000 millis, self, Tick)
  val taskQueue = mutable.Map[URL, Boolean]()

  def createScraper: Unit = {
    context.actorOf(Scraper.props, Scraper.name)
    log.info("scraper created!")
  }

  lazy val indexer = context.actorOf(Indexer.props, Indexer.name)

  def receive = {
    case Start =>
      context.child(Scraper.name).fold(createScraper)(_ => log.debug("scraper already exists"))
    case Task(urls) =>
      log.info(s"Got links - {}", urls)
      urls.filterNot(url => taskQueue.keySet.map(u => s"${u.getHost}${u.getPath}") contains s"${url.getHost}${url.getPath}")
        .foreach(u => taskQueue += (u -> false))
    case Tick =>
      val toDoList = taskQueue.filter(_._2 == false)
      log.info(s"queue size - {}", toDoList.size)

      toDoList.toList match {
        case task :: rest =>
          taskQueue += (task._1 -> true)
          log.info(s"Received url - {}", task._1.getPath)
          context.child(Scraper.name).map{ scraper =>
            scraper ! Scrap(task._1)
          }
        case Nil =>
          log.info(s"Nothing left to process")
      }
    case Query(keyword) =>
      indexer.forward(Query(keyword))
    case ScrapingFinished(url, content) =>
      log.info(s"Scraping finished for {}", url)
      indexer ! Index(url, content.text)
      self ! Task(content.links)
    case ScrapingError(e) =>
      log.info("Scraping failed with error: {}", e)
    case Finished =>
      self ! PoisonPill
      system.terminate()
  }

}
