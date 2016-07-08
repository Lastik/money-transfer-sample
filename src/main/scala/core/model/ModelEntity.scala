package core.model

trait ModelEntityKey {
  def id: String

  override def toString = id
}

trait ModelEntity {
  def id: ModelEntityKey
}
