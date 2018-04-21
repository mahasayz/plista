package com.solution.core.actors

import java.net.URL

import akka.actor.{Actor, ActorLogging, Props}
import com.outr.lucene4s.Lucene

object Indexer {
  def props = Props(new Indexer)
  def name = "indexer"

  case class Index(url: URL, text: String)
  case class Query(keyword: String)
  case class QueryResult(fragment: String, url: String)
  case class Response(response: Vector[QueryResult])
}

class Indexer extends Actor with ActorLogging {
  import Indexer._

  private val lucene = new Lucene(defaultFullTextSearchable = true)
  private val url = lucene.create.field[String]("url")
  private val content = lucene.create.field[String]("content")

  def receive = {
    case Index(link, text) =>
      log.info(s"came to index - url {}", s"${link.getHost}${link.getPath}")
      lucene.doc().fields(
        url(s"${link.getHost}${link.getPath}"),
        content(text)
      ).index()
    case Query(keyword) =>
      val result = lucene.query().filter(content(keyword))
        .highlight().search()

      val response: Vector[QueryResult] = result.results.map(searchResult => {
        val highlighting = searchResult.highlighting(content).head
        println(s"Fragment: ${highlighting.fragment}, URL: ${searchResult(url)}")
        QueryResult(highlighting.fragment, searchResult(url))
      })

      sender() ! Response(response)
  }

}
