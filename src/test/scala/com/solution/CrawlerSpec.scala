package com.solution

import java.net.URL

import akka.testkit.TestActorRef
import com.solution.core.actors.Crawler
import com.solution.core.actors.Crawler.Task

class CrawlerSpec extends UnitSpec {

  "Crawler" must {
    "put new task to queue" in {
      val crawlerActor = TestActorRef[Crawler]
      val link = new URL("http://www.google.com")
      crawlerActor ! Task(List(link))

      crawlerActor.underlyingActor.taskQueue.keySet.map(_.toString) should contain(link.toString)
    }

    "multi-url crawl" in {
      val crawlerActor = TestActorRef[Crawler]
      val link1 = new URL("http://www.google.com")
      val link2 = new URL("http://www.example.com")
      crawlerActor ! Task(List(link1, link2))

      crawlerActor.underlyingActor.taskQueue.keySet.size shouldBe 2
    }

    "previously processed urls should not be re-processed if submitted" in {
      val crawlerActor = TestActorRef[Crawler]
      val link = new URL("http://www.google.com")
      crawlerActor ! Task(List(link))
      crawlerActor ! Task(List(link))

      crawlerActor.underlyingActor.taskQueue.keySet.size shouldBe 1
    }
  }

}
