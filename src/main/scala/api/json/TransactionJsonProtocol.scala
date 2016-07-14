package api.json

import core.model.Transaction
import core.services.ProcessTransactionResponseDTO
import spray.json.DefaultJsonProtocol

trait TransactionJsonProtocol {
  this: DefaultJsonProtocol with AccountJsonProtocol =>

  implicit val transactionJsonFormat = jsonFormat3(Transaction.apply)

  implicit val processTransactionResponseDTOJsonFormat = jsonFormat2(ProcessTransactionResponseDTO.apply)

}