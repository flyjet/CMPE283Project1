package edu.sjsu.cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineQuickStats;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VMManager {
	
	private static ServiceInstance si;
	private static int NUM_OF_RETRY = 20;
	
	public VMManager() throws Exception {
		URL url = new URL(AvailablityManager.VCENTERURL);
		si = new ServiceInstance(url, AvailablityManager.USERNAME, AvailablityManager.PASSWORD, true);
	}
	
	//********** VM power on ****************//	
	public static boolean PowerOn(VirtualMachine vm) throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println("Power On " + vm.getName() + " in process..." );
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(vm.getName() + " is powering on.");
			return true;
		}else {
			System.out.println(vm.getName() + " failed to power on");
			return false;
		}
	}
	
	
	//********** VM power off ****************//	
	public static boolean PowerOff(VirtualMachine vm) throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println("Power Off " + vm.getName() + " in process..." );
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(vm.getName() + " is powering off.");
			return true;
		}else {
			System.out.println(vm.getName() + " failed to power off");
			return false;
		}
	}
	
	
	//********** check if VM power on  ****************//	
	public static boolean isVMPowerOn(VirtualMachine vm) throws Exception {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		return vmps == VirtualMachinePowerState.poweredOn;
	}
	
	
	//********** check if VM power off  ****************//	
	public static boolean isVMPowerOff(VirtualMachine vm) throws Exception {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		return vmps == VirtualMachinePowerState.poweredOff;
	}

	
	//********* check VM IP configure ************//
	public static boolean vmIpConfigure(VirtualMachine vm) throws Exception{
		boolean PINGABLE =false;
		for(int i=0; i<NUM_OF_RETRY; i++) {
			if(PingManager.pingVM(vm)){
				PINGABLE = true;
				break;
			}
		}
		return PINGABLE;

	}
	
	
	//******* find all VMs ***********//
	public static VirtualMachine[] getAllVMs() throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		if(entities==null || entities.length==0) {
			System.out.println("\n Cannot find any virtual machine!");
			return null;
		}
		VirtualMachine[] virtualMachines = new VirtualMachine[entities.length];
		for(int i=0; i<entities.length; i++) {
			virtualMachines[i] = (VirtualMachine) entities[i];
		}
		return virtualMachines;
	}

	//******* find all vHosts ***********//
	public static HostSystem[] getAllHosts() throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(entities==null || entities.length==0) {
			System.out.println("\n Cannot find any hostsystem!");
			return null;
		}
		HostSystem[] hostSystems = new HostSystem[entities.length];
		for(int i=0; i<entities.length; i++) {
			hostSystems[i] = (HostSystem) entities[i];
		}
		return hostSystems;
	}
	
	
	//******* migrate VM by name ***********//
	public static void migrateVMByName(String vMachine, String newHost) throws Exception {
		VirtualMachine vm = findVmByNameInVcenter(vMachine);
		HostSystem hs = VhostManager.findVhostByNameInVcenter(newHost);
		ComputeResource cr = (ComputeResource) hs.getParent();
		Task task = vm.migrateVM_Task(cr.getResourcePool(), hs, VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOff);
		System.out.println("Try to migrate " + vm.getName() + " to " + hs.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("\n Migrate virtual machine: " + vm.getName() + " successfully!");
		} else {
			System.out.println("\n Migrate vm failed!");
		}
	}
	
	
	//******* find VM by name ***********//
	public static VirtualMachine findVmByNameInVcenter(String vmname) throws Exception{
		Folder rootFolder = si.getRootFolder();	
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).
				searchManagedEntity("VirtualMachine", vmname);
		if (vm== null)  throw new Exception("vm is not found");
		return vm;
	}
		
	//******* find vHost name by VM name  ***************//	
	public static String findVhostNameByVmName(String vmname) throws Exception{
		String vHostName = null;
		Folder rootFolder = si.getRootFolder();	
    	ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
    	if(hosts==null || hosts.length==0) {
    		System.out.println("Did not find vHost");    
    		return vHostName;
    	}
    	for(int i=0; i<hosts.length; i++){     //get all vHost 
    		HostSystem newHost = (HostSystem) hosts[i];
    		VirtualMachine[] vms=newHost.getVms();
    		for(int j=0; j<vms.length; j++)
    		{
    			String vmName =vms[j].getName();
    			//System.out.println("vms " + j + "  "+ vmName);     			
    			if (vmName.equals(vmname) ){
    				vHostName = hosts[i].getName();   
    				System.out.println("Find vHost for vm name " + vmname + ", vHost is " + vHostName);    
    				return vHostName;   				
    			}
    		}   		 		
    	}
    	return vHostName;
	}	
	
	
	//******* Print all vHost and vms statics  ***************//	
	public static void showVMStatics()  throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(hosts==null || hosts.length==0) {
			return;
		}
		
		System.out.println("***********VHost Statics**************");
		for(int h=0; h<hosts.length; h++) {
			System.out.println("Host IP " + (h+1) + ": "+ hosts[h].getName());
			
		}
		System.out.println("***********End VHost******************");
		
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

		for(int m=0; m<mes.length; m++) {		
			VirtualMachine vm = (VirtualMachine) mes[m];
			String alarmColor = vm.getSummary().getOverallStatus().toString();
			
			
			System.out.println("\n--------------VM Statics--------------");
			System.out.println("VM " + (m+1) );
			System.out.println("Name: " + vm.getName());
			System.out.println("Guest OS: "+ vm.getSummary().getConfig().guestFullName);
			System.out.println("VM Version: " + vm.getConfig().version);
			System.out.println("CPU: " + vm.getConfig().getHardware().numCPU+ " vCPU");
			System.out.println("Memory: " + vm.getConfig().getHardware().memoryMB + " MB");
			System.out.println("IP Addresses: " + vm.getGuest().getIpAddress());
			System.out.println("Running State: " + vm.getGuest().guestState);
			System.out.println("Over stauts: " + alarmColor );
			if(alarmColor.equals("yellow")){
				System.out.println(vm.getName() + " is powered off by user." );
			}
			
			System.out.println("\nData from VirtualMachineQuickStats: ");
			VirtualMachineQuickStats vqs = vm.getSummary().getQuickStats();
			System.out.println( "OverallCpuUsage: " + vqs.getOverallCpuUsage() + " MHz");
			System.out.println( "GuestMemoryUsage: " + vqs.getGuestMemoryUsage() + " MB");
			System.out.println( "ConsumedOverheadMemory: " + vqs.getConsumedOverheadMemory() + " MB");
			System.out.println( "FtLatencyStatus: " + vqs.getFtLatencyStatus());
			System.out.println( "GuestHeartbeatStatus: " + vqs.getGuestHeartbeatStatus());
			
			System.out.println( "End ---------------------- ");
		}	
	}
	
	
	  //********* create snapshot ***********//  
	public static void createVMSnapshot(VirtualMachine vm) throws Exception {
		// create a snapshot for selected virtual machine
		// precondition:  vm is normal, can be ping
				
		System.out.println("\n Trying to create snapshot for " + vm.getName());
				
		//first check the vm can be ping
		if( !PingManager.pingVM(vm)) {
			  System.out.println("\n Can not ping VM, it's abnormal, will not create snapshot");
		   	  return;		 
		}
			
		String snapshotname = vm.getName() + "_lastest_SnapShot";
		String description = "new snapshot of " + vm.getName();
			
		Task task = vm.createSnapshot_Task(snapshotname, description, false, false);	
			//?? how to remove old snapshot		
		System.out.println("creating " + snapshotname + " now");
		if (task.waitForTask() == Task.SUCCESS){
			System.out.println("\n"+ snapshotname + " was created.");
			System.out.println("-----------------");
		}else{
			System.out.println("\n" + snapshotname + " create failed.");
			System.out.println("-----------------");
		}			
	}
		
	
		//******* revert the virtual machine from snapshot and power on	    
	public static void revertToSnapshotAndPoweron(VirtualMachine vm) throws Exception {
			
		Task revertTask = vm.revertToCurrentSnapshot_Task(null);
		System.out.println("\n Trying to revert " + vm.getName() + " to lastest snapshot" );
		if (revertTask.waitForTask() == Task.SUCCESS) {
			System.out.println("\n VM "+vm.getName()+" has been reverted to lastest snapshot.");
		}else {
			System.out.println("\n fail to recover VM "+vm.getName());
		}	
			  
		Task task = vm.powerOnVM_Task(null);
		task.waitForTask();
		System.out.println("\n vm:" + vm.getName() + " powered on.");
			  
		if (VMManager.vmIpConfigure(vm))		
			System.out.println(vm.getName() + "ip is configured. ");
		}		
		
}
