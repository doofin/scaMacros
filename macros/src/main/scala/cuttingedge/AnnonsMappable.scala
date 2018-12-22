package cuttingedge

//https://github.com/scalameta/scalameta/blob/master/notes/quasiquotes.md
import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.meta._

@compileTimeOnly("enable macro paradise to expand macro annotations")
class AnnonsMappable extends StaticAnnotation {
  inline def apply(ast: Any): Any = meta {
    ast match {
      case xx@q"..$mods class $tName (..$params) extends $template " =>
        val add=q"""def printit=println("macros print1111 !")"""
        val result = q"""
          $xx
        """
        println(mods)
        println(tName)
        q"""..$mods class $tName(..$params) {
      $add
    }"""
    }
  }
}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class AnnonsChis(insertAst : String) extends StaticAnnotation {
  inline def apply(ast: Any): Any = meta {
    ast match {
      case xx@q"..$mods class $tName (..$params) extends $template " =>
        val stats=q""" val bbb = "bbbbb" """
        val res = q"""..$mods class $tName(..$params) {
      $stats
    }"""
        println(1 to 100 map (_=> "--") reduce(_+_))
        println(res)
        res
    }
  }
}


