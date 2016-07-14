package core.model

import squants.Money

case class Transaction(from: AccountId, to: AccountId, amount: Money)