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


