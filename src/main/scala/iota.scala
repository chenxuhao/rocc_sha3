//see LICENSE for license

package sha3

import chisel3._

class IotaModule(val w: Int = 64) extends Module {
	val io = new Bundle {
		val state_i = Vec(25, Bits(w.W))
		val state_o = Vec(25, Bits(w.W))
		val round = UInt(5)
	}
	for(i <- 0 until 5) {
		for(j <- 0 until 5) {
			if(i !=0 || j!=0)
				io.state_o(i*5+j) := io.state_i(i*5+j)
		}
	}

	val temp = IOTA.round_const(io.round)
    io.state_o(0) := io.state_i(0) ^ temp
}
