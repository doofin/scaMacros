package cuttingedge

import chisel3.{ChiselExecutionSuccess, Driver}
//import firrtl.customized._
//import firrtl.customized.myUtils._
//import chisel3.customized
import firrtl_interpreter._
object main extends App {
  override def main(args: Array[String]): Unit = {
    println("mainU")
//    MacroUsage.sdfsdf(1).printit
//    defMacros.isEvenLog(2)
//    Fir.run()

    Driver.execute(Array(""), () => new complex2lev) match {
      case ChiselExecutionSuccess(Some(circuit), emittedChirrtl, _) =>
        println("ChiselExecutionSuccess : !!!")
        println(circuit)
        println(1 to 100 map (_ => "-") reduce (_ + _))
        println("chirrtl : ")
        println(emittedChirrtl)
        println("gen toVerilog")
        println(FirrtlStr.toVerilog(emittedChirrtl))
      case _ =>
    }
    //    Fir.toVerilog(Fir.input)
  }

  val interpt = () => {

    new InterpretiveTester("") {
      peek("")
    }
//    val circ=

  }
}
