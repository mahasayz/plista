package com.solution.core.api

import java.net.URL

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.solution.core.actors.Crawler
import com.solution.core.actors.Indexer.{Query, Response}
import org.apache.commons.validator.routines.UrlValidator

import scala.concurrent.{ExecutionContext, Future}

class RestApi(system: ActorSystem, timeout: Timeout) extends RestRoutes {

  implicit val requestTimeout = timeout
  implicit val ec = system.dispatcher

  def createCrawler = system.actorOf(Crawler.props, Crawler.name)

}

trait RestRoutes extends CrawlerApi
  with CrawlerMarshalling {
  import akka.http.scaladsl.model.StatusCodes._

  def routes: Route =
    pathPrefix("crawler") {
      post {
        path("crawl") {
          entity(as[TaskSubmission]) { task =>
            if (urlValidator.isValid(task.url)) {
              onComplete(submitUrl(task.url)) { done =>
                complete("task submitted!")
              }
            } else {
              val e = Error("Invalid URL")
              complete(BadRequest, e)
            }
          }
        }
      } ~ post {
        path("query") {
          entity(as[QuerySubmission]) { query =>
            onSuccess(queryText(query.keyword)) { response =>
              complete(response)
            }
          }
        }
      } ~ get {
        pathPrefix("start") {
          onComplete(startCrawler) { done =>
            complete("crawler started")
          }
        }
      }
    }

}

trait CrawlerApi {
  import com.solution.core.actors.Crawler._
  import akka.pattern.ask

  implicit def ec: ExecutionContext
  implicit def requestTimeout: Timeout

  def createCrawler(): ActorRef
  lazy val crawler = createCrawler()
  val urlValidator = new UrlValidator()

  def submitUrl(url: String): Future[Done] = {
    crawler ! Task(List(new URL(url)))
    Future { Done }
  }

  def queryText(keyword: String) = {
    crawler.ask(Query(keyword)).mapTo[Response]
  }

  def startCrawler: Future[Done] = {
    crawler ! Start
    Future { Done }
  }
}

