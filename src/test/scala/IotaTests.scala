package sha3
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class IotaModuleTests(c: IotaModule) extends Tester(c) {
	val W = 64
	val maxInt  = 1 << (5*5*W)
	val round = 0
	val state = Array.fill(5*5){BigInt(3)}
	val out_state = Array.fill(5*5){BigInt(3)}
	out_state(0) = state(0) ^ BigInt(1)
	poke(c.io.state_i, state)
	poke(c.io.round, round)
	step(1)
	expect(c.io.state_o, out_state)
}

class IotaTester extends ChiselFlatSpec {
	behavior of "Iota"
	backends foreach { backend =>
		it should s"test the basic theta circuit" in {
			Driver(() => new Iota, backend)((c) => new IotaTests(c)) should be (true)
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
