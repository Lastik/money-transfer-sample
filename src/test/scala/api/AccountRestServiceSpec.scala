package api

import api.json.{AccountJsonProtocol, CommonJsonProtocol, CustomerJsonProtocol}
import common.{ErrorMessage, ServiceSuccess}
import core._
import core.model.{Account, AccountId, CustomerId}
import core.services.AccountDTO
import core.services.helpers.{AccountServiceHelper, CustomerServiceHelper}
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
  with Core with CoreActors  with SprayJsonSupport with BeforeAfter with DefaultTimeout
  with DefaultJsonProtocol with CommonJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol with AwaitHelper
  with AccountServiceHelper with CustomerServiceHelper {

  def actorRefFactory = system

  sequential

  val customerIdPromise: Promise[CustomerId] = Promise()

  def getCreatedCustomerId = customerIdPromise.future.awaitResult

  val accountIdPromise: Promise[AccountId] = Promise()

  def getCreatedAccountId = accountIdPromise.future.awaitResult

  override def before: Any = {}

  override def after: Any = {}

  val route = new AccountRestService().route

  implicit val routeTestTimeout = RouteTestTimeout(DurationInt(5).seconds)

  "AccountRestService" should {

    "create account for customer" in {

      val customerId = createCustomer("Some Customer")
      customerIdPromise.success(customerId)

      Post(s"/accounts", AccountDTO(customerId, RUB(10000))) ~> route ~> check {
        status === StatusCodes.OK

        val createAccountResponse = responseAs[Either[ErrorMessage, ServiceSuccess[AccountId]]]

        createAccountResponse match {
          case Left(errorMessage) => failure(errorMessage.text)
          case Right(ServiceSuccess(accountId)) =>
            accountIdPromise.success(accountId)
            success
        }
      }
    }

    "get account by id" in {

      val accountId = getCreatedAccountId

      Get(s"/accounts/$accountId") ~> route ~> check {
        status === StatusCodes.OK

        val getAccountResponse = responseAs[Either[ErrorMessage, ServiceSuccess[Account]]]

        getAccountResponse match {
          case Left(errorMessage) => failure(errorMessage.text)
          case Right(ServiceSuccess(account)) =>
            success
        }
      }
    }
  }
}