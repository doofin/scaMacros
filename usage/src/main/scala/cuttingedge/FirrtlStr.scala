package cuttingedge

import firrtl.Driver._
import firrtl.Utils.throwInternalError
import firrtl._
import firrtl.ir._
import firrtl.passes._
import firrtl.transforms._
import logger.Logger
object FirrtlStr {
  val input = """
circuit tag_array_ext :
  module tag_array_ext :
    input RW0_clk : Clock
    input RW0_addr : UInt<6>
    input RW0_wdata : UInt<80>
    output RW0_rdata : UInt<80>
    input RW0_en : UInt<1>
    input RW0_wmode : UInt<1>
    input RW0_wmask : UInt<4>
  
    inst mem_0_0 of rawr
    inst mem_0_1 of rawr
    inst mem_0_2 of rawr
    inst mem_0_3 of rawr
    mem_0_0.clk <= RW0_clk
    mem_0_0.addr <= RW0_addr
    node RW0_rdata_0_0 = bits(mem_0_0.dout, 19, 0)
    mem_0_0.din <= bits(RW0_wdata, 19, 0)
    mem_0_0.write_en <= and(and(RW0_wmode, bits(RW0_wmask, 0, 0)), UInt<1>("h1"))
    mem_0_1.clk <= RW0_clk
    mem_0_1.addr <= RW0_addr
    node RW0_rdata_0_1 = bits(mem_0_1.dout, 19, 0)
    mem_0_1.din <= bits(RW0_wdata, 39, 20)
    mem_0_1.write_en <= and(and(RW0_wmode, bits(RW0_wmask, 1, 1)), UInt<1>("h1"))
    mem_0_2.clk <= RW0_clk
    mem_0_2.addr <= RW0_addr
    node RW0_rdata_0_2 = bits(mem_0_2.dout, 19, 0)
    mem_0_2.din <= bits(RW0_wdata, 59, 40)
    mem_0_2.write_en <= and(and(RW0_wmode, bits(RW0_wmask, 2, 2)), UInt<1>("h1"))
    mem_0_3.clk <= RW0_clk
    mem_0_3.addr <= RW0_addr
    node RW0_rdata_0_3 = bits(mem_0_3.dout, 19, 0)
    mem_0_3.din <= bits(RW0_wdata, 79, 60)
    mem_0_3.write_en <= and(and(RW0_wmode, bits(RW0_wmask, 3, 3)), UInt<1>("h1"))
    node RW0_rdata_0 = cat(RW0_rdata_0_3, cat(RW0_rdata_0_2, cat(RW0_rdata_0_1, RW0_rdata_0_0)))
    RW0_rdata <= mux(UInt<1>("h1"), RW0_rdata_0, UInt<1>("h0"))

  extmodule rawr :
    input clk : Clock
    input addr : UInt<6>
    input din : UInt<32>
    output dout : UInt<32>
    input write_en : UInt<1>
  
    defname = rawr
"""

  val in2 = """
circuit tag_array_ext :
  module tag_array_ext :
    input RW0_clk : Clock
    input RW0_addr : UInt<6>
    input RW0_wdata : UInt<80>
    output RW0_rdata : UInt<80>
    input RW0_en : UInt<1>
    input RW0_wmode : UInt<1>
    input RW0_wmask : UInt<4>
             
    mem_0_0.clk <= RW0_clk
    mem_0_0.addr <= RW0_addr
"""
//  import scala.tools.reflect._

  class mypass extends firrtl.passes.Pass {
    override def run(c: Circuit): Circuit = {
      c
    }
  }

  def toVerilog(s: String): FirrtlExecutionResult = {
    val optionsManager = new ExecutionOptionsManager("firrtl") with HasFirrtlOptions
    def firrtlConfig   = optionsManager.firrtlOptions

    Logger.makeScope(optionsManager) {
      val finalState: CircuitState = try {
        /*Circuit(info: Info, modules: Seq[DefModule], main: String)*/
        val circuit: Circuit = firrtl.Parser.parse(s)
        val annos            = getAnnotations(optionsManager)

        optionsManager.makeTargetDir()

        firrtlConfig.compiler.compile(
          CircuitState(circuit, ChirrtlForm, annos),
          Seq()
        )
      } catch {
        case e: Exception => throwInternalError(exception = Some(e))
      }

      // Do emission !!!
      // Note: Single emission target assumption is baked in here
      // Note: FirrtlExecutionSuccess emitted is only used if we're emitting the whole Circuit
      val emittedRes = firrtlConfig.getOutputConfig(optionsManager) match {

        case SingleFile(filename) =>
          val emitted = finalState.getEmittedCircuit
          println("emitted verilog : " + emitted.name)
          println(emitted.value) //
          emitted.value

        case OneFilePerModule(dirName) =>
          val emittedModules = finalState.emittedComponents collect { case x: EmittedModule => x }
          if (emittedModules.isEmpty) throwInternalError() // There should be something
          emittedModules.foreach { module =>
            val filename = optionsManager.getBuildFileName(firrtlConfig.outputSuffix, s"$dirName/${module.name}")
          }
          "" // Should we return something different here?
      }
      FirrtlExecutionSuccess(firrtlConfig.compilerName, emittedRes, finalState)
    }
  }

  def run() = {

    // Parse the input
    val state = CircuitState(firrtl.Parser.parse(input), UnknownForm)
    state.getEmittedCircuit
    // Designate a series of transforms to be run in this order
    val transforms: Seq[Transform] =
      Seq(ToWorkingIR, ResolveKinds, InferTypes, ResolveGenders, InferWidths, new ConstantPropagation)

    // Run transforms and capture final state

    val finalState = transforms.foldLeft(state)((c: CircuitState, t: Transform) => t runTransform c)

    // Emit output
    println(finalState.circuit.serialize)
  }

}

/*(
src/main/scala/midas/passes/MidasTransforms.scala

        firrtl.passes.RemoveValidIf,
        new firrtl.transforms.ConstantPropagation,
        firrtl.passes.SplitExpressions,
        firrtl.passes.CommonSubexpressionElimination,
        new firrtl.transforms.DeadCodeElimination,
        new ConfToJSON(conf, json),
        new barstools.macros.MacroCompilerTransform,
        firrtl.passes.ResolveKinds,
        firrtl.passes.RemoveEmpty,
        new Fame1Transform(Some(lib getOrElse json)),
        new strober.passes.StroberTransforms(dir, lib getOrElse json),
        new SimulationMapping(io),
        new PlatformMapping(state.circuit.main, dir))*/
