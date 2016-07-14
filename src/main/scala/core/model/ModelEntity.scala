package core.model

trait ModelEntityKey {
  def id: String

  override def toString = id
}

trait ModelEntity {

  type KeyType <: ModelEntityKey

  def id: KeyType
}
