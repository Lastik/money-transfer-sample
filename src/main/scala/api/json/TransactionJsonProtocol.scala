package api.json

import core.model.Transaction
import spray.json.DefaultJsonProtocol

trait TransactionJsonProtocol {
  this: DefaultJsonProtocol with AccountJsonProtocol with CommonJsonProtocol =>

  implicit val transactionJsonFormat = jsonFormat3(Transaction.apply)

}