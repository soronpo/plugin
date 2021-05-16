import counter.*

object Hello { 
  class Bar(using Context) extends OnCreateEvents:
    def + (that : Bar)(using Context) : Bar = new Plus(this, that)
  class Plus(lhs : Bar, rhs : Bar)(using Context) extends Bar

  class Foo(arg1 : Int, arg2 : Int)(using Context) extends Bar
  object Internal:
    class Foo(arg1 : Int, arg2 : Int)(using Context) extends Bar

  def newBar(using Context) : Bar = new Bar
  class Top(using Context):
    object FooObj extends Foo(1, 2)             
    case object FooCaseObj extends Foo(1, 2)             
    val fooCls = new Foo(1, 2) 
    val fooCls2 = new Foo(1, 2) {
      val i = 1
    }
    val internalFoo = new Internal.Foo(1, 2)       
    val nb1 = newBar
    val nb2 = newBar   
    val plus = nb1 + nb2
    val plus3 = nb1 + nb2 + nb1 
    val barPlus = new Bar + nb1
    val fooClsBlock =  
      new Foo(1, 2)  
      println("fooBlock")      
      new Foo(11, 12)  
    case class FooCC(arg1 : Int, arg2 : Int) extends Foo(arg1, arg2)
    
    val fooCC = FooCC(1, 2)
    val fooNewCC = new FooCC(1, 2)
  
  val top = new Top(using Context(Some("top"), Position.unknown, false))    

  def main(args: Array[String]): Unit = {
    foo(5)
  }  

  def foo(x: Int): Unit =
    if (x > 0)  
      foo(x - 1)  
    else bar()             

  def bar(): Unit = println("hello")
}
