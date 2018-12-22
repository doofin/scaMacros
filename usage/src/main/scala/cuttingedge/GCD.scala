// See LICENSE.txt for license details.
package cuttingedge

import chisel3._
import chisel3.util.Cat

/**
  * Compute the GCD of 'a' and 'b' using Euclid's algorithm.
  * To start a computation, load the values into 'a' and 'b' and toggle 'load'
  * high.
  * The GCD will be returned in 'out' when 'valid' is high.
  */
//@AnnonsChis(""" val a = "aaabbbbbb"  """)
//@AnnonsMappable
class GCD extends Module {
  val io = IO(new Bundle {
    val a     = Input(UInt(16.W))
    val b     = Input(UInt(16.W))
    val load  = Input(Bool())
    val out   = Output(UInt(16.W))
    val valid = Output(Bool())
  })
  val x = Reg(UInt())
  val y = Reg(UInt())

  when(io.load) {
    x := io.a; y := io.b
  }.otherwise {
    when(x > y) {
      x := x - y
    }.elsewhen(x <= y) {
      y := y - x
    }
  }

  io.out := x
  io.valid := y === 0.U
}

class GCD22 extends Module {
  val io = IO(new Bundle {
    val a     = Input(UInt(16.W))
    val b     = Input(UInt(16.W))
    val load  = Input(Bool())
    val out   = Output(UInt(16.W))
    val valid = Output(Bool())
  })
  val x = Reg(UInt())
  val y = Reg(UInt())

  when(io.load) {
    x := io.a; y := io.b
  }.otherwise {
    when(x > y) {
      x := x - y
    }.elsewhen(x <= y) {
      y := y - x
    }
  }

  io.out := x
  io.valid := y === 0.U
}

class multireg0 extends Module {
  val io = IO(new Bundle {
    val a     = Input(UInt(16.W))
    val b     = Input(UInt(16.W))
    val load  = Input(Bool())
    val out   = Output(UInt(16.W))
    val valid = Output(Bool())
    val outs2 = Output(UInt(4.W))
  })
  val x = Reg(UInt())
  val y = Reg(UInt())

  x := io.a
  y := io.b
  io.out := x
  io.valid := y

  val shif = Module(new shifter)

  io.outs2 := shif.io.c
  shif.io.a := 0.U
  shif.io.b := 0.U

  val shif1 = Module(new shifter)

  io.outs2 := shif.io.c
  shif1.io.a := 0.U
  shif1.io.b := 0.U
}

class shifter extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(4.W))
    val b = Input(UInt(4.W))
    val c = Output(UInt(4.W))

  })
  val gcd = Module(new GCD)
  gcd.io.a := 0.U
  gcd.io.b := 0.U
  gcd.io.load := 0.U
  val bb = Reg(UInt(4.W))
  bb := io.b << io.a
  io.c := bb
}

class use_mem extends Module {
  val io = IO(new Bundle {

    val wen   = Input(Bool())
    val waddr = Input(UInt(16.W))
    val wdata = Input(UInt(32.W))
    val ren   = Input(Bool())
    val raddr = Input(UInt(16.W))
    val rdata = Output(UInt(32.W))
  })
  val mem   = Mem(UInt(32.W), 1 << 16)
  val rdata = Reg(UInt(32.W))
  when(io.wen) {
    mem(io.waddr) := io.wdata
  }
  when(io.ren) {

    rdata := mem(io.raddr)
  }
  io.rdata := rdata

}
class complex2lev extends Module {
  val io = IO(new Bundle {
    val a      = Input(UInt(16.W))
    val b      = Input(UInt(16.W))
    val load   = Input(Bool())
    val out    = Output(UInt(16.W))
    val valid  = Output(Bool())
    val outs1  = Output(UInt(16.W))
    val aggreg = Output(UInt())

    val wen   = Input(Bool())
    val waddr = Input(UInt(16.W))
    val wdata = Input(UInt(32.W))
    val ren   = Input(Bool())
    val raddr = Input(UInt(16.W))
    val rdata = Output(UInt(32.W))
//    val outs2 = Output(UInt(4.W))
  })
  val x = Reg(UInt(16.W))
  val y = Reg(UInt(16.W))
  val r = Module(new multireg0)
  r.io.a := io.a
  r.io.b := io.b
  r.io.load := io.load
  x := io.a
  y := io.b
  io.out := x
  io.valid := y
  io.outs1 := r.io.a

  val mem = Module(new use_mem)
  io.aggreg := Cat(io.out, io.outs1, io.rdata)
//  val shif = Module(new shifter)
//
//  io.outs2 := shif.io.c
//  shif.io.a := 0.U
//  shif.io.b := 0.U

  mem.io.wen := io.wen
  mem.io.waddr := io.waddr
  mem.io.wdata := io.wdata
  mem.io.ren := io.ren
  mem.io.raddr := io.raddr
  io.rdata := mem.io.rdata
}
