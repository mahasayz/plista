package com.solution

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Minutes, Span}
import org.scalatest.{Matchers, WordSpecLike}

abstract class UnitSpec extends TestKit(ActorSystem("tester"))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with DefaultTimeout
  with StopSystemAfterAll
  with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Minutes), interval = Span(500, Millis))
  val config = ConfigFactory.load("application-test")

}
