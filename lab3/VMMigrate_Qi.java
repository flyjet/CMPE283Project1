package edu.sjsu.cmpe283.lab3;

import java.net.URL;

import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.TaskInfo;
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
 * http://vijava.sf.net
 * @author Steve Jin
 */

public class VMMigrate_Qi{
	
  public static void main(String[] args) throws Exception
  {
		
	String vmname = "T06-VM02-Ubuntu1";
	String newHostName = "130.65.132.184";  //vHost name
	  
	ServiceInstance si = new ServiceInstance(new URL("https://130.65.132.106/sdk"), "administrator", "12!@qwQW", true);
	Folder rootFolder = si.getRootFolder();	
	VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
	        rootFolder).searchManagedEntity(
	            "VirtualMachine", vmname);	

	
	//ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
	//System.out.println("hosts name "+  hosts[0].getName() + " ---------");
	
	//ManagedEntity[] mes = rootFolder.getChildEntity(); 
	
	//System.out.println("vm name. "+ vm.getName() + " ======");
	//System.out.println("vm root child name. "+  mes[0].getName() + "******");
	 
     HostSystem newHost = (HostSystem) new InventoryNavigator(
        rootFolder).searchManagedEntity(
            "HostSystem", newHostName);
     
     String ip = newHost.getConfig().getNetwork().getVnic()[0].getSpec().getIp().getIpAddress();
     System.out.println("get vHost Ip address " + ip );
     
    
     
    ComputeResource cr = (ComputeResource) newHost.getParent();
    
    String[] checks = new String[] {"cpu", "software"};
    HostVMotionCompatibility[] vmcs =
      si.queryVMotionCompatibility(vm, new HostSystem[] 
         {newHost},checks );
    
    String[] comps = vmcs[0].getCompatibility();
    if(checks.length != comps.length)
    {
      System.out.println("CPU/software NOT compatible. Exit.");
      si.getServerConnection().logout();
      return;
    }
    
    
    Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
        VirtualMachineMovePriority.highPriority, 
        VirtualMachinePowerState.poweredOff);
    
    String status = task.waitForMe();
  
    if(status ==Task.SUCCESS)
    {
      System.out.println("VMotioned!");
    }
    else
    {
      System.out.println("VMotion failed!");
      TaskInfo info = task.getTaskInfo();
      System.out.println(info.getError().getFault());
    }
    si.getServerConnection().logout();   
    
  }
  
}
