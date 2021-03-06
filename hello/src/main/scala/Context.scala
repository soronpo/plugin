import counter.*

final case class Context(
  nameOpt: Option[String], position: Position, lateConstruction: Boolean,
  clsNameOpt: Option[String], clsPosition : Position, defaultDir : Int = 0
) extends MetaContext {
  def setMeta(
    nameOpt: Option[String], position: Position, lateConstruction: Boolean,
    clsNameOpt: Option[String], clsPosition: Position
  ) = copy(
    nameOpt = nameOpt, position = position, lateConstruction = lateConstruction,
    clsNameOpt = clsNameOpt, clsPosition = clsPosition
  ).asInstanceOf[this.type]
  def setName(name : String) : this.type = copy(nameOpt = Some(name)).asInstanceOf[this.type]
  def anonymize : this.type = copy(nameOpt = None).asInstanceOf[this.type]
  def <> (that : Int) : this.type = copy(defaultDir = that).asInstanceOf[this.type]

}
