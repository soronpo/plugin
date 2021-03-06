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

abstract class CommonPhase extends PluginPhase:
  import tpd._
  extension (clsSym : Symbol)
    def inherits(parentFullName : String)(using Context) : Boolean =
      if (clsSym.isClass)
        clsSym.asClass.parentSyms.exists(ps => ps.fullName.toString == parentFullName || ps.inherits(parentFullName))
      else false

  extension (tree : Tree)(using Context)
    def unique : String =
      val pos = tree.srcPos.startPos
      val endPos = tree.srcPos.endPos
      s"${pos.source.path}:${pos.line}:${pos.column}-${endPos.line}:${endPos.column}"

  extension (tree : Apply)(using Context)
    def replaceArg(fromArg : Tree, toArg : Tree) : Apply =
      var changed = false
      val repArgs = tree.args.map {a =>
        if (a == fromArg)
          changed = true
          toArg
        else a
      }
      tree.fun match
        case apply : Apply if !changed =>
          Apply(apply.replaceArg(fromArg, toArg), tree.args)
        case _ =>
          Apply(tree.fun, repArgs)

  extension (tree : Apply)(using Context)
    def isContextDelegate : Boolean = 
      tree.symbol.annotations.exists(_.symbol.name.toString == "metaContextDelegate")
  
  object ContextArg:
    def unapply(tree : Tree)(using Context) : Option[Tree] =
      tree match
        case Apply(tree, args) => args.collectFirst {
          case a if a.tpe.typeSymbol.inherits("counter.MetaContext") => a
        }.orElse(unapply(tree))
        case _ => None

  object ApplyFunArgs:
    @tailrec private def recurUnapply(fun : Tree, args : List[List[Tree]])(using Context) : (Tree, List[List[Tree]]) = 
      fun match
        case Apply(f, a) => recurUnapply  (f, a :: args)
        case f => (f, args)
    def unapply(tree : Apply)(using Context) : Option[(Tree, List[List[Tree]])] = 
      Some(recurUnapply(tree, Nil))
    def apply(fun : Tree, args : List[List[Tree]])(using Context) : Apply = 
      fun.appliedToArgss(args).asInstanceOf[Apply]
