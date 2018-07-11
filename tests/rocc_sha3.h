// See LICENSE for license details.

#ifndef SRC_MAIN_C_ACCUMULATOR_H
#define SRC_MAIN_C_ACCUMULATOR_H

#include "xcustom.h"

#define k_SET_UP  0
#define k_DO_SHA3 1
//#define k_DO_READ 1
//#define k_DO_LOAD 2

#define XCUSTOM_ACC 0

#define setUp(x, in, out)                                               \
  ROCC_INSTRUCTION(XCUSTOM_ACC, x, in, out, k_SET_UP);
#define doSha3(x, y)                                                    \
  ROCC_INSTRUCTION(XCUSTOM_ACC, x, y, 0, k_DO_SHA3);
//#define doRead(y, rocc_rd)                                              \
  ROCC_INSTRUCTION(XCUSTOM_ACC, y, 0, rocc_rd, k_DO_READ);
//#define doLoad(y, rocc_rd, mem_addr)                                    \
  ROCC_INSTRUCTION(XCUSTOM_ACC, y, mem_addr, rocc_rd, k_DO_LOAD);

#endif  // SRC_MAIN_C_ACCUMULATOR_H
