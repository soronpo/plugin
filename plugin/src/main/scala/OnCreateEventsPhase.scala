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
import Types._
import Constants.Constant

import annotation.tailrec
import scala.language.implicitConversions
import collection.mutable

class OnCreateEventsPhase(setting: Setting) extends CommonPhase {
  import tpd._

  val phaseName = "OnCreateEvents"

  override val runsAfter = Set("CustomIf")
  override val runsBefore = Set(transform.FirstTransform.name)
  
  val ignore = mutable.Set.empty[Tree]

  private object OnCreateEventsInstance:
    def apply(clsSym : ClassSymbol, tree : Tree)(using Context): Tree =
      tree.select(clsSym.requiredMethodRef("onCreate"))
        .withType(TermRef(tree.tpe, clsSym.requiredMethod("onCreate")))
    @tailrec def unapply(tree : Tree)(using Context) : Option[ClassSymbol] =
      tree match 
        case Apply(Select(New(id),_),_) => 
          val sym = id.symbol
          if (sym.isClass)
            val clsSym = sym.asClass
            if (clsSym.inherits("counter.OnCreateEvents")) Some(clsSym)
            else None
          else None
        case Apply(tree, tpt) => unapply(tree)
        case _ => None

  override def prepareForTemplate(tree: Template)(using Context): Context = 
    ignore ++= tree.parents
    ctx

  override def transformApply(tree: Apply)(using Context): Tree = 
    if (tree.tpe.isParameterless && !ignore.exists(i => i.sameTree(tree))) 
      tree match 
        case OnCreateEventsInstance(clsSym) => 
          OnCreateEventsInstance(clsSym, tree) 
        case _ => tree
    else tree

  override def prepareForUnit(tree: Tree)(using Context): Context = 
   if (tree.source.toString.contains("Hello"))
     println(tree.show) 
   ctx

}


