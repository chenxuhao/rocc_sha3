package sha3
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import scala.util.Random

class CtrlTests(c: CtrlModule) extends PeekPokeTester(c) {
	val inst_funct = 0
	val rs1 = 0x3
	val rs2 = 0x8
	val rd  = 0x0
	val valid = true
	val ready = true
	val fire = false
	val mem_req_ready = false
	val mem_resp_valid = false
	val mem_resp_tag = 0
	val mem_resp_data = 0
	poke(c.io.dmem_req_rdy, mem_req_ready)
	poke(c.io.dmem_resp_val, mem_resp_valid)
	poke(c.io.dmem_resp_tag, mem_resp_tag)
	poke(c.io.dmem_resp_data, mem_resp_data)
	poke(c.io.rocc_inst_funct, inst_funct)
	poke(c.io.rocc_rs1, rs1)
	poke(c.io.rocc_rs2, rs2)
	poke(c.io.rocc_rd, rd)
	poke(c.io.rocc_fire, fire)
	poke(c.io.rocc_req_val, valid)
	step(1)
	expect(c.io.rocc_req_rdy, ready)
}

class CtrlTester extends ChiselFlatSpec {
	behavior of "Ctrl"
	backends foreach { backend =>
		it should s"test the basic ctrl circuit" in {
			Driver(() => new CtrlModule, backend)((c) => new CtrlTests(c)) should be (true)
		}
	}
}

