import core.model.{Account, Customer, CustomerId}
import squants.market._

package object demo {

  val StanSmithCustomerId = CustomerId()
  val HomerSimpsonCustomerId = CustomerId()

  object DemoAccounts {

    import Account._

    val StanSmithRublesAccount = Account(customerId = StanSmithCustomerId, balance = RUB(10000))
    val StanSmithDollarAccount = Account(customerId = StanSmithCustomerId, balance = USD(1000))

    val HomerSimpsonDollarAccount = Account(customerId = HomerSimpsonCustomerId, balance = USD(1000))

    val Values = List(StanSmithRublesAccount, StanSmithDollarAccount, HomerSimpsonDollarAccount)
  }

  object DemoCustomers {

    import DemoAccounts._

    val StanSmith = Customer(id = StanSmithCustomerId, name = "Stan Smith",
      accounts = List(StanSmithRublesAccount.id, StanSmithDollarAccount.id))
    val HomerSimpson = Customer(id = HomerSimpsonCustomerId, name = "Homer Simpson",
      accounts = List(HomerSimpsonDollarAccount.id))

    val Values = List(StanSmith, HomerSimpson)
  }

}