import core.model.{Account, Customer}
import squants.market._

package object demo {

  object DemoCustomers {
    val StanSmith = Customer(name = "Stan Smith")
    val HomerSimpson = Customer(name = "Homer Simpson")

    val Values = List(StanSmith, HomerSimpson)
  }

  object DemoAccounts {

    import DemoCustomers._
    import Account._

    val StanSmithRublesAccount = Account(customerId = StanSmith.id, balance = RUB(10000))
    val StanSmithDollarAccount = Account(customerId = StanSmith.id, balance = USD(1000))

    val HomerSimpsonDollarAccount = Account(customerId = HomerSimpson.id, balance = USD(1000))

    val Values = List(StanSmithRublesAccount, StanSmithDollarAccount, HomerSimpsonDollarAccount)
  }

}