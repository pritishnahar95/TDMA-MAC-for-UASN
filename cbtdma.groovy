/******************************************************************************
Copyright (c) 2016, Pritish Nahar
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import static org.arl.unet.Services.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import org.arl.unet.PDU
import static org.arl.unet.phy.Physical.*
import java.nio.ByteOrder
import org.arl.fjage.Message
import org.arl.fjage.Performative
import org.arl.unet.phy.PhysicalChannelParam 
import java.lang.*
import java.util.Random 
import java.util.Calendar
import org.arl.unet.bb.BasebandParam 


class cbtdma extends UnetAgent {

	final static int TDMA_PROTOCOL = Protocol.USER
	def slots_for_tms	       = new Integer[1]
	def slots_for_data             = new Integer[1]	
	def slen 		       = new Integer[1]
	def node_list                  = new Integer[3][9]
	def cluster_head_list          = new Integer[3]
	def number_of_nodes            = new Integer[3]
	def schedule                   = new Integer[31][61]
	def slot                       = 2.s
	def new_number_of_nodes	       = new Integer[3]
	def cycle_count 	       = new Integer[1]
	def ini_nodes                  = new Integer[1] 
  	def add_nodes	 	       = new Integer[3]
  	def new_cluster_head 	       = new Integer[3]
 	def ch 			       = new Integer[3]
 	def max_number_of_nodes        = new Integer[1]
 	def phase		       = new Integer[1]
 	def is_cluster_head	       = new Integer[1]	
 	def energy_value	       = new Integer[1]
 	def flag 		       = new Integer[1]	
 	def count 		       = new Integer[1]
 	
 	//Constants for Energy calculations
 	def data_rate		       = new Integer[1]
 	def recv_bytes		       = new Integer[1]
 	def send_bytes 		       = new Integer[1]
 	def send_bytes_head	       = new Integer[1]
 	def tms_packet_size	       = new Integer[1]
 	def data_packet_size	       = new Integer[1]
 	def power_level_send	       = new Float[1]
 	def power_level_send_head      = new Float[1]
 	def power_level_recv	       = new Float[1]
 	def send_energy		       = new Float[1]
 	def send_energy_normal	       = new Float[1]
 	def send_energy_head	       = new Float[1] 	
 	def recv_energy		       = new Float[1]
 	def total_energy	       = new Float[1]

 	def stage 		       = new Integer[1]
 	def rxtime 		       = new Long[1]
 	def txtime 		       = new Long[1]

 	def skew 		       = new Float[1]
 	def offset  		       = new Float[1]

 	def A1			       = new Long[1]
 	def A2			       = new Long[1]
 	def A3			       = new Long[1]
 	def A4		               = new Long[1] 	
 	def A5			       = new Long[1]
 	def A6			       = new Long[1]

 	def tms_msg = PDU.withFormat {
		uint32('txtime')
		uint32('rxtime')
		uint8('new_nodes_level_1')
		uint8('new_nodes_level_2')
		uint8('new_nodes_level_3')		
	}



 	def data_msg = PDU.withFormat {
	 	uint32('data')
	}


	public void startup() {
		def phy = agentForService(Services.PHYSICAL)

		InitialiseNumberOfNodes()
		InitialiseNodeList()
		InitialiseClusterHeadList()
		SetSchedule()
		//DisplayLists()
		RunningTDMA()

		subscribe phy
	
	}

	public void InitialiseNodeList()
	{
		
		for(int k = 0; k < 3; k++)
		{
			new_number_of_nodes[k] = 0
			for(int j = 1; j < number_of_nodes[k] + 1; j++)
			{
				node_list[k][j-1] = (k+1)*max_number_of_nodes[0] + j
			}
		}

	 
	}

	public void InitialiseClusterHeadList()
	{
		for(int k = 0; k < 3; k++)
		{
			cluster_head_list[k] = (k+1)*max_number_of_nodes[0]
			new_cluster_head[k]  = 0 
			ch[k] = cluster_head_list[k]-max_number_of_nodes[0]
		}
	}

	public void InitialiseNumberOfNodes()
	{
		tms_packet_size[0] = 11 
		data_packet_size[0] = 11
		data_rate[0] = 300 //bytes per second
		power_level_send[0] = 0.00001585
		power_level_send_head[0]  = 0.316227
		power_level_recv[0] = 0.000001
		
		send_energy[0] = 0
		send_energy_normal[0] = 0
		send_energy_head[0] = 0
		recv_energy[0] = 0
		send_bytes[0] = 0
		send_bytes_head[0] = 0
		recv_bytes[0] = 0
		
		flag[0] = 0
		count[0] = 0
		cycle_count[0] = 0
		ini_nodes[0] = 3
		add_nodes[0] = 3
		add_nodes[1] = 3
		max_number_of_nodes[0] = 10

		stage[0] = 0
		rxtime[0] = 0

		for(int k = 0; k < 3; k++)
		{
		  number_of_nodes[k] = ini_nodes[0]
		}

		slots_for_tms[0]  = 13 + ( max_number_of_nodes[0] - 1 )*4
		slots_for_data[0] = max_number_of_nodes[0] - 1 + 3 
		slen[0] = slots_for_tms[0] + slots_for_data[0] 

	}
	
	public void CalculateEnergy()
	{

		def node = agentForService NODE_INFO

		send_energy_normal[0] = (send_bytes[0] / data_rate[0] ) * (power_level_send[0])
		send_energy_head[0] = (send_bytes_head[0] / data_rate[0] ) * (power_level_send_head[0])
		send_energy[0] = send_energy_head[0] + send_energy_normal[0]
		recv_energy[0] = (recv_bytes[0]/data_rate[0])*power_level_recv[0]
		total_energy[0] = send_energy[0] + recv_energy[0]

	}

	public void SetSchedule()
	{
		int ch_0 = ch[0]
		int ch_1 = ch[1]
		int ch_2 = ch[2]

		schedule[30][1]   = cluster_head_list[0]
 		schedule[30][3]   = cluster_head_list[0]
 		schedule[ch_0][2] = 01 
 		schedule[ch_0][4] = 01 

		schedule[ch_0][5]  = cluster_head_list[1]
		schedule[ch_0][7]  = cluster_head_list[1]	
		schedule[ch_1][6]  = cluster_head_list[0]
		schedule[ch_1][8]  = cluster_head_list[0]
		schedule[ch_1][9]  = cluster_head_list[2]
		schedule[ch_1][11] = cluster_head_list[2]
		schedule[ch_2][10] = cluster_head_list[1]
		schedule[ch_2][12] = cluster_head_list[1]

		int data_slot_cl3 = slen[0]-3 
		int data_slot_cl2 = slen[0]-2
		int data_slot_cl1 = slen[0]-1		 

		schedule[ch_1][data_slot_cl2]  = cluster_head_list[0] 		
		schedule[ch_2][data_slot_cl3]  = cluster_head_list[1] 
		schedule[ch_0][data_slot_cl1]  = 01 	

		for(int k = 0; k < 3; k++)
		{
			for(int j = 0; j < number_of_nodes[k]; j++)
			{
				int clhd = ch[k]
				int nm = node_list[k][j] - max_number_of_nodes[0] 

				def node = agentForService NODE_INFO


				if(nm != clhd)
				{

					int tms1 = 13 + j*4 
					int tms2 = 15 + j*4 
					int tms3 = 14 + j*4 
					int tms4 = 16 + j*4 
			
					int data_slot = slots_for_tms[0] + j

					
					schedule[clhd][tms1]      = node_list[k][j]           //for cluster_head
					schedule[clhd][tms2]      = node_list[k][j] 	 	     //for cluster_head		
					schedule[nm][tms3]        = cluster_head_list[k]      //for normal node 	
					schedule[nm][tms4]        = cluster_head_list[k]      //for normal node 
					schedule[nm][data_slot]   = cluster_head_list[k]
					
				}
				
			}
				
		}	

	}


public void DisplayLists() {

		def node = agentForService Services.NODE_INFO


		if(node.Address == cluster_head_list[0])
		{
		println "------------------"
		println "node Address      : ${node.Address}"
		println "cycle_count       : ${cycle_count[0]}"
		println "cluster_head_list : ${cluster_head_list}"
		println "node_list[0]      : ${node_list[0]}"
		println "node_list[1]      : ${node_list[1]}"
		println "node_list[2]      : ${node_list[2]}"
		println "number of nodes   : ${number_of_nodes}"
		println "------------------"
		//println "${schedule[0]}"
		//println "${schedule[1]}"
		//println "${schedule[30]}"
		////println "${schedule[3]}"

		}
		


	}

public void UpdateNodeLists() {
	
	ch[0] = cluster_head_list[0] - max_number_of_nodes[0]
	ch[1] = cluster_head_list[1] - max_number_of_nodes[0]
	ch[2] = cluster_head_list[2] - max_number_of_nodes[0]

	int check = 0
	for(int k = 0; k < 3; k++)
	{
		if(new_number_of_nodes[k] != number_of_nodes[k])
		{
			check++
		}
	}	
	if(check != 0)
	{
		for (int k = 0; k<30;k++)
		{
			for(int j = 0; j<slen[0]; j++)
			{			
				schedule[k][j]=0
			}
		}
	}	


		for(int k = 0; k < 3; k++)
		{
			def node = agentForService Services.NODE_INFO
		
			int new_position = number_of_nodes[k]
			int start = number_of_nodes[k]+(k+1)*max_number_of_nodes[0] + 1
			int diff = new_number_of_nodes[k]-number_of_nodes[k]
			number_of_nodes[k]=new_number_of_nodes[k]
			for(int j = 1; j <= diff; j++)
			{
				//if(node.Address == 10) ////println ("node_list: ${node_list[k]}")
				node_list[k][new_position] = start
				new_position = new_position + 1	
				start = start + 1			
			}

		}

	}


	public void ChangeClusterHead()
	{
		new_cluster_head[0] = 0
		new_cluster_head[1] = 0
		new_cluster_head[2] = 0

		if(cycle_count[0] == 4)
		{
		new_cluster_head[0] = 11
		new_cluster_head[1] = 21
		new_cluster_head[2] = 31
		}

		if(cycle_count[0] == 7)
		{
		new_cluster_head[0] = 12
		new_cluster_head[1] = 22
		new_cluster_head[2] = 32
		}

		int check_2 = 0
		for(int k = 0; k < 3; k++)
		{
			if(new_cluster_head[k])
			{
				check_2 = check_2 + 1

				int no = number_of_nodes[k]
				def temp_array = new Integer[no]


				for(int w = 0; w < number_of_nodes[k]; w++)
				{
					if(node_list[k][w] == new_cluster_head[k])
					{
						temp_array[w] = cluster_head_list[k]
					}
					else
					{
						temp_array[w] = node_list[k][w]
					}
				}
				temp_array.sort()

				for(int q = 0; q<number_of_nodes[k]; q++)
				{
					node_list[k][q] = temp_array[q]
				}

				cluster_head_list[k] = new_cluster_head[k]
			}
		}
		if(check_2 != 0)
		{
			for (int x = 0; x<30;x++)
			{
				for(int y = 0; y<slen[0]; y++)
				{			
					schedule[x][y]=0
				}
			}

			UpdateNodeLists()
			SetSchedule()
		}



	}	


	public void CalculateSkewOffset()
	{

		def node = agentForService Services.NODE_INFO
		
		skew[0]   = (A6[0] - A2[0])/(A5[0] - A1[0])
		offset[0] = ( ( (A2[0] + A3[0])*0.5 ) - ( (A1[0] + A4[0]) * skew[0] * 0.5) )

		//println "node : ${node.Address} A1[0] = ${A1[0]} A2[0] = ${A2[0]} A3[0] = ${A3[0]} A4[0] = ${A4[0]} A5[0] = ${A5[0]} A6[0] = ${A6[0]}"
		//println " node : ${node.Address} skew : ${skew[0]}, offset : ${offset[0]}"
	}
	
	public void RunningTDMA()
	{
		def phy  = agentForService PHYSICAL
		def node = agentForService Services.NODE_INFO
		
//Timer starts here
		add new TickerBehavior(1000*slot, {

		int sch_pos = node.Address-max_number_of_nodes[0]
 
		if(node.Address == 01)
		{
			sch_pos = 30
		}

		def s = schedule[sch_pos][(tickCount-1)%slen[0]]
		if(node.Address == 10)
		{
			//println "tickCount = ${tickCount}"			
		}

		phase[0] = (tickCount-1) % slen[0]


//for adding nodes dynamically
		if( phase[0] == 0)
		{

			cycle_count[0] = cycle_count[0] + 1			

			ParameterReq req1 = new ParameterReq(agent.agentForService(Services.PHYSICAL))
			req1.get(PhysicalParam.time)
			ParameterRsp rsp1 = (ParameterRsp) agent.request(req1, 1000)			
			long time = rsp1.get(PhysicalParam.time)
			println "node : ${node.Address} time = ${time}"


			if(node.Address == cluster_head_list[0])
			{
				ParameterReq req3 = new ParameterReq(agent.agentForService(Services.BASEBAND))
				req3.get(BasebandParam.preambleDuration)				
				ParameterRsp rsp3 = (ParameterRsp) agent.request(req3)	
				println "rsp3 = ${rsp3}"
							
				ParameterReq req2 = new ParameterReq(agent.agentForService(Services.PHYSICAL));
				req2.get(PhysicalParam.propagationSpeed);
				ParameterRsp rsp2 = (ParameterRsp) agent.request(req2, 1000);				
				println "propogation speed = ${rsp2}"

			}


			if((node.Address==cluster_head_list[0] || node.Address==cluster_head_list[1] || node.Address==cluster_head_list[2] || node.Address == 01))
			{
				

				if(cycle_count[0] == 2)
				{
					new_number_of_nodes[0] = ini_nodes[0]+add_nodes[0]
					new_number_of_nodes[1] = ini_nodes[0]+add_nodes[0]
					new_number_of_nodes[2] = ini_nodes[0]+add_nodes[0]		
					UpdateNodeLists()
					SetSchedule()
				}

				if(cycle_count[0] == 3)
				{
					new_number_of_nodes[0] = ini_nodes[0]+add_nodes[0]+add_nodes[1]
					new_number_of_nodes[1] = ini_nodes[0]+add_nodes[0]+add_nodes[1]
					new_number_of_nodes[2] = ini_nodes[0]+add_nodes[0]+add_nodes[1]		
					UpdateNodeLists()
					SetSchedule() 
				}
			}		
		}

// for setting appropriate power levels
		if((node.Address == cluster_head_list[0]) || (node.Address ==  cluster_head_list[1]) || (node.Address == cluster_head_list[2] || node.Address == 01))
		{ 
			if((s == cluster_head_list[0]) || (s == cluster_head_list[1]) || (s == cluster_head_list[2] || s == 01))
			{
				phy[1].powerLevel   =  -5.dB
			}
			else
			{
				phy[1].powerLevel   =  -48.dB
			} 

		}
		else
		{
			phy[1].powerLevel   =  -48.dB
		}

//if control or tms phase
		if( phase[0] < slots_for_tms[0] )
		{

			ParameterReq req = new ParameterReq(agent.agentForService(Services.PHYSICAL))
			req.get(PhysicalParam.time)
			ParameterRsp rsp = (ParameterRsp) agent.request(req)			
			txtime[0] = rsp.get(PhysicalParam.time)

			if( cycle_count[0] == 2 )
			{
				if (s) phy << new TxFrameReq(to: s, type: Physical.DATA, data : tms_msg.encode([txtime : txtime[0], rxtime : rxtime[0], new_nodes_level_1 : new_number_of_nodes[0], new_nodes_level_2 : new_number_of_nodes[1], new_nodes_level_3 : new_number_of_nodes[2] ]))
			}
			else if( cycle_count[0] == 3 ) 
			{
				if (s) phy << new TxFrameReq(to: s, type: Physical.DATA, data : tms_msg.encode([txtime : txtime[0], rxtime : rxtime[0], new_nodes_level_1 : new_number_of_nodes[0], new_nodes_level_2 : new_number_of_nodes[1], new_nodes_level_3 : new_number_of_nodes[2] ]))
			}
			else
			{
				if (s) phy << new TxFrameReq(to: s, type: Physical.DATA, data : tms_msg.encode([txtime : txtime[0], rxtime : rxtime[0], new_nodes_level_1 : number_of_nodes[0], new_nodes_level_2 : number_of_nodes[1], new_nodes_level_3 : number_of_nodes[2]]))
			}

//for setting A3 value 
			if(node.Address != cluster_head_list[0] || node.Address != cluster_head_list[1] || node.Address != cluster_head_list[2])
			{
				if(stage[0] == 1)
				{		
					A3[0] = txtime[0]
					////println "in stage condition , txtime = ${txtime[0]}"
					stage[0] = 2							
				}
			}

			if(phase[0] == 6 && node.Address == cluster_head_list[1])
			{
				A3[0] = txtime[0]
			}
			if(phase[0] == 10 && node.Address == cluster_head_list[2])
			{
				A3[0] = txtime[0]
			}			


//for counting appropriate send bytes
			if(node.Address == cluster_head_list[0] || node.Address == cluster_head_list[1] || node.Address == cluster_head_list[2])
			{
				if(s == cluster_head_list[0] || s == cluster_head_list[1] || s == cluster_head_list[2] || s == 01)
				{
					send_bytes_head[0] = send_bytes_head[0] + tms_packet_size[0]
				}
				else 
				{
					if(s!= null && s!=0)
					{		
						send_bytes[0] = send_bytes[0] + tms_packet_size[0]
					}
				}
			}	
			else
			{

				if((s == cluster_head_list[0]) || (s == cluster_head_list[1]) || (s == cluster_head_list[2]))
				{
					if(cycle_count == 1)
					{
						if(node.Address < (10 + ini_nodes[0] + 1))
						{
							send_bytes[0] = send_bytes[0] + tms_packet_size[0]
						}
						if(node.Address < (20 + ini_nodes[0] + 1) && node.Address > 19)
						{
							send_bytes[0] = send_bytes[0] + tms_packet_size[0]
						}
						if(node.Address < (30 + ini_nodes[0] + 1) && node.Address > 29) 
						{
							send_bytes[0] = send_bytes[0] + tms_packet_size[0]
						}

					}
					else if(cycle_count[0] == 2)
					{
						if(node.Address < (10 + new_number_of_nodes[0] + 1))
						{
							send_bytes[0] = send_bytes[0] + tms_packet_size[0]
						}
						if(node.Address < (20 + new_number_of_nodes[1] + 1)  && node.Address > 19)
						{
							send_bytes[0] = send_bytes[0] + tms_packet_size[0]
						}
						if(node.Address < (30 + new_number_of_nodes[2] + 1)&& node.Address > 29)
						{
							send_bytes[0] = send_bytes[0] + tms_packet_size[0]
						}

					}
					else
					{
						send_bytes[0] = send_bytes[0] + tms_packet_size[0]
					}
				}	
			}
		}
		
//else data phase
		else
		{

			ParameterReq req = new ParameterReq(agent.agentForService(Services.PHYSICAL))
			req.get(PhysicalParam.time)
			ParameterRsp rsp = (ParameterRsp) agent.request(req)			
			txtime[0] = rsp.get(PhysicalParam.time)

			if (s) phy << new TxFrameReq(to: s, type: Physical.DATA, data : data_msg.encode([data : 99 ]))

			if(s == cluster_head_list[0] || s == cluster_head_list[1] || s == cluster_head_list[2] || s == 01)
			{

				if(node.Address == cluster_head_list[0] || node.Address == cluster_head_list[1] || node.address == cluster_head_list[2])
				{
					send_bytes_head[0] = send_bytes_head[0] + data_packet_size[0]
				}
				else
				{
					if(cycle_count == 1)
					{
						if(node.Address < (10 + ini_nodes[0] + 1))
						{
							send_bytes[0] = send_bytes[0] + data_packet_size[0]
						}
						if(node.Address < (20 + ini_nodes[0] + 1) && node.Address > 19)
						{
							send_bytes[0] = send_bytes[0] + data_packet_size[0]
						}
						if(node.Address < (30 + ini_nodes[0] + 1) && node.Address > 29) 
						{
							send_bytes[0] = send_bytes[0] + data_packet_size[0]
						}

					}
					
					else if(cycle_count[0] == 2)
					{
						if(node.Address < (10 + new_number_of_nodes[0] + 1))
						{
							send_bytes[0] = send_bytes[0] + data_packet_size[0]
						}
						if(node.Address < (20 + new_number_of_nodes[1] + 1)  && node.Address > 19)
						{
							send_bytes[0] = send_bytes[0] + data_packet_size[0]
						}
						if(node.Address < (30 + new_number_of_nodes[2] + 1) && node.Address > 29)
						{
							send_bytes[0] = send_bytes[0] + data_packet_size[0]
						}
					}
					
					else
					{
						send_bytes[0] = send_bytes[0] + data_packet_size[0]
					}

				}	
				//}
			}

//for energy calculations at the end of the cycle as well as cycle incremement
		if(phase[0] == slen[0]-1)
		{

			CalculateEnergy()
			DisplayLists()
			ChangeClusterHead()

//			cycle_count[0] = cycle_count[0] + 1

			reset()
		}
	}
		
	})
	
	}

 public Message processRequest(Message msg) {

	}

 void processMessage(Message msg) {
////println "Checkpoint Process Message"
		def phy = agentForService Services.PHYSICAL
		def node = agentForService Services.NODE_INFO

		if(msg instanceof TxFrameNtf)
		{

		long ttime = msg.getTxTime().toLong()
		long delay = ttime - txtime[0]

		println "node : ${node.Address} delay : ${delay} ttime : ${ttime} txtime : ${txtime[0]}"		 

		}

	 
	   if(msg instanceof RxFrameNtf)
	   {

			rxtime[0] = msg.getRxTime().toLong()
			

			if(phase[0] < slots_for_tms[0])
			{
				def pkt = tms_msg.decode(msg.data)
				
				println "cycle : ${cycle_count[0]} phase : ${phase[0]} source = ${msg.getFrom()} and destination = ${msg.getTo()} with txtime : ${pkt.txtime} and rxtime : ${rxtime[0]}"
				
				recv_bytes[0] = recv_bytes[0] + tms_packet_size[0]

				if(node.Address!=cluster_head_list[0] && node.Address!=cluster_head_list[1] && node.Address!=cluster_head_list[2] )
				{				
					new_number_of_nodes[0] = pkt.new_nodes_level_1
					new_number_of_nodes[1] = pkt.new_nodes_level_2	
					new_number_of_nodes[2] = pkt.new_nodes_level_3
					UpdateNodeLists()
					SetSchedule()

					if(stage[0] == 0)
					{
						A1[0] = pkt.txtime
						A2[0] = rxtime[0]
						stage[0] = 1
					}
					else if(stage[0] == 2)
					{
						A5[0] = pkt.txtime
						A4[0] = pkt.rxtime
						A6[0] = rxtime[0]
						stage[0] = 0

						CalculateSkewOffset()

					}
					else
					{
						////println "nothing, do nothing"
					}

				}


				if(phase[0] < 5)
				{
					if(node.Address == cluster_head_list[0])
					{
						if(stage[0] == 0)
						{
							A1[0] = pkt.txtime
							A2[0] = rxtime[0]
							stage[0] = 1
						}
						else if(stage[0] == 2)
						{
							A5[0] = pkt.txtime
							A4[0] = pkt.rxtime
							A6[0] = rxtime[0]
							stage[0] = 0

							CalculateSkewOffset()
						}
						else
						{
							////println "do nothing"
						}
					}	
				}


				if(phase[0] > 4 && phase[0] < 9)
				{
					if(node.Address == cluster_head_list[1])
					{
						if(stage[0] == 0)
						{
							A1[0] = pkt.txtime
							A2[0] = rxtime[0]
							stage[0] = 1
						}
						else if(stage[0] == 2)
						{
							A5[0] = pkt.txtime
							A4[0] = pkt.rxtime
							A6[0] = rxtime[0]
							stage[0] = 0

							CalculateSkewOffset()
						}
						else
						{
							////println "do nothing"
						}
					}	
				}

				if(phase[0] > 8 && phase[0] < 13)
				{
					if(node.Address == cluster_head_list[2])
					{
						if(stage[0] == 0)
						{
							A1[0] = pkt.txtime
							A2[0] = rxtime[0]
							stage[0] = 1
						}
						else if(stage[0] == 2)
						{
							A5[0] = pkt.txtime
							A4[0] = pkt.rxtime
							A6[0] = rxtime[0]
							stage[0] = 0

							CalculateSkewOffset()
						}
						else
						{
							////println "do nothing"
						}
					}					
				}

			}

			else
			{
				def pkt = data_msg.decode(msg.data)

				recv_bytes[0] = recv_bytes[0] + data_packet_size[0]

				if(phase[0] == 59 && node.Address == 10)
				{
					recv_bytes[0] = recv_bytes[0] + data_packet_size[0] 
				}

				println "cycle : ${cycle_count[0]} phase : ${phase[0]} source = ${msg.getFrom()} and destination = ${msg.getTo()} with data : ${pkt.data} "
			}
		}	
	}
}		


