//see LICENSE for license

package sha3

import chisel3._
import chisel3.util._
//import freechips.rocketchip.rocket._
//import freechips.rocketchip.tile._
import freechips.rocketchip.config._

class CtrlModule(val w: Int, val s: Int)(implicit p: Parameters) extends Module() {
	val r = 2*256
	val c = 25*w - r
	val round_size_words = c/w
	val rounds = 24 //12 + 2l
	val hash_size_words = 256/w
	val bytes_per_word = w/8

	val io = IO(new Bundle {
		val rocc_req_val = Input(Bool())
		val rocc_req_rdy = Output(Bool())
		val rocc_funct = Input(Bits(2.W))
		val rocc_rs1 = Input(Bits(64.W))
		val rocc_rs2 = Input(Bits(64.W))
		val rocc_rd = Input(Bits(5.W))

		//val dmem_resp_tag = Input(Bits(7.W))

		val busy   = Output(Bool())
		val round  = Output(UInt(5.W))
		val stage  = Output(UInt(log2Ceil(s).W))
		val absorb = Output(Bool())
		val init   = Output(Bool())
		val write  = Output(Bool())
		val aindex = Output(UInt(log2Ceil(round_size_words).W))
		val windex = Output(UInt(log2Ceil(hash_size_words+1).W))

		val buffer_out = Output(Bits(w.W))
	})

	val r_idle :: r_eat_addr :: r_eat_len :: Nil = Enum(3)
	val msg_addr  = RegInit(0.U(64.W))
	val hash_addr = RegInit(0.U(64.W))
	val msg_len   = RegInit(0.U(64.W))

	val busy   = RegInit(false.B)

	val rocc_s = RegInit(r_idle)

	//memory pipe state
	//val dmem_resp_tag_reg = RegNext(io.dmem_resp_tag)
	val fast_mem = p(FastMem)
	val m_idle :: m_read :: m_wait :: m_pad :: m_absorb :: Nil = Enum(5)
	val mem_s = RegInit(m_idle)

	//SRAM Buffer
	val buffer_sram = p(BufferSram)
	val buffer_mem = Mem(round_size_words, UInt(w.W))
	//val buffer_mem = Mem(UInt(w.W), round_size_words, seqRead = true)
	val initValues = Seq.fill(round_size_words) { 0.U(w.W) }
	val buffer = RegInit(VecInit(initValues))
	val writes_done = RegInit(VecInit(Seq.fill(hash_size_words){false.B}))
	
	val buffer_reg_raddr = RegInit(0.U(log2Ceil(round_size_words).W))
	val buffer_waddr = 0.U(w.W)
	val buffer_wdata = 0.U(w.W)
	val buffer_rdata = 0.U(w.W)

	// some flag registers and counters
	val buffer_valid = RegInit(false.B)
	val buffer_count = RegInit(0.U(5.W))
	val areg   = RegInit(false.B) // a flag to indicate if we are doing absorb
	val aindex = RegInit(0.U(log2Ceil(round_size_words).W)) // absorb counter
	val windex = RegInit(0.U(log2Ceil(hash_size_words+1).W))
	val rindex = RegInit((rounds+1).U(5.W)) // round index, a counter for absorb (Max=round_size_words-1)
	val sindex = RegInit(0.U((log2Ceil(s)+1).W)) // stage index, a counter for hash
	val hashed = RegInit(0.U(32.W)) // count how many words in total have been hashed

	val s_idle :: s_absorb :: s_hash :: s_write :: Nil = Enum(4)
	val state = RegInit(s_idle)

	io.rocc_req_rdy := false.B
	io.aindex := RegNext(aindex)
	io.absorb := areg
	areg      := false.B
	io.init   := false.B
	io.busy   := busy
	io.round  := rindex
	io.stage  := sindex
	io.write  := true.B
	io.windex := windex

	val rindex_reg = RegNext(rindex)

	// decode the rocc instruction
	when (rocc_s === r_idle) {
		io.rocc_req_rdy := !busy
		when (io.rocc_req_val && !busy) {
			io.rocc_req_rdy := true.B
			io.busy := true.B
			when (io.rocc_funct === 0.U) {
				msg_addr := io.rocc_rs1
				hash_addr := io.rocc_rs2
			} .elsewhen (io.rocc_funct === 1.U) {
				busy := true.B
				msg_len := io.rocc_rs1
			}
		}
	}
	
	if (buffer_sram) {
		buffer_reg_raddr := aindex
		io.buffer_out := buffer_rdata
	} else {
		io.buffer_out := buffer(io.aindex)
	}

	switch(state) {
		is(s_idle) {
			val canAbsorb = busy && rindex_reg >= rounds.U && buffer_valid && hashed <= msg_len
			when (canAbsorb) {
				busy := true.B
				state := s_absorb
			} .otherwise {
				state := s_idle
			}
		}
		is(s_absorb) {
			io.write := !areg
			areg := true.B
			aindex := aindex + 1.U
			when(io.aindex >= (round_size_words-1).U) {
				aindex := 0.U
				rindex := 0.U
				sindex := 0.U
				areg := false.B
				buffer_valid := false.B
				buffer_count := 0.U
				hashed := hashed + (8*round_size_words).U
				state := s_hash
			} .otherwise {
				state := s_absorb
			}
		}
		is(s_hash) {
			when (rindex <= rounds.U) {
				when (sindex <= s.U) {
					sindex := sindex + 1.U
					io.stage := sindex
					io.round := rindex
					io.write := false.B
					state := s_hash
				} .otherwise {
					sindex := 0.U
					rindex := rindex + 1.U
					io.round := rindex
					io.write := false.B
					state := s_hash
				}
			} .otherwise {
				io.write := true.B
				// TODO: why go back to idle?
				when (hashed < msg_len || (hashed === msg_len && rindex === rounds.U)) {
					windex := 0.U
					state := s_write
				} .otherwise {
					state := s_idle
				}
			}
		}
		is(s_write) {
			when (writes_done.reduce(_&&_)) {
				// all the writes are done
				// reset
				busy := false.B
				//writes_done := Seq.fill(hash_size_words){false.B}
				windex := hash_size_words.U
				rindex := (rounds+1).U
				msg_addr := 0.U
				hash_addr := 0.U
				msg_len := 0.U
				hashed := 0.U
				buffer_valid := false.B
				buffer_count := 0.U
				io.init := false.B
				state := s_idle
			} .otherwise {
				state := s_write
			}
		}
	} // end swith
}

