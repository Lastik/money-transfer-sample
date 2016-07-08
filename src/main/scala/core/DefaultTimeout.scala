package core

import akka.util.Timeout

import scala.concurrent.duration._

trait DefaultTimeout {

  implicit val timeout = Timeout(1800, SECONDS)
}

