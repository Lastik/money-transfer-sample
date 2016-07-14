case class X(i:Int) {
  def add(v: Int)(implicit x:X)=x.i+i + v
}
object X {
  implicit val xx = new X(3)
}
// implicit is obtained from companion object of X

new X(3).add(2)