package edu.sjsu.cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

//This class is the main thread

public class AvailablityManager {
	
	  final static String VCENTERURL = "https://130.65.132.106/sdk";
	  final static String USERNAME = "administrator";
	  final static String PASSWORD = "12!@qwQW";
	
	  public static final String NEWHOSTURL = "130.65.132.185";
	  public static final String NEWHOSTUSERNAME = "root";
	  public static final String NEWHOSTPASSWORD = "12!@qwQW";
	  public static final String NEWHOSTSSLTHUMBPRINT 
	  					= "4C:4C:40:3A:A9:87:67:67:56:EF:92:5B:C4:40:B1:5B:14:83:E8:05";
	  	  
	  private static VMManager vmManager;
	  private static VhostManager vhostManager;
		
	  static String vmname = "T06-VM02-Ubuntu1";
	  static String newHostName = "130.65.132.182";  //vHost name

	  public static void main(String[] args) throws Exception {
		
	  vmManager = new VMManager();
	  vhostManager = new VhostManager();
	  
	  VMManager.showVMStatics();
	  
	  // VirtualMachine vm =VMManager.findVmByNameInVcenter(vmname);
	  //VMManager.PowerOff(vm);
	  //PingManager.pingVM(vm);
	  	  
		// run thread for monitor all VMs
	   // Monitor monitor = new Monitor();
		//new Thread(monitor).start();
		
		//run thread for snapshot for VMs
		//SnapshotManager snapshotManager = new SnapshotManager();
		//new Thread(snapshotManager).start();
		
		//create new vHost
		vhostManager.addHost();
	
	    //vmManager.migrateVMByName(vmname, newHostName);
	  	  
	   // alarmManager.setPowerOffAlarm();
	  
	  
	}

}
