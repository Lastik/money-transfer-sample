package util

import scala.concurrent.{Await, Awaitable}

trait AwaitHelper {

  implicit class AwaitableHelper[T](awaitable: Awaitable[T]) {

    def awaitResult: T = {
      import scala.concurrent.duration._
      Await.result(awaitable, 60.minutes)
    }

  }

}

