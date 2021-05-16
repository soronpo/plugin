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


class MetaContextPhase(setting: Setting) extends PluginPhase {
  import tpd._

  val phaseName = "MetaContext"

  override val runsAfter = Set("OnCreateEvents")
  override val runsBefore = Set(transform.FirstTransform.name)
  def PosStr(sym : Symbol)(using Context) : String = s"${sym.startPos.source.path}:${sym.startPos.line}:${sym.startPos.column}"

  // private object ContextArg:
  //   def unapply(tree : Tree)(using Context) : Option[Tree] = 
  //     val sym = tree.tpe.typeSymbol
  //     if (sym.isChildOf("counter.MetaContext"))
  //       val clsSym = sym.asClass
  //       // println(clsSym)
  //       Some(tree)
  //     else None

  // private object ContextApply:
  //   def unapply(tree : Apply)(using Context) : Option[Tree] = 
  //     val sym = tree.tpe.typeSymbol
  //     if (sym.isChildOf("counter.MetaContext"))
  //       val clsSym = sym.asClass
  //       // println(clsSym)
  //       Some(tree)
  //     else None

  // override def transformApply(tree: Apply)(using Context): Tree = 
  //   var changed : Boolean = false
  //   val args = tree.args.map {
  //     case ContextArg(argTree) => 
  //       changed = true
        
  //       println(s"sym ${tree.symbol} ${PosStr(tree.symbol)}")
  //       println(s"own ${tree.symbol.owner} ${PosStr(tree.symbol.owner)}")
  //       argTree
  //     case tree => tree
  //   }
  //   if (changed) Apply(tree.fun, args)
  //   else tree
  
  // override def transformValDef(tree: ValDef)(using Context): Tree = 
  //   tree.rhs match 
  //     case ContextApply()

}

