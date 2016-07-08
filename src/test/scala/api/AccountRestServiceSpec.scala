package api

import api.json.{AccountJsonProtocol, CustomerJsonProtocol}
import core._
import core.model.{Account, Customer}
import core.services.{CustomersDTO, TransferMoneyRequestDTO, TransferMoneyResponseDTO}
import org.specs2.mutable.{BeforeAfter, Specification}
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import squants.market.RUB
import util.AwaitHelper

import scala.concurrent.Promise
import scala.concurrent.duration._

class AccountRestServiceSpec extends Specification with Specs2RouteTest with HttpService
  with CoreActors with Core  with SprayJsonSupport with BeforeAfter with DefaultTimeout
  with CustomerJsonProtocol with AccountJsonProtocol with DefaultJsonProtocol with AwaitHelper {

  def actorRefFactory = system

  sequential

  override def before: Any = {}

  override def after: Any = {}

  val route = new AccountRestService().route

  implicit val routeTestTimeout = RouteTestTimeout(DurationInt(5).seconds)

  "AccountRestService" should {
    "return account by id" in {
      val accountToGet = Account.Values.head
      Get(s"/accounts/${accountToGet.id}") ~> route ~> check {
        status === StatusCodes.OK
        val resAccount = responseAs[Account]
        resAccount.id shouldEqual accountToGet.id
      }
    }

    //TODO: add more tests (when there is insufficient amount of money on account, when we ask to transfer from/to non existed account, etc).
    "transfer money between accounts" in {

      val fromAccount = Account.Values(1)
      val toAccount = Account.Values(2)

      Post(s"/accounts/transferMoney", TransferMoneyRequestDTO(fromAccount.id, toAccount.id, RUB(10))) ~> route ~> check {
        status == StatusCodes.OK
        val res = responseAs[TransferMoneyResponseDTO]
        res.succeeded shouldEqual true
      }
    }
  }
}
