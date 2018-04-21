package com.solution.core.api

import java.net.URL

import com.solution.core.actors.Indexer.{QueryResult, Response}
import com.solution.core.actors.Scraper.Content
import spray.json._

import scala.util.Try

case class TaskSubmission(url: String) {
  require(!url.isEmpty)
}

case class QuerySubmission(keyword: String) {
  require(!keyword.isEmpty)
}

case class Error(message: String)

trait CrawlerMarshalling extends DefaultJsonProtocol {

  implicit val taskSubmissionFormat = jsonFormat1(TaskSubmission)
  implicit val querySubmissionFormat = jsonFormat1(QuerySubmission)
  implicit val errorFormat = jsonFormat1(Error)

  implicit val urlJsonFormat = new JsonFormat[URL] {
    override def read(json: JsValue): URL = json match {
      case JsString(url) => Try(new URL(url)).getOrElse(deserializationError("Invalid URL format"))
      case _             => deserializationError("URL should be string")
    }

    override def write(obj: URL): JsValue = JsString(obj.toString)
  }
  implicit val contentFormat = jsonFormat2(Content)
  implicit val queryResultFormat = jsonFormat2(QueryResult)
  implicit val responseFormat = jsonFormat1(Response)

}