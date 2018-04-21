package com.solution

import java.net.URL

import com.solution.core.actors.Indexer
import com.solution.core.actors.Indexer.{Index, Query, Response}

class IndexerSpec extends UnitSpec {

  val indexer = system.actorOf(Indexer.props, "indexerTest")
  indexer ! Index(new URL("http://www.google.com"), "some google content")
  indexer ! Index(new URL("http://www.example.com"), "some example content")

  "Indexer" must {

    "return proper query result" in {
      indexer ! Query("google")

      expectMsgPF() {
        case Response(r) =>
          r.size shouldBe 1
          r.head.url should include("google.com")
      }
    }

  }

}
