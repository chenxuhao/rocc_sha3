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
	case BuildRoCC => List(
		(p: Parameters) => {
			val sha3 = LazyModule(new Sha3Accel(OpcodeSet.custom0)(p))
			sha3
		}
	)
})

class Sha3AccelConfig extends Config(new WithSha3Accel ++ new DefaultConfig)
