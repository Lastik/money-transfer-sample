package api

import api.json.{AccountJsonProtocol, CommonJsonProtocol, CustomerJsonProtocol}
import common.{ErrorMessage, ServiceSuccess}
import core._
import core.model.CustomerId
import core.services.helpers.{AccountServiceHelper, CustomerServiceHelper}
import core.services.{AccountsDTO, CustomerDTO, CustomersDTO}
import org.specs2.mutable.{BeforeAfter, Specification}
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest
import util.AwaitHelper

import scala.concurrent.Promise
import scala.concurrent.duration._

class CustomerRestServiceSpec extends Specification with Specs2RouteTest with HttpService
   with Core with CoreActors  with SprayJsonSupport with BeforeAfter with DefaultTimeout
  with DefaultJsonProtocol with CommonJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol with AwaitHelper
  with AccountServiceHelper with CustomerServiceHelper {

  def actorRefFactory = system

  sequential

  override def before: Any = {}

  override def after: Any = {}

  val route = new CustomerRestService().route

  implicit val routeTestTimeout = RouteTestTimeout(DurationInt(5).seconds)

  val customerIdPromise: Promise[CustomerId] = Promise()

  def getCreatedCustomerId = customerIdPromise.future.awaitResult

  "CustomerRestService" should {

    "create customer" in {
      val customer = CustomerDTO("Stan Smith")

      Post(s"/customers", customer) ~> route ~> check {
        status === StatusCodes.OK
        val customerId = responseAs[CustomerId]
        customerIdPromise.success(customerId)
        success
      }
    }

    "return list of customers" in {
      Get(s"/customers") ~> route ~> check {
        status === StatusCodes.OK
        val customersDTO = responseAs[CustomersDTO]
        customersDTO.data.nonEmpty shouldEqual true
        customersDTO.data.map(_.id).contains(getCreatedCustomerId) shouldEqual true
      }
    }


    "return list of accounts for customer" in {

      val customerId = createCustomer("Some Customer")
      val accountForCustomer = createAccount(customerId)

      Get(s"/customers/$customerId/accounts") ~> route ~> check {
        status == StatusCodes.OK

        val accountsResponse = responseAs[Either[ErrorMessage, ServiceSuccess[AccountsDTO]]]

        accountsResponse match{
          case Left(error) => failure(error.text)
          case Right(ServiceSuccess(accountsDTO)) => accountsDTO.accounts.length shouldEqual 1
        }

        success
      }
    }

    "fail to return list of accounts for non existed customer" in {

      val customerId = CustomerId()

      Get(s"/customers/$customerId/accounts") ~> route ~> check {
        status == StatusCodes.OK

        val accountsResponse = responseAs[Either[ErrorMessage, ServiceSuccess[AccountsDTO]]]

        accountsResponse match{
          case Left(error) => success
          case Right(ServiceSuccess(accountsDTO)) => failure
        }
      }
    }
  }
}