package api

import api.json.{AccountJsonProtocol, CommonJsonProtocol, CustomerJsonProtocol, TransactionJsonProtocol}
import common.{ErrorMessage, ServiceSuccess}
import core._
import core.model.{AccountId, CustomerId, Transaction}
import core.services.helpers.{AccountServiceHelper, CustomerServiceHelper}
import core.services.{AccountsDTO, CustomerDTO, CustomersDTO}
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

class TransactionRestServiceSpec extends Specification with Specs2RouteTest with HttpService
   with Core with CoreActors  with SprayJsonSupport with BeforeAfter with DefaultTimeout
  with DefaultJsonProtocol with CommonJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol
  with TransactionJsonProtocol with AwaitHelper with AccountServiceHelper with CustomerServiceHelper {

  def actorRefFactory = system

  sequential

  override def before: Any = {}

  override def after: Any = {}

  val route = new TransactionRestService().route

  implicit val routeTestTimeout = RouteTestTimeout(DurationInt(5).seconds)

  "TransactionRestService" should {

    "perform money transfer transaction" in {

      val stanSmithCustomerId = createCustomer("Stan Smith")
      val homerSimpsonCustomerId = createCustomer("Homer Simpson")

      val stanSmithAccountId = createAccount(stanSmithCustomerId, RUB(10000)).right.get.result
      val homerSimpsonAccountId = createAccount(homerSimpsonCustomerId, RUB(5000)).right.get.result

      val transaction = Transaction(from = stanSmithAccountId, to = homerSimpsonAccountId, amount = RUB(2000))

      Post("/transaction/process", transaction) ~> route ~> check {
        status === StatusCodes.OK

        val processTransactionResult = responseAs[Either[ErrorMessage, ServiceSuccess[String]]]

        processTransactionResult match {
          case Left(errorMessage) => failure(errorMessage.text)
          case Right(ServiceSuccess(_)) => success
        }
      }
    }

    "fail to perform money transfer transaction (non existing accounts)" in {

      val mrXAccountId = AccountId()
      val mrYAccountId = AccountId()

      val transaction = Transaction(from = mrXAccountId, to = mrYAccountId, amount = RUB(2000))

      Post("/transaction/process", transaction) ~> route ~> check {
        status === StatusCodes.OK

        val processTransactionResult = responseAs[Either[ErrorMessage, ServiceSuccess[String]]]

        processTransactionResult match {
          case Left(errorMessage) => success
          case Right(_) => failure("We tried to perform transaction for non existed account but somehow succeeded")
        }
      }
    }
  }
}