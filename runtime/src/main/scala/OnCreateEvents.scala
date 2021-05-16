package counter

trait OnCreateEvents {
  onCreateStart
  def onCreateStart : Unit = {}
  def onCreateEnd : Unit = {}
  final def onCreate : this.type = 
    onCreateEnd
    this
}

