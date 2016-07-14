package util

import akka.testkit.{DefaultTimeout, ImplicitSender, TestKitBase}
import core.{Core, CoreActors}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

trait ActorSpecBase
  extends ImplicitSender
  with Core
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with AwaitHelper
  with DefaultTimeout {
  self: TestKitBase =>
}
