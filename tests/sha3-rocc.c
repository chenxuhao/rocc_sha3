//see LICENSE for license
// The following is a RISC-V program to test the 
// functionality of the sha3 RoCC accelerator.
// Compile with riscv64-unknown-elf-gcc sha3-rocc.c -o sha3-rocc.rv
// Run with spike --extension=sha3 pk a.out

#include <assert.h>
#include <stdio.h>
#include <stdint.h>
#include "sha3.h"
#include "rocc_sha3.h"

int main() {
	do {
		printf("start basic test 1.\n");
		uint64_t data [] = {0xdead, 0xbeef, 0x0bad, 0xf00d}, y;
		uint16_t addr = 1;
		printf("[INFO] Write R[%d] = 0x%lx\n", addr, data[0]);
		doWrite(y, addr, data[0]);

		printf("[INFO] Read R[%d]\n", addr);
		doRead(y, addr);
		printf("[INFO]   Received 0x%lx (expected 0x%lx)\n", y, data[0]);
		assert(y == data[0]);

		unsigned int len = 150;
		unsigned char input[150] = "\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000";
		unsigned char output[SHA3_256_DIGEST_SIZE];

		unsigned int temp = 0;
		asm volatile ("fence");
		printf("Setup...\n");
		setUp(temp, input, output);
		asm volatile ("fence");
		printf("Do Sha3...\n");
		doSha3(temp, len);
		asm volatile ("fence");
		printf("Done...\n");
		// Check result
		unsigned char result[SHA3_256_DIGEST_SIZE] =
		{221,204,157,217,67,211,86,31,54,168,44,245,97,194,193,26,234,42,135,166,66,134,39,174,184,61,3,149,137,42,57,238};
		sha3ONE(input, len, result);
		for(int i = 0; i < SHA3_256_DIGEST_SIZE; i++) {
			printf("output[%d]:%d ==? results[%d]:%d \n",i,output[i],i,result[i]);
			assert(output[i]==result[i]); 
		}
	} while(0);
	printf("success!\n");
	return 0;
}
