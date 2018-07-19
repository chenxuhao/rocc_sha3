// See LICENSE.txt for license details.
package sha3

import chisel3._
import chisel3.iotesters.{Driver, TesterOptionsManager}
//import Sha3Runner

object Launcher {
  val tests = Map(
    "Theta" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new ThetaModule(), manager) {
        (c) => new ThetaTests(c)
      }
    },
    //"RhoPi" -> { (manager: TesterOptionsManager) =>
    //  Driver.execute(() => new RhoPiModule(), manager) {
    //    (c) => new RhoPiTests(c)
    //  }
    //},
    "Chi" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new ChiModule(), manager) {
        (c) => new ChiTests(c)
      }
    },
    "Iota" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new IotaModule(), manager) {
        (c) => new IotaTests(c)
      }
    }/*,
    "Ctrl" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new Ctrl(), manager) {
        (c) => new CtrlTests(c)
      }
    },
    "Dpath" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new Dpath(), manager) {
        (c) => new DpathTests(c)
      }
    },
    "Sha3" -> { (manager: TesterOptionsManager) =>
      Driver.execute(() => new Sha3(), manager) {
        (c) => new Sha3Tests(c)
      }
    }
*/
  )

  def main(args: Array[String]): Unit = {
    Sha3Runner("sha3", tests, args)
  }
}
