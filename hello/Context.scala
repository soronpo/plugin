import counter.*
case class Context(nameOpt : Option[String], position : Position, lateConstruction : Boolean) extends MetaContext {
  def setMeta(nameOpt : Option[String], position : Position, lateConstruction : Boolean) = 
    copy(nameOpt = this.nameOpt, position = this.position, lateConstruction = this.lateConstruction).asInstanceOf[this.type]
}

