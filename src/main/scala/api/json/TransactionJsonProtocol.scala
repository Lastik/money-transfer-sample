package api.json

import spray.json._
import core.model.{AccountId, Transaction}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsValue, RootJsonFormat}
import squants.Money

trait TransactionJsonProtocol {
  this: DefaultJsonProtocol with AccountJsonProtocol with CommonJsonProtocol =>

  implicit val transactionJsonFormat = jsonFormat3(Transaction.apply)

}