package edu.sjsu.cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author Qi Cao
 */

public class PingAndSnpashot_test{
	
  private final static String VCENTERURL = "https://130.65.132.106/sdk";
  private final static String USERNAME = "administrator";
  private final static String PASSWORD = "12!@qwQW";
	
  static String vmname = "T06-VM02-Ubuntu1";
  static String newHostName = "130.65.132.184";  //vHost name
	
  public static void main(String[] args) throws Exception
  {  
	ServiceInstance si = new ServiceInstance(new URL(VCENTERURL), USERNAME, PASSWORD, true);
	
	Folder rootFolder = si.getRootFolder();		
	VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).
			searchManagedEntity("VirtualMachine", vmname);	
		
	checkAllVMRunningState(si);
	checkAllVhostState(si);
	
	//test for VM Snapshot
	//createVMSnapshot(vm);
	
	//test for VM recover from snapshot
	//revertToSnapshotAndPoweron(vm);
	
	//test for find vHost for target vm
	findVhostNameByVmName(vmname, si);
    
		
  } //end of main
  
  
  //********* check all VMs running State *********//
    public static void checkAllVMRunningState(ServiceInstance si )throws Exception {
    	Folder rootFolder = si.getRootFolder();	
    	ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
    	for(int j=0; j<mes.length; j++) {
    		VirtualMachine vm = (VirtualMachine) mes[j];
    		VirtualMachineConfigInfo vminfo = vm.getConfig();
    		
    		vm.getResourcePool();
    		System.out.println("vHost: " + vm.getParent().getName());    //??? 
    		System.out.println("Guest: " + vm.getName());
    		System.out.println("GuestOS: " + vminfo.getGuestFullName());
    		System.out.println("GuestState: " + vm.getGuest().guestState);
    		System.out.println("Guest Ip address: " + vm.getGuest().getIpAddress());
   		
    		pingByIP(vm.getGuest().getIpAddress());	  //ping all VMs	
    	}		    	    	
    }
  
  //********* check all vHost and ping **********//
    public static void checkAllVhostState(ServiceInstance si )throws Exception {
    	Folder rootFolder = si.getRootFolder();	
    	ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
    	if(hosts==null || hosts.length==0) {
    		return;
    	}
    	for(int i=0; i<hosts.length; i++){     //get all vHost to ping
    		HostSystem newHost = (HostSystem) hosts[i];
    		String vHost_IP = newHost.getConfig().getNetwork().getVnic()[0].getSpec().getIp().getIpAddress();
    		System.out.println("vHost IP address: " + vHost_IP); 
    		System.out.println("HostName: " + hosts[i].getName());  
    		
    		pingByIP(vHost_IP);		//ping vHost
    	}	     	
    }
  
  //********* create snapshot ***********//  
	public static void createVMSnapshot(VirtualMachine vm) throws Exception {
		// create a snapshot for selected virtual machine
		// precondition:  vm is normal and could be ping through
		
		System.out.println("Trying to create snapshot for " + vm.getName() + " now...");
		
		//first make sure vm could be ping through
		if( !pingByIP(vm.getGuest().getIpAddress())) {
	   		 System.out.println("could not ping through VM, it's abnormal, will not create snapshot");
	   	     return;		 
	   	 }
		
		String snapshotname = vm.getName() + "_SnapShot";
		String description = "new snapshot of " + vm.getName();
		
		Task task = vm.createSnapshot_Task(snapshotname, description, false, false);
		System.out.println("creating " + snapshotname + " now....");
		if (task.waitForTask() == Task.SUCCESS){
			System.out.println(snapshotname + " was created.");
		}else{
			System.out.println(snapshotname + " create failed.");
		}			
	}
  
  
  //*******  Ping vHost or VM by IP address
  public static boolean pingByIP(String ip) throws Exception {
	String cmd = "";
	if (System.getProperty("os.name").startsWith("Windows")) {
		// For Windows
		cmd = "ping -n 1 " + ip;
	} else {
		// For Linux and OSX
		cmd = "ping -c 1 " + ip;
	}
	Process process = Runtime.getRuntime().exec(cmd);
	process.waitFor();	
	if(process.exitValue() == 0){
		System.out.println("ping " + ip + " success");
		System.out.println("=======================");
		return true;
	}else{
		System.out.println("ping " + ip + "  not success");
		System.out.println("-----------------------");
		return false;
	}	
  } 
  
  //******* recover the virtual machine from snapshot and power on
    
	public static void revertToSnapshotAndPoweron(VirtualMachine vm) throws Exception {
		
		  Task revertTask = vm.revertToCurrentSnapshot_Task(null);
		  System.out.println("\n Trying to revert " + vm.getName() + " to snapshot...." );
		  if (revertTask.waitForTask() == Task.SUCCESS) {
			  System.out.println("VM "+vm.getName()+" has been reverted to recent snapshot.");
		  }			 						
		  else {
			  System.out.println("fail to recover VM "+vm.getName());
		  }	
		  
		  Task task = vm.powerOnVM_Task(null);
		  task.waitForTask();
		  System.out.println("\n vm:" + vm.getName() + " powered on.");
		  
		  // need to check VM IP configurable	

	}	
	
	//******* need find which vHost the VM belongs to
	
	public static String findVhostNameByVmName(String vmname, ServiceInstance si) throws Exception{
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
    		System.out.println("HostName: " + hosts[i].getName());     		
    	}
    	return vHostName;
	}	  
}
