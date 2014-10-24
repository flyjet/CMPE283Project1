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

public class PingTest{
	
  private final static String VCENTERURL = "https://130.65.132.106/sdk";
  private final static String USERNAME = "administrator";
  private final static String PASSWORD = "12!@qwQW";
	
	//String vmname = "T06-VM02-Ubuntu1";
	//String newHostName = "130.65.132.184";  //vHost name
	
  public static void main(String[] args) throws Exception
  {  
	ServiceInstance si = new ServiceInstance(new URL(VCENTERURL), USERNAME, PASSWORD, true);
	Folder rootFolder = si.getRootFolder();	
	ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
	if(hosts==null || hosts.length==0) {
		return;
	}
	for(int i=0; i<hosts.length; i++){     //get all vm in Vhost
		System.out.println("vHost loop ******************");
		System.out.println("HostName: " + hosts[i].getName());  
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		for(int j=0; j<mes.length; j++) {
			VirtualMachine vm = (VirtualMachine) mes[j];
			VirtualMachineConfigInfo vminfo = vm.getConfig();
			vm.getResourcePool();
			System.out.println("vHost: " + vm.getParent().getName());
			System.out.println("Guest: " + vm.getName());
			System.out.println("GuestOS: " + vminfo.getGuestFullName());
			System.out.println("GuestState: " + vm.getGuest().guestState);
			System.out.println("Guest Ip address: " + vm.getGuest().getIpAddress());
			
			pingByIP(vm.getGuest().getIpAddress());	  //ping VM		
		}	
		
		HostSystem newHost = (HostSystem) hosts[i];
		String vHost_IP = newHost.getConfig().getNetwork().getVnic()[0].getSpec().getIp().getIpAddress();
		System.out.println("vHost IP address: " + vHost_IP);  
		
		// test for ping vHost
		pingByIP(vHost_IP);		//ping vHost
	}	
  } //end of main
  
  
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
	}else{
		System.out.println("ping " + ip + "  not success");
	}
	return process.exitValue() == 0;	
  } 
  
	public static boolean pingVM (VirtualMachine vm) throws Exception{
		String ip=vm.getGuest().getIpAddress();
		return pingByIP(ip);
	}
	
	public static boolean pingVhost (HostSystem vhost) throws Exception{
		String ip=vhost.getConfig().getNetwork().getVnic()[0].getSpec().getIp().getIpAddress();
		return pingByIP(ip);
	}
  
}
