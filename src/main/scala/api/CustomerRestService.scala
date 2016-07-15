package api

import akka.actor.ActorSystem
import akka.pattern.ask
import api.json.{AccountJsonProtocol, CommonJsonProtocol, CustomerJsonProtocol}
import common.{ErrorMessage, ServiceSuccess}
import core.model.{Account, CustomerId}
import core.services._
import spray.json.DefaultJsonProtocol
import spray.routing.Directives
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext

class CustomerRestService(implicit executionContext: ExecutionContext, implicit val system: ActorSystem) extends RestServiceBase
  with Directives with DefaultJsonProtocol with CommonJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol {

  val customerService = lookup(CustomerService.Id)
  val accountService = lookup(AccountService.Id)

  val route =
    pathPrefix("customers") {
      pathEnd {
        get {
          onComplete(customerService.ask(CustomerService.GetAllCustomers()).mapTo[CustomersDTO]) {
            case scala.util.Success(res) => complete(res)
            case scala.util.Failure(ex) => failWith(ex)
          }
        } ~
          post(entity(as[CustomerDTO]) { customerDTO =>
            onComplete(customerService.ask(CustomerService.CreateCustomer(customerDTO)).mapTo[CustomerId]) {
              case scala.util.Success(res) => complete(res)
              case scala.util.Failure(ex) => failWith(ex)
            }
          })
      } ~
        pathPrefix(JavaUUID / "accounts") {
          customerId =>
            pathEnd {
              get {
                onComplete(accountService.ask(AccountService.FindCustomerAccounts(customerId)).mapTo[Either[ErrorMessage, ServiceSuccess[AccountsDTO]]]) {
                  case scala.util.Success(res) => complete(res)
                  case scala.util.Failure(ex) => failWith(ex)
                }
              }
            }
        }
    }
}
