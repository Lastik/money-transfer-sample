package common

import core.model.ModelEntity

trait ModelEntityFixture[EntityType <: ModelEntity] {
  def entity: EntityType
}