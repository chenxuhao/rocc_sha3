package sha3
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.util.Random


class ThetaTests(c: ThetaModule) extends PeekPokeTester(c) {
	def ROTL(x: BigInt, y: Int, w: Int) = (((x) << (y)) | ((x) >> (w - (y))))
	val rand    = new Random(1)
	val w       = 4
	val in_state = Array.fill(5*5){BigInt(rand.nextInt(1 <<w))} // random number between 0 ~ 15
	val out_state = Array.fill(5*5){BigInt(0)}
	//for(i <- 0 until 25) {
	//	printf("cxh debug: in_state(%d) = %d\n", i, in_state(i))
	//}
	val bc = Array.fill(5){BigInt(0)}
	for(i <- 0 until 5) {
		bc(i) = in_state(0*5+i) ^ in_state(1*5+i) ^ in_state(2*5+i) ^ in_state(3*5+i) ^ in_state(4*5+i)
	}
	//for(i <- 0 until 5) {
	//	printf("cxh debug: bc(%d)=%d\n", i, bc(i))
	//}
	for(i <- 0 until 5) {
		val t = bc((i+4)%5) ^ ROTL(bc((i+1)%5), 1, 64)
		//printf("cxh debug: t(%d)=%d\n", i, t)
		for(j <- 0 until 5) {
			out_state(i*5+j) = in_state(i*5+j) ^ t
		}
	}

	for (i <- 0 until 25) {
		poke(c.io.state_i(i), in_state(i))
	}
	step(1)
	for(i <- 0 until 25) {
		expect(c.io.state_o(i), out_state(i))
	}
}

class ThetaTester extends ChiselFlatSpec {
	behavior of "Theta"
	backends foreach { backend =>
		it should s"test the basic theta circuit" in {
			Driver(() => new ThetaModule, backend)((c) => new ThetaTests(c)) should be (true)
		}
	}
}
/*
object thetaMain { 
  def main(args: Array[String]): Unit = {
    val res =
    args(0) match {
      // Generate default design and dump parameter space
      case "THETA_dump" => {
        chiselMain(args.slice(2,args.length), () => Module(new ThetaModule()))
        Params.dump(args(1))
      }
      // Generate design based on design point input
      case "THETA" => {
        Params.load(args(1))
        chiselMain(args.slice(2,args.length), () => Module(new ThetaModule()))
      }
      case "THETA_test" => {
        Params.load(args(1))
        chiselMainTest(args.slice(1,args.length), () => Module(new ThetaModule())) {c => new ThetaModuleTests(c) }
      }
      case "THETA_NP_test" => {
        chiselMainTest(args.slice(1,args.length), () => Module(new ThetaModule())) {c => new ThetaModuleTests(c) }
      }
      case _ => {
        printf("Bad arg(0)\n")
      }
    }
  }
}
*/
