package api

import akka.actor.ActorSystem
import akka.pattern.ask
import api.json.{AccountJsonProtocol, CommonJsonProtocol, CustomerJsonProtocol}
import common.{ErrorMessage, ServiceSuccess}
import core.model.{Account, AccountId}
import core.services._
import spray.json.DefaultJsonProtocol
import spray.routing.Directives
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext

class AccountRestService(implicit executionContext: ExecutionContext, implicit val system: ActorSystem) extends RestServiceBase
  with Directives with DefaultJsonProtocol with CommonJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol {

  val customerService = lookup(CustomerService.Id)
  val accountService = lookup(AccountService.Id)

  val route =
    pathPrefix("accounts") {
      path(JavaUUID) {
        accountId =>
          get {
            onComplete(accountService.ask(AccountService.FindAccountById(accountId)).mapTo[Either[ErrorMessage, ServiceSuccess[Account]]]) {
              case scala.util.Success(res) => complete(res)
              case scala.util.Failure(ex) => failWith(ex)
            }
          }
      } ~
        pathEnd {
          post {
            entity(as[AccountDTO]) { accountDTO =>
              onComplete(accountService.ask(AccountService.CreateAccount(accountDTO)).mapTo[Either[ErrorMessage, ServiceSuccess[AccountId]]]) {
                case scala.util.Success(res) => complete(res)
                case scala.util.Failure(ex) => failWith(ex)
              }
            }
          }
        }
    }
}