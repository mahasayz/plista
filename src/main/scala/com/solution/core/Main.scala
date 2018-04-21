package com.solution.core

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.solution.core.api.RestApi
import com.solution.util.ConfigUtil
import com.typesafe.config.Config
import scala.concurrent.duration._

import scala.io.StdIn
import scala.util.Try

object Main extends App with RequestTimeout with ConfigUtil {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val api = new RestApi(system, 5.seconds).routes

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val bindingFuture = Http().bindAndHandle(api, host, port)
  println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}

trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(config: Config): Timeout = {
    val t = Try(config.getString("spray.can.server.request-timeout")).toOption.getOrElse("5000")  //uses default request-timeout of Akka-Http server conf
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}