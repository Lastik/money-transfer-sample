package api

import akka.actor.ActorSystem
import akka.pattern.ask
import api.json.{AccountJsonProtocol, CustomerJsonProtocol, TransactionJsonProtocol}
import core.model.Transaction
import core.services._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import spray.routing.Directives

import scala.concurrent.ExecutionContext

class TransactionRestService(implicit executionContext: ExecutionContext, implicit val system: ActorSystem) extends RestServiceBase
  with Directives with DefaultJsonProtocol with TransactionJsonProtocol with AccountJsonProtocol with CustomerJsonProtocol {

  val transactionService = lookup(TransactionService.Id)

  val route =
    pathPrefix("transaction" / "process") {
      pathEnd {
        post(entity(as[Transaction]) { transaction =>
          onComplete(transactionService.ask(TransactionService.ProcessTransaction(transaction)).mapTo[ProcessTransactionResponseDTO]) {
            case scala.util.Success(res) => complete(res)
            case scala.util.Failure(ex) => failWith(ex)
          }
        })
      }
    }
}