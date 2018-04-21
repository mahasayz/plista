package com.solution.util

import com.typesafe.config.ConfigFactory

trait ConfigUtil {

  val config = ConfigFactory.load()

}
