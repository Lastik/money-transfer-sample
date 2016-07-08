package api

import akka.actor.ActorSystem
import akka.pattern.ask
import api.json.{AccountJsonProtocol, CustomerJsonProtocol}
import core.model.Account
import core.services._
import spray.json.DefaultJsonProtocol
import spray.routing.Directives
import spray.httpx.SprayJsonSupport._

import scala.concurrent.ExecutionContext

class AccountRestService(implicit executionContext: ExecutionContext, implicit val system: ActorSystem) extends RestServiceBase
  with Directives with DefaultJsonProtocol with CustomerJsonProtocol with AccountJsonProtocol {

  val customerService = lookup(CustomerService.Id)
  val accountService = lookup(AccountService.Id)

  val route =
    pathPrefix("accounts") {
      path(JavaUUID) {
        accountId =>
          onComplete(accountService.ask(AccountService.GetAccountById(accountId)).mapTo[Account]) {
            case scala.util.Success(res) => complete(res)
            case scala.util.Failure(ex) => failWith(ex)
          }
      } ~
        path("transferMoney") {
          post {
            entity(as[TransferMoneyRequestDTO]) { transferMoneyRequestDTO =>
              onComplete(accountService.ask(AccountService.TransferMoney(transferMoneyRequestDTO)).mapTo[TransferMoneyResponseDTO]) {
                case scala.util.Success(res) => complete(res)
                case scala.util.Failure(ex) => failWith(ex)
              }
            }
          }
        }
    }
}