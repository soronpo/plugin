package counter

import dotty.tools.dotc._

import plugins._

import core._
import Contexts._
import Symbols._
import Flags._
import SymDenotations._

import Decorators._
import ast.Trees._
import ast.tpd
import StdNames.nme
import Names._
import Constants.Constant
import Types._
import scala.language.implicitConversions
import collection.mutable
import annotation.tailrec

class MetaContextPhase(setting: Setting) extends PluginPhase {
  import tpd._

  val phaseName = "MetaContext"

  override val runsAfter = Set(transform.Pickler.name)
  override val runsBefore = Set(transform.FirstTransform.name)
  def PosStr(tree : Tree)(using Context) : String = 
    val pos = tree.sourcePos.startPos
    s"${pos.source.path}:${pos.line}:${pos.column}"

  private object ContextArg:
    def unapply(tree : Tree)(using Context) : Option[Tree] = 
      tree match
        case Apply(tree, args) => args.collectFirst {
          case a if a.tpe.typeSymbol.inherits("counter.MetaContext") => a
        }.orElse(unapply(tree))
        case _ => None

  val nameMap = mutable.Map.empty[Tree, String]

  override def transformApply(tree: Apply)(using Context): Tree = 
    if (!tree.tpe.isContextualMethod) 
      tree match 
        case ContextArg(argTree) =>
          // println(s"method ${tree.symbol.enclosingMethod}")
          // println(PosStr(tree))
          if (nameMap.get(tree).isEmpty)
            report.warning(nameMap.get(tree).toString, tree.srcPos)
          tree
        case _ => tree
    else tree

  val localPattern = "\\<local (.*)\\$\\>".r
  override def prepareForTemplate(tree: Template)(using Context): Context = 
    val nameStr = tree.symbol.name.toString 
    tree.symbol.name.toString match
      case localPattern(name) => 
        tree.parents.foreach(p => nameMap += (p -> name))
      case _ =>
    ctx
  
  override def prepareForValDef(tree: ValDef)(using Context): Context = 
    val nameStr = tree.symbol.name.toString
    if (nameStr == "pp")
      println(tree.rhs.show)
      println(tree.rhs)
      println(tree.tpe.isContextualMethod)

    @tailrec def nameit(tree : Tree) : Unit = 
      tree match 
        case apply : Apply => nameMap += (apply -> nameStr)
        case Block(TypeDef(tpn, cls : Template) :: _, expr) if tpn.toString == "$anon"=> 
          cls.parents.foreach(p => nameMap += (p -> nameStr))
        case block : Block => nameit(block.expr)
        case _ => 
    nameit(tree.rhs)
    ctx

  override def transformUnit(tree: Tree)(using Context): Tree = 
    report.error("moshe")
    tree


}

