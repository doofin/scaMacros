// See LICENSE.txt for license details.
package cuttingedge

import chisel3._
import chisel3.util._

import scala.collection.mutable.ArrayBuffer

object chiselCirc {
  class Stack(val depth: Int) extends Module {
    val io = IO(new Bundle {
      val push    = Input(Bool())
      val pop     = Input(Bool())
      val en      = Input(Bool())
      val dataIn  = Input(UInt(32.W))
      val dataOut = Output(UInt(32.W))
    })

    val stack_mem = Mem(depth, UInt(32.W))
    val sp        = RegInit(0.U(log2Ceil(depth + 1).W))
    val out       = RegInit(0.U(32.W))

    when(io.en) {
      when(io.push && (sp < depth.asUInt)) {
        stack_mem(sp) := io.dataIn
        sp := sp + 1.U
      }.elsewhen(io.pop && (sp > 0.U)) {
        sp := sp - 1.U
      }
      when(sp > 0.U) {
        out := stack_mem(sp - 1.U)
      }
    }

    io.dataOut := out
  }

  class matmul extends Module {
    val io = IO(new Bundle {
      val matA = Input(Vec(15, UInt(32.W)))
      val matB = Input(Vec(10, UInt(32.W)))
      val matC = Output(Vec(6, UInt(32.W)))

      val load  = Input(Bool())
      val valid = Output(Bool())
    })
    var sum  = UInt(32.W)
    val matC = new ArrayBuffer[UInt]()

    for (i <- 0 until 6) {
      matC += 0.asUInt(32.W)
    }

    when(io.load) {
      for (i <- 0 until 3) {
        for (j <- 0 until 2) {
          sum = 0.asUInt(32.W)
          for (k <- 0 until 5) {
            sum = sum + io.matA(i * 5 + k) * io.matB(k * 2 + j)
          }
          matC(i * 2 + j) = sum
        }
      }
      io.valid := true.B
    }.otherwise {
      io.valid := false.B
    }

    io.matC := Vec(matC)

  }

  class FullAdder extends Module {
    val io = IO(new Bundle {
      val a    = Input(UInt(1.W))
      val b    = Input(UInt(1.W))
      val cin  = Input(UInt(1.W))
      val sum  = Output(UInt(1.W))
      val cout = Output(UInt(1.W))
    })

    // Generate the sum
    val a_xor_b = io.a ^ io.b
    io.sum := a_xor_b ^ io.cin
    // Generate the carry
    val a_and_b   = io.a & io.b
    val b_and_cin = io.b & io.cin
    val a_and_cin = io.a & io.cin
    io.cout := a_and_b | b_and_cin | a_and_cin
  }

  //A 4-bit adder with carry in and carry out
  class Adder4 extends Module {
    val io = IO(new Bundle {
      val A    = Input(UInt(4.W))
      val B    = Input(UInt(4.W))
      val Cin  = Input(UInt(1.W))
      val Sum  = Output(UInt(4.W))
      val Cout = Output(UInt(1.W))
    })
    //Adder for bit 0
    val Adder0 = Module(new FullAdder())
    Adder0.io.a := io.A(0)
    Adder0.io.b := io.B(0)
    Adder0.io.cin := io.Cin
    val s0 = Adder0.io.sum
    //Adder for bit 1
    val Adder1 = Module(new FullAdder())
    Adder1.io.a := io.A(1)
    Adder1.io.b := io.B(1)
    Adder1.io.cin := Adder0.io.cout
    val s1 = Cat(Adder1.io.sum, s0)
    //Adder for bit 2
    val Adder2 = Module(new FullAdder())
    Adder2.io.a := io.A(2)
    Adder2.io.b := io.B(2)
    Adder2.io.cin := Adder1.io.cout
    val s2 = Cat(Adder2.io.sum, s1)
    //Adder for bit 3
    val Adder3 = Module(new FullAdder())
    Adder3.io.a := io.A(3)
    Adder3.io.b := io.B(3)
    Adder3.io.cin := Adder2.io.cout
    io.Sum := Cat(Adder3.io.sum, s2).asUInt
    io.Cout := Adder3.io.cout
  }
  //A n-bit adder with carry in and carry out
  class Adder(val n: Int) extends Module {
    val io = IO(new Bundle {
      val A    = Input(UInt(n.W))
      val B    = Input(UInt(n.W))
      val Cin  = Input(UInt(1.W))
      val Sum  = Output(UInt(n.W))
      val Cout = Output(UInt(1.W))
    })
    //create an Array of FullAdders
    //  NOTE: Since we do all the wiring during elaboration and not at run-time,
    //  i.e., we don't need to dynamically index into the data structure at run-time,
    //  we use an Array instead of a Vec.
    val FAs   = Array.fill(n)(Module(new FullAdder()).io)
    val carry = Wire(Vec(n + 1, UInt(1.W)))
    val sum   = Wire(Vec(n, Bool()))

    //first carry is the top level carry in
    carry(0) := io.Cin

    //wire up the ports of the full adders
    for (i <- 0 until n) {
      FAs(i).a := io.A(i)
      FAs(i).b := io.B(i)
      FAs(i).cin := carry(i)
      carry(i + 1) := FAs(i).cout
      sum(i) := FAs(i).sum.toBool()
    }
    io.Sum := sum.asUInt
    io.Cout := carry(n)
  }

}
