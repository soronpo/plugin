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
  extension (tree : Tree)(using Context) def simplePos : String = 
    val pos = tree.sourcePos.startPos
    s"${pos.source.path}:${pos.line}:${pos.column}"
  
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
    if (!ignore.contains(tree)) tree match 
      case ContextArg(argTree) =>
        val sym = argTree.symbol
        treeOwnerMap.get(tree) match
          case Some(t : DefDef) => 
            contextDefs.get(sym.fixedFullName) match
              case Some(ct) if ct != t =>
                report.error(s"${t.symbol} is missing an implicit Context parameter", t.symbol)
              case _ => //do nothing
          case Some(t : ValDef) =>
            println(s"Val ${t.name}")
          case Some(t : TypeDef) if t.name.toString.endsWith("$") => 
            println(s"Obj ${t.name.toString.dropRight(1)}")
          case Some(t : TypeDef)  => 
            if (t != contextDefs(sym.fixedFullName))
              report.error(s"${t.symbol} is missing an implicit Context parameter", t.symbol)
          case _ =>
            println(s"Anonymous at ${tree.simplePos}")
        tree
      case _ => tree
    else tree

  val localPattern = "\\<local (.*)\\$\\>".r
  override def prepareForTypeDef(tree: TypeDef)(using Context): Context = 
    tree.rhs match
      case template : Template if tree.name.toString != "$anon" => 
        template.parents.foreach(p => treeOwnerMap += (p -> tree))
        addContextDef(tree)
      case _ => 
    ctx

  @tailrec private def nameValOrDef(tree : Tree, ownerTree : Tree)(using Context) : Unit = 
    tree match 
      case apply : Apply => 
        ignoreInternalApplies(apply)
        treeOwnerMap += (apply -> ownerTree)
      case Block(TypeDef(tpn, cls : Template) :: _, expr) if tpn.toString == "$anon"=> 
        cls.parents.foreach{p => 
          treeOwnerMap += (p -> ownerTree)
        }
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
    if (tree.source.toString.contains("Hello"))
      println(tree.show) 
    ctx

}

