import counter.*

object Hello { 
  class Bar extends OnCreateEvents:
    def + (that : Bar) : Bar = new Bar
  val nb1 = new Bar
  val barPlus = new Bar + nb1              
}
