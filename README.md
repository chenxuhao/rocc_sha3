SHA3 RoCC Accelerator
=============
SHA3 is a cryptographic algorithm. The algorithm operates on variable length messages with 
a sponge function, and thus alternates between absorbing chunks of the message into a set 
of state bits and permuting the state. The absorbing is a simple bitwise XOR while the 
permutation is a more complex function composed of several operations, χ, θ, ρ, π, ι, that 
all perform various bitwise operations, including rotations, parity calculations, XORs, etc.

Usage:

If cloned into rocket-chip directory use

    cd rocket-chip/
    git clone https://github.com:chenxuhao/rocc_sha3.git
    mv sha3_rocc sha3

You can build the emulator using

    cd emulator && make ROCKETCHIP_ADDONS=sha3 CONFIG=FibAccelConfig


You can emulate the software implementation of sha3 by running

    ./emulator-freechips.rocketchip.system-FibAccelConfig pk ../sha3/tests/sha3-sw.rv

or

    ./emulator-freechips.rocketchip.system-FibAccelConfig pk ../sha3/tests/sha3-sw-bm.rv

You can emulate the accelerated sha3 by running

    ./emulator-freechips.rocketchip.system-FibAccelConfig pk ../sha3/tests/sha3-rocc-bm.rv

or 

    ./emulator-freechips.rocketchip.system-FibAccelConfig pk ../sha3/tests/sha3-rocc.rv

The -bm versions of the code omit the print statements and will complete faster.
