//see LICENSE for license
//authors: Xuhao Chen
package sha3
import chisel3._
import chisel3.util._
import freechips.rocketchip.rocket._
import freechips.rocketchip.config._
import freechips.rocketchip.tile._

case object WidthP extends Field[Int]
case object Stages extends Field[Int]
case object FastMem extends Field[Boolean]
case object BufferSram extends Field[Boolean]

class Sha3Accel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes) {
	override lazy val module = new Sha3AccelModuleImp(this)
}

class Sha3AccelModuleImp(outer: Sha3Accel) extends LazyRoCCModuleImp(outer) with HasCoreParameters {
	val w = outer.p(WidthP)
	val s = outer.p(Stages)

	// control
	val ctrl = Module(new CtrlModule(w,s)(outer.p))

	ctrl.io.rocc_funct   <> io.cmd.bits.inst.funct
	ctrl.io.rocc_rs1     <> io.cmd.bits.rs1
	ctrl.io.rocc_rs2     <> io.cmd.bits.rs2
	ctrl.io.rocc_rd      <> io.cmd.bits.inst.rd
	ctrl.io.rocc_req_val <> io.cmd.valid
	ctrl.io.rocc_req_rdy <> io.cmd.ready
	ctrl.io.busy         <> io.busy

	// datapath
	val dpath = Module(new DpathModule(w,s))

	dpath.io.message_in <> ctrl.io.buffer_out
	dpath.io.init   <> ctrl.io.init
	dpath.io.round  <> ctrl.io.round
	dpath.io.write  <> ctrl.io.write
	dpath.io.absorb <> ctrl.io.absorb
	dpath.io.aindex <> ctrl.io.aindex

	// output hash back to the memory
	io.mem.req.bits.data := dpath.io.hash_out(ctrl.io.windex)

	io.interrupt := false.B
	io.mem.req.valid := false.B
}

