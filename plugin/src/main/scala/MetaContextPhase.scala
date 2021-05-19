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
  var positionCls : ClassSymbol = _
  extension (tree : Tree)(using Context) 
    def simplePos : String = 
      val pos = tree.sourcePos.startPos
      s"${pos.source.path}:${pos.line}:${pos.column}"
    def setMeta(nameOpt : Option[String], srcPos : util.SrcPos, lateConstruction : Boolean) : Tree = 
      val nameOptTree = nameOpt match
        case Some(str) => 
          New(defn.SomeClass.typeRef.appliedTo(defn.StringType), Literal(Constant(str)) :: Nil)
        case None => 
          ref(defn.NoneModule.termRef)
      val setMetaSym = tree.symbol.requiredMethod("setMeta")
      val fileNameTree = Literal(Constant(srcPos.startPos.source.path))
      val lineTree = Literal(Constant(srcPos.startPos.line+1))
      val columnTree = Literal(Constant(srcPos.startPos.column+1))
      val positionTree = 
        New(positionCls.typeRef, fileNameTree :: lineTree :: columnTree :: Nil)
      val lateConstructionTree = Literal(Constant(lateConstruction))
      tree
      .select(setMetaSym)
      .appliedToArgs(nameOptTree :: positionTree :: lateConstructionTree :: Nil)
      .withType(TermRef(tree.tpe, setMetaSym))

  extension (tree : Apply)(using Context) 
    def replaceArg(fromArg : Tree, toArg : Tree) : Tree =
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
    
  val treeOwnerMap = mutable.Map.empty[Tree, Tree]
  val contextDefs = mutable.Map.empty[String, Tree]
  val ignore = mutable.Set.empty[Tree]

  extension (sym : Symbol) def fixedFullName(using Context) : String =
    sym.fullName.toString.replace("._$",".")

  private object ContextArg: 
    def unapply(tree : Tree)(using Context) : Option[Tree] = 
      tree match
        case Apply(tree, args) => args.collectFirst {
          case a if a.tpe.typeSymbol.inherits("counter.MetaContext") => a
        }.orElse(unapply(tree))
        case _ => None

  override def transformApply(tree: Apply)(using Context): Tree = 
    if (!ignore.exists(i => i.sameTree(tree))) tree match
      case ContextArg(argTree) =>
        val sym = argTree.symbol
        treeOwnerMap.get(tree) match 
          case Some(t : ValDef) =>
            tree.replaceArg(argTree, argTree.setMeta(Some(t.name.toString), tree.srcPos, false))
          case Some(t : TypeDef) if t.name.toString.endsWith("$") => 
            tree.replaceArg(argTree, argTree.setMeta(Some(t.name.toString.dropRight(1)), tree.srcPos, false))
          case Some(t)  =>  //Def or Class
            contextDefs.get(sym.fixedFullName) match
              case Some(ct) if ct != t =>
                report.error(s"${t.symbol} is missing an implicit Context parameter", t.symbol)
              case _ => //do nothing
            tree
          case _ => //Anonymous
            tree.replaceArg(argTree, argTree.setMeta(None, tree.srcPos, false))
      case _ => tree
    else tree

  val localPattern = "\\<local (.*)\\$\\>".r
  override def prepareForTypeDef(tree: TypeDef)(using Context): Context = 
    tree.rhs match
      case template : Template if !tree.symbol.isAnonymousClass => 
        template.parents.foreach(p => treeOwnerMap += (p -> tree))
        addContextDef(tree)
      case _ => 
    ctx

  @tailrec private def nameValOrDef(tree : Tree, ownerTree : Tree)(using Context) : Unit = 
    tree match 
      case apply : Apply => 
        ignoreInternalApplies(apply)
        treeOwnerMap += (apply -> ownerTree)
      case Block((cls @ TypeDef(tpn, template : Template)) :: _, expr) if cls.symbol.isAnonymousClass => 
        template.parents.foreach(p => treeOwnerMap += (p -> ownerTree))
      case block : Block => nameValOrDef(block.expr, ownerTree)
      case _ => 
  
  def addContextDef(tree : Tree)(using Context) : Unit =
    val defdefTree = tree match
      case tree : DefDef => tree
      case tree : TypeDef => 
        // println(tree.symbol)
        tree.rhs.asInstanceOf[Template].constr
    defdefTree.paramss.flatten.view.reverse.collectFirst {
      case a if a.tpe.typeSymbol.inherits("counter.MetaContext") =>
        val fixedName = a.symbol.fixedFullName
        // println(s"Def   ${fixedName}, ${tree.show}")
        contextDefs += (fixedName -> tree)
    }
    
  override def prepareForDefDef(tree: DefDef)(using Context): Context = 
    if (!tree.symbol.isClassConstructor)
      addContextDef(tree)
      nameValOrDef(tree.rhs, tree)  
    ctx
  
  @tailrec private def ignoreInternalApplies(tree : Apply)(using Context) : Unit = 
    tree.fun match 
      case apply : Apply => 
        ignore += apply
        ignoreInternalApplies(apply)
      case _ =>
  
  override def prepareForValDef(tree: ValDef)(using Context): Context =
    nameValOrDef(tree.rhs, tree)
    ctx

  override def prepareForUnit(tree: Tree)(using Context): Context = 
    positionCls = requiredClass("counter.Position")
    if (tree.source.toString.contains("Hello"))
      println(tree.show) 
    ctx

}

