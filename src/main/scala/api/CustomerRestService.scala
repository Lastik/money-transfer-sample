package api

import akka.actor.ActorSystem
import akka.pattern.ask
import api.json.{AccountJsonProtocol, CustomerJsonProtocol}
import core.services.{AccountService, AccountsDTO, CustomerService, CustomersDTO}
import spray.json.DefaultJsonProtocol
import spray.routing.Directives
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext

class CustomerRestService(implicit executionContext: ExecutionContext, implicit val system: ActorSystem) extends RestServiceBase
  with Directives with DefaultJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol  {

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
        }
      } ~
      pathPrefix(JavaUUID / "accounts") {
        customerId =>
          pathEnd {
            onComplete(accountService.ask(AccountService.GetCustomerAccounts(customerId)).mapTo[AccountsDTO]) {
              case scala.util.Success(res) => complete(res)
              case scala.util.Failure(ex) => failWith(ex)
            }
          }
      }
    }
}
