package common

trait ErrorMessage {
  def text: String

  private[this] case class CompositeErrorMessage(text: String) extends ErrorMessage

  def concat(other: ErrorMessage): ErrorMessage = CompositeErrorMessage(text = s"$text\n${other.text}")
}

