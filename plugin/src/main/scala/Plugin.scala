package counter

import dotty.tools.dotc.plugins._

class Plugin extends StandardPlugin {
  val name: String = "counter"
  override val description: String = "Count method calls"

  def init(options: List[String]): List[PluginPhase] =
    (new OnCreateEventsPhase) :: Nil
}
