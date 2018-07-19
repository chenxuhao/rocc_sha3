package sha3
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class IotaTests(c: IotaModule) extends PeekPokeTester(c) {
	val w = 64
	val maxInt  = 1 << (5*5*w)
	val round = 0
	val state = Array.fill(5*5){BigInt(3)}
	val out_state = Array.fill(5*5){BigInt(3)}
	out_state(0) = state(0) ^ BigInt(1)
	//printf("cxh debug: out_state(0) = %d\n", out_state(0))
	poke(c.io.state_i, state)
	poke(c.io.round, round)
	step(1)
	for(i <- 0 until 25) {
		expect(c.io.state_o(i), out_state(i))
	}
}

class IotaTester extends ChiselFlatSpec {
	behavior of "Iota"
	backends foreach { backend =>
		it should s"test the basic iota circuit" in {
			Driver(() => new IotaModule, backend)((c) => new IotaTests(c)) should be (true)
		}
	}
}
/*
object iotaMain { 
	def main(args: Array[String]): Unit = {
		chiselMainTest(args, () => Module(new IotaModule())) {
			c => new IotaModuleTests(c)
		}
	}
}
*/
