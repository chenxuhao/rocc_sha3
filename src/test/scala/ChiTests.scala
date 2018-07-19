package sha3
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import scala.util.Random

class ChiTests(c: ChiModule) extends PeekPokeTester(c) {
	val rand    = new Random(1)
	val w       = 4
	val in_state = Array.fill(5*5){BigInt(rand.nextInt(1 <<w))}
	val out_state = Array.fill(5*5){BigInt(0)}
	//printf("cxh debug: in_state(0) = %d\n", in_state(0))
	//printf("cxh debug: in_state(1) = %d\n", in_state(1))
	//printf("cxh debug: in_state(2) = %d\n", in_state(2))
	//printf("cxh debug: in_state(3) = %d\n", in_state(3))
	//printf("cxh debug: in_state(4) = %d\n", in_state(4))
	for(i <- 0 until 5) {
		for(j <- 0 until 5) {
			out_state(i*5+j) = in_state(i*5+j) ^ 
				( ~in_state(i*5+((j+1)%5)) & in_state(i*5+((j+2)%5)))
		}
	}
	for(i <- 0 until 25) {
		poke(c.io.state_i(i), in_state(i))
	}
	step(1)
	for(i <- 0 until 25) {
		expect(c.io.state_o(i), out_state(i))
	}
}

class ChiTester extends ChiselFlatSpec {
	behavior of "Chi"
	backends foreach { backend =>
		it should s"test the basic chi circuit" in {
			Driver(() => new ChiModule, backend)((c) => new ChiTests(c)) should be (true)
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
