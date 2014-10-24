package edu.sjsu.cmpe283.project1;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;

public class Monitor implements Runnable {
	private static int NUM_OF_RETRY = 6;
	private static int INTERVAL = 10000;
	private static boolean RUNNING = true;
	private static boolean PINGABLE = false;
	
	@Override
	public void run() {
		System.out.println("\n *********Now start monitor all the VMs********");
		try {
			
			while(RUNNING) {

				VirtualMachine[] vms = VMManager.getAllVMs();
				for(VirtualMachine vm : vms) {
					String vmname=vm.getName();
					System.out.println("\n --------------------");
					
					if(PingManager.pingVM(vm)) {
						//ping vm successfully
						System.out.println("Ping " + vmname + " success");
						System.out.println(vmname + " works fine! \n");
					} else {
						System.out.println("Fail to ping the vm " + vmname);
						if(VMManager.isVMPowerOff(vm)) {
							//vm power off
							System.out.println("Power state: " + vm.getRuntime().getPowerState() +"\n");
						}
						else if(VMManager.isVMPowerOn(vm)) {
							//vm power on, ping vm failed, try again
							System.out.println("Power state: " + vm.getRuntime().getPowerState());
							System.out.println("Try to ping " + vmname + " again!");
							for(int i=0; i<NUM_OF_RETRY; i++) {
								Thread.sleep(INTERVAL);
								System.out.println((i+1) + " Ping "+ vmname + ".....");
								if(PingManager.pingVM(vm)){
									PINGABLE = true;
									break;
								}
							}
							if(!PINGABLE) {
								//vm power on, ping vm failed
								System.out.println("Something wrong with " + vmname + "..... \n");																
								Monitor.failOver(vm);
							}
						}
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void failOver(VirtualMachine vm) throws Exception {
		PINGABLE = false;
		String vmname = vm.getName();
		String vhname = VMManager.findVhostNameByVmName(vmname);
		HostSystem vhost = VhostManager.findVhostByNameInVcenter(vhname);
	
		if(!PingManager.pingVM(vm) ){  //Fail to ping the vm
			System.out.println("Try to ping vHost " + vhname + " for vm  " + vmname);			
			if(PingManager.pingVhost(vhost)) {
				System.out.println("Host " + vhname + " work fine, and revert VM " + vmname + " to the lastest Snapshot ");
				VMManager.revertToSnapshotAndPoweron(vm);
			} else {
				System.out.println("Ping vHost " + vhname + " fail and try again.... ");
				for(int i=0; i<NUM_OF_RETRY; i++) {
					System.out.println((i+1) + " Ping "+ vhname + ".....");
					if(PingManager.pingVhost(vhost)){
						PINGABLE = true;
						break;
					}
				}
				if(!PINGABLE){
					
					//???????????
					//Do something.....
				}

			}
		}
		System.out.println();
	}
}