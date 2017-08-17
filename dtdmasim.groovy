//! Simulation: Aloha wireless network
///////////////////////////////////////////////////////////////////////////////
/// 0.013 + 
/// To run simulation:
///   bin/unet samples/aloha/aloha
///
/// Output trace file: logs/trace.nam
/// Plot results: bin/unet samples/aloha/plot-results
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*
import static org.arl.unet.phy.Physical.*





///////////////////////////////////////////////////////////////////////////////
// settings

def nodes    =     1..30
def slot     =       2.s         // default packet length is about 345 ms
def hrange   =      20.m
def vrange   =	   500.m         // about slot x 1540 m/s
def vrange1  =  vrange*1         // about slot x 1540 m/s
def vrange2  =  vrange*2         // about slot x 1540 m/s
def vrange3  =  vrange*3         // about slot x 1540 m/s
def time     =     900.s         // simulation time
trace.warmup = 1.minutes

///////////////////////////////////////////////////////////////////////////////
// display documentation

println"""
Cyclindrical Column network
----------------------------

Circle Radius:          ${hrange} m
Slot length:            ${(1000*slot).round()} ms
Simulation time:        ${time} s"""

channel.model 		     = BasicAcousticChannel

channel.carrierFrequency = 25.kHz       	// f
channel.bandwidth        = 4096.Hz              // B
channel.spreading        = 2                    // α
channel.temperature      = 25.C              	// T
channel.salinity         = 35.ppt               // S
channel.noiseLevel       = 60.dB              	// N0
channel.waterDepth       = 1000.m               // d


modem.model        		= org.arl.unet.sim.HalfDuplexModem
modem.dataRate     		= [0, 2400].bps
modem.frameLength  		= [0, 11].bytes
modem.clockOffset 		= 0.s
 
//modem.powerLevel = [-20.dB, -30.dB]
//def p = agentForService PHYSICAL
//p[1].powerLevel  =  50.dB

//////////////////////////////////////////////////////////////////////////////
// simulate schedule

simulate time, {

	def n = []

 //for (int me: nodes) {                   //   with randomly placed nodes
  //    node "$me", location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1]}

	n << node('10', address: 01, location: [0,0,0], stack: { container -> container.add 'cbtdma', new cbtdma()})

//Cluster1
	n << node('100', address: 10, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})
	n << node('101', address: 11, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('102', address: 12, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('103', address: 13, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})//, shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('104', address: 14, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('105', address: 15, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})
	n << node('106', address: 16, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('107', address: 17, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('108', address: 18, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})//, shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('109', address: 19, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange1], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})



//Cluster 2
	n << node('200', address: 20, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('201', address: 21, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('202', address: 22, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('203', address: 23, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})//, shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('204', address: 24, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('205', address: 25, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('206', address: 26, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('207', address: 27, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('208', address: 28, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})//, shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('209', address: 29, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange2], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})




//Cluster 3
	n << node('300', address: 30, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('301', address: 31, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('302', address: 32, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('303', address: 33, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})//, shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('304', address: 34, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('305', address: 35, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('306', address: 36, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('307', address: 37, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('308', address: 38, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})//, shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})
	n << node('309', address: 39, location: [rnd(-hrange, hrange), rnd(-hrange, hrange), vrange3], stack: { container -> container.add 'cbtdma', new cbtdma()})//,shell: GUI, stack: { container -> container.shell.addInitrc "${script.parent}/fshrc.groovy"})


}

// display statistics


println """
TX: 			${trace.txCount}
RX:                     ${trace.rxCount}
Offered load:           ${trace.offeredLoad.round(3)}
Throughput:             ${trace.throughput.round(3)}"""
