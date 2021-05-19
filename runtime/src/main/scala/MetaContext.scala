package counter

final case class Position(file : String, line : Int, column : Int) {
  def > (that : Position) : Boolean = {
    assert(file == that.file, "Can only compare positions within the same file")
    line > that.line || (line == that.line && column > that.column)
  }
  def >= (that : Position) : Boolean = (this == that) || (this > that)
  def < (that : Position) : Boolean = !(this >= that)
  def <= (that : Position) : Boolean = !(this > that)
  override def toString: String = s"$file:$line:$column"
}
object Position:
  val unknown = Position("", 0, 0)

trait MetaContext {
  def setMeta(nameOpt : Option[String], position : Position, lateConstruction : Boolean) : this.type 
  def setName(nameOpt : Option[String]) : this.type
  val nameOpt : Option[String] 
  val position : Position 
  val lateConstruction : Boolean 
  final val isAnonymous : Boolean = nameOpt.isEmpty
  final val name : String = nameOpt.getOrElse(s"anon${this.hashCode.toHexString}")
}
