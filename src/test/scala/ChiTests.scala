package sha3
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class ChiModuleTests(c: ChiModule) extends Tester(c) {
	val rand    = new Random(1)
	val W       = 4
	val maxInt  = 1 << (5*5*W)
	for (i <- 0 until 1) {
		//val state_i = rnd.nextInt(maxInt)
		val state = Array.fill(5*5){BigInt(rand.nextInt(1 <<W))}
		val out_state = Array.fill(5*5){BigInt(0)}
		for(i <- 0 until 5) {
			for(j <- 0 until 5) {
				out_state(i*5+j) = state(i*5+j) ^ 
					( ~state(i*5+((j+1)%5)) & state(i*5+((j+2)%5)))
			}
		}
		poke(c.io.state_i, state)
		step(1)
		expect(c.io.state_o, out_state)
	}
}

class ChiTester extends ChiselFlatSpec {
	behavior of "Chi"
	backends foreach { backend =>
		it should s"test the basic chi circuit" in {
			Driver(() => new Chi, backend)((c) => new ChiTests(c)) should be (true)
		}
	}
}

/*
object chiMain { 
  def main(args: Array[String]): Unit = {
    //chiselMainTest(Array[String]("--backend", "c", "--genHarness", "--compile", "--test"),
    chiselMainTest(args,
      () => Module(new ChiModule())){c => new ChiModuleTests(c)
    }
  }
}
*/
