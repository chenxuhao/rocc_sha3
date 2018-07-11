//see LICENSE for license
//authors: Xuhao Chen
package sha3
import Chisel._
import freechips.rocketchip.rocket._
import freechips.rocketchip.config._
import freechips.rocketchip.tile._

class FibAccel(opcodes: OpcodeSet, val n: Int = 70)(implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new FibAccelModuleImp(this)
}

class FibAccelModuleImp(outer: FibAccel) extends LazyRoCCModuleImp(outer)
    with HasCoreParameters {
  val regfile = Mem(outer.n, UInt(width = xLen))
  val funct = io.cmd.bits.inst.funct
  val length = io.cmd.bits.rs1
  val addr = io.cmd.bits.rs2(log2Up(outer.n)-1,0)
  val doWrite = funct === UInt(0)
  val doRead = funct === UInt(1)
  val doLoad = funct === UInt(2)
  val doAccum = funct === UInt(3)

  regfile(0) := UInt(0)
  regfile(1) := UInt(1)
  val counter = Reg(init = 2.U(8.W))
  when (counter < UInt(70) && counter < length) {
    regfile(counter) := regfile(counter - UInt(1)) + regfile(counter - UInt(2))
	counter := counter + 1.U
  }
  val s_idle :: s_req :: s_resp :: s_other :: Nil = Enum(Bits(), 4)
  val state = Reg(init = s_idle)

  // datapath
  io.cmd.ready := (state === s_idle)
  val sha3 = regfile(addr)
  
  // control
  when (io.cmd.fire()) { state := s_req }
  when (io.resp.fire()) { state := s_idle }

  io.resp.valid := io.cmd.valid
  io.resp.bits.rd := io.cmd.bits.inst.rd
  io.resp.bits.data := sha3

  io.busy := io.cmd.valid
  io.interrupt := Bool(false)
  io.mem.req.valid := Bool(false)
}
