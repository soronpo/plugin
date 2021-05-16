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

extension (clsSym : Symbol) 
  def inherits(parentFullName : String)(using Context) : Boolean = 
    if (clsSym.isClass)
      clsSym.asClass.parentSyms.exists(ps => ps.fullName.toString == parentFullName || ps.inherits(parentFullName))
    else false


class OnCreateEventsPhase(setting: Setting) extends PluginPhase {
  import tpd._

  val phaseName = "OnCreateEvents"

  override val runsAfter = Set(transform.Pickler.name)
  override val runsBefore = Set(transform.FirstTransform.name)
  
  val ignore = mutable.Set.empty[Tree]

  private object OnCreateEventsInstance:
    def apply(clsSym : ClassSymbol, tree : Tree)(using Context): Tree =
      Select(tree, clsSym.requiredMethodRef("onCreate"))
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
    if (!tree.tpe.isContextualMethod && !ignore.contains(tree)) 
      tree match 
        case OnCreateEventsInstance(clsSym) => 
          OnCreateEventsInstance(clsSym, tree) 
        case _ => tree
    else tree

}


