//see LICENSE for license
package freechips.rocketchip.system

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.system._
import freechips.rocketchip.tile._
import freechips.rocketchip.diplomacy._
import sha3._

class WithSha3Accel extends Config((site, here, up) => {
	case WidthP => 64
	case Stages => 1
	case FastMem => false 
	case BufferSram => false
	case BuildRoCC => List(
		(p: Parameters) => {
			val sha3 = LazyModule(new Sha3Accel(OpcodeSet.custom0)(p))
			sha3
		}
	)
})
/*
class MyDefaultConfig() extends Config(new WithNBigCores(1) ++ new BaseConfig) {
	override val topDefinitions:World.TopDefs = {
		(pname,site,here) => pname match {
			case WidthP => 64
			case Stages => Knob("stages")
			case FastMem => Knob("fast_mem")
			case BufferSram => Dump(Knob("buffer_sram"))
		}
	}
	override val topConstraints:List[ViewSym=>Ex[Boolean]] = List(
			ex => ex(WidthP) === 64,
			ex => ex(Stages) >= 1 && ex(Stages) <= 4 && (ex(Stages)%2 === 0 || ex(Stages) === 1),
			ex => ex(FastMem) === ex(FastMem),
			ex => ex(BufferSram) === ex(BufferSram)
			)
	override val knobValues:Any=>Any = {
		case "stages" => 1
		case "fast_mem" => true
		case "buffer_sram" => false
	}
}
*/
class Sha3AccelConfig extends Config(new WithSha3Accel ++ new DefaultConfig)
