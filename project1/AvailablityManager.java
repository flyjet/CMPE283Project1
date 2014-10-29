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
	  static String newHostName = "130.65.132.181";  //vHost name
	  static String oldHostName = "130.65.132.184";  

	  public static void main(String[] args) throws Exception {
		
	  vmManager = new VMManager();
	  vhostManager = new VhostManager();
	  
	  /*--- set alarm for user power off VM --*/	  
	  //alarmManager.setPowerOffAlarm();
	  
	  /*--- show VM Statics --*/
	  VMManager.showVMStatics();
	  
	  /*--- run thread for monitor all VMs --*/
	  Monitor monitor = new Monitor();
	  new Thread(monitor).start();		

	  
	  /*--- run thread for snapshot for VMs --*/		
	  //SnapshotManager snapshotManager = new SnapshotManager();
	  //new Thread(snapshotManager).start();
	  
	  
	  /*--- show all live vHost --*/
	  //HostSystem[] liveHost = VhostManager.getLivevHost();
	  //for(int i= 0; i<liveHost.length; i++) {
	  //	  System.out.println("======" + liveHost[i].getName());    		  
	  //}
		
	  /*--- add new vhost --*/	
	  //vhostManager.addHost();
	  
	  /*--- remove vhost --*/	
	  //HostSystem vhost = VhostManager.findVhostByNameInVcenter(oldHostName);
	  //VhostManager.removeHost(vhost);
	
	  /*--- migrate vm to newHost --*/	
	  //vmManager.migrateVMByName(vmname, newHostName);
	}

}
