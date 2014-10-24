package edu.sjsu.cmpe283.project1;


import com.vmware.vim25.mo.VirtualMachine;

public class SnapshotManager implements Runnable {

	private static boolean RUNNING = true;
	private static int INTERVAL = 600000;
	
	@Override
	public void run() {
		System.out.println("\n *********Now snapshot VMs start********");
		try {
			while(RUNNING) {
				//VMManager.showVMStatics();				
				//Test.setPowerOffAlarm();				
				VirtualMachine[] vms = VMManager.getAllVMs();
				for(VirtualMachine vm : vms) {
					if(PingManager.pingVM(vm)) {
						VMManager.createVMSnapshot(vm);
					}	
				}
				System.out.println("\n Finish snapshot of all live VMs!\n");
				Thread.sleep(INTERVAL);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}