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

class MetaContextDelegatePhase(setting: Setting) extends CommonPhase {
  import tpd._

  val phaseName = "MetaContextDelegate"

  override val runsAfter = Set(transform.Pickler.name)
  override val runsBefore = Set("MetaContextGen")
  val ignore = mutable.Set.empty[String]

  extension (tree : Apply)(using Context)
    def isContextDelegate : Boolean = 
      tree.symbol.annotations.exists(_.symbol.name.toString == "metaContextDelegate")

  override def transformApply(tree: Apply)(using Context): Tree =
    tree

  override def prepareForTypeDef(tree: TypeDef)(using Context): Context =
    ctx

  override def transformTypeDef(tree: TypeDef)(using Context): Tree = 
    tree

  override def prepareForValDef(tree: ValDef)(using Context): Context =
    ctx

  override def prepareForUnit(tree: Tree)(using Context): Context = 
    if (tree.source.toString.contains("Hello"))
      println(tree.show) 
    ctx

}

