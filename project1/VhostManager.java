package edu.sjsu.cmpe283.project1;



import java.net.URL;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;


public class VhostManager {
	
	private static ServiceInstance si;
	
	public VhostManager() throws Exception {
		URL url = new URL(AvailablityManager.VCENTERURL);
		si = new ServiceInstance(url, AvailablityManager.USERNAME, AvailablityManager.PASSWORD, true);
	}
		
	//******* find vHost by name ***********//
	public static HostSystem findVhostByNameInVcenter(String vhostname) throws Exception{
		Folder rootFolder = si.getRootFolder();
		HostSystem vhost =(HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", vhostname);		 
	    if(vhost == null) {
	    	System.out.println("Fail to find HostSystem: " + vhost);
	    	return null;
	    }else{
	    	System.out.println( "Success found HostSystem: " + vhost);
			return vhost;  	 	    	
	    }    		    	
    }
	
	//******* find all VMs in vHost  ***********//
    public static VirtualMachine[] findAllVmsInVhost(HostSystem vhost) throws Exception{ //may not necessary 
		//find all virtual machines in selected vHost
	    //may need to figure out a way to find all vms when this vHost is down.
    	VirtualMachine[] vms= vhost.getVms();  
	 return vms;
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
    		boolean pingable = PingManager.pingVhost(newHost);	
    		if(pingable){
    			System.out.println("vHost " + vHost_IP + "works fine"); 
    		}else{
    			System.out.println("Can not connect to vHost " + vHost_IP); 
    		}
    	}	     	
    }
    
    //********* add vHost **********//
    public static void addHost() throws Exception {
		HostConnectSpec addHost = new HostConnectSpec();
		String hostName =AvailablityManager.NEWHOSTURL;
		addHost.setHostName(hostName);
		addHost.setUserName(AvailablityManager.NEWHOSTUSERNAME);
		addHost.setPassword(AvailablityManager.NEWHOSTPASSWORD);
		addHost.setSslThumbprint(AvailablityManager.NEWHOSTSSLTHUMBPRINT);
		Datacenter dc = getDatacenter();
		System.out.println("---------------------------");
		System.out.println("\nTry to add a new host: " + hostName);
		Task task = dc.getHostFolder().addStandaloneHost_Task(addHost, new ComputeResourceConfigSpec(), true);
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("New host: " + hostName + " added succesfully!");
		} else {
			System.out.println("Fail to add host " + hostName);
		}
	}
    
    
    //********* get Data Center*********//
    public static Datacenter getDatacenter() throws Exception {
		Datacenter dc = null;
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
		if(entities==null || entities.length==0) {
			return null;
		}
		for(ManagedEntity entity : entities) {
			dc = (Datacenter)entity;
		}
		return dc;
	}

}
