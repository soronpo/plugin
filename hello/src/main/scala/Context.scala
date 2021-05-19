import counter.*

case class Context(nameOpt: Option[String], position: Position, lateConstruction: Boolean) extends MetaContext {
  def setMeta(nameOpt: Option[String], position: Position, lateConstruction: Boolean) =
    copy(nameOpt = nameOpt, position = position, lateConstruction = lateConstruction).asInstanceOf[this.type]
  def setName(nameOpt: Option[String]) =
    copy(nameOpt = nameOpt).asInstanceOf[this.type]

}
