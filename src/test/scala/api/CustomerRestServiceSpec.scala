package integration.api

import api.json.{AccountJsonProtocol, CustomerJsonProtocol}
import core._
import core.model.Customer
import core.services.CustomersDTO
import spray.testkit.Specs2RouteTest
import spray.routing.{Directives, HttpService}
import org.specs2.mutable.{BeforeAfter, Specification}
import spray.http.{HttpResponse, StatusCodes}
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import util.AwaitHelper

import scala.concurrent.Promise
import scala.concurrent.duration._

/*class CustomerRestServiceSpec extends Specification with Specs2RouteTest with HttpService
  with CoreActors with Core  with SprayJsonSupport with BeforeAfter with DefaultTimeout
  with CustomerJsonProtocol with AccountJsonProtocol with DefaultJsonProtocol with AwaitHelper {

  def actorRefFactory = system

  sequential

  override def before: Any = {}

  override def after: Any = {}

  val route = new CustomerRestService().route

  implicit val routeTestTimeout = RouteTestTimeout(DurationInt(5).seconds)

  val customersPromise: Promise[List[Customer]] = Promise()

  "CustomerRestService" should {
    "return list of customers" in {
      Get(s"/customers") ~> route ~> check {
        status === StatusCodes.OK
        val customersDTO = responseAs[CustomersDTO]
        customersPromise.success(customersDTO.data)
        customersDTO.data.nonEmpty shouldEqual true
      }
    }

    "return list of accounts for each cusromer" in {
      val customers = customersPromise.future.awaitResult
      customers.forall(customer => {
        Get(s"/customers/${customer.id}/accounts") ~> route ~> check {
          status == StatusCodes.OK
        }
      }) shouldEqual true
    }
  }
}
*/