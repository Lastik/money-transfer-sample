package api

import akka.actor.ActorSystem
import akka.pattern.ask
import api.json.{AccountJsonProtocol, CommonJsonProtocol, CustomerJsonProtocol, TransactionJsonProtocol}
import common.{ErrorMessage, ServiceSuccess}
import core.model.Transaction
import core.services._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import spray.routing.Directives

import scala.concurrent.ExecutionContext

class TransactionRestService(implicit executionContext: ExecutionContext, implicit val system: ActorSystem) extends RestServiceBase
  with Directives with DefaultJsonProtocol with CommonJsonProtocol with AccountJsonProtocol with CustomerJsonProtocol with TransactionJsonProtocol {

  val transactionService = lookup(TransactionService.Id)

  val route =
    pathPrefix("transaction" / "process") {
      pathEnd {
        post(entity(as[Transaction]) { transaction =>
          onComplete(transactionService.ask(TransactionService.ProcessTransaction(transaction)).mapTo[Either[ErrorMessage, ServiceSuccess[String]]]) {
            case scala.util.Success(res) => complete(res)
            case scala.util.Failure(ex) => failWith(ex)
          }
        })
      }
    }
}