package cuttingedge
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
object defMacros {
  import scala.reflect.macros.blackbox
  def isEvenLog(number: Int) = macro isEvenLogImplementation

  def isEvenLogImplementation(c: blackbox.Context)(number: c.Expr[Int]): c.Tree = {
    import c.universe._
    println("isEvenLogImplementation")
    println(number.toString())
    q"""
      if ($number%2==0){
        println($number.toString + " is even")
      }else {
        println($number.toString + " is odd")
      }
    """
  }
}
/*
def anylen[t](x: Seq[t]) = macro map[t]

def map[T : c.WeakTypeTag](c: blackbox.Context)(p: c.Expr[Seq[T]]): c.Tree = {
  import c.universe._
  q"""1"""
}
 */
