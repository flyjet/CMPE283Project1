package edu.sjsu.cmpe283.lab3;

import java.net.URL;

import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VMClone_Qi
{
  public static void main(String[] args) throws Exception
  {


    String vmname = "T06-VM02-Ubuntu1";
    String cloneName = "T06-VM02-Ubuntu1_clone";
    
	ServiceInstance si = new ServiceInstance(new URL("https://130.65.132.106/sdk"), "administrator", "12!@qwQW", true);
	Folder rootFolder = si.getRootFolder();

    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
        rootFolder).searchManagedEntity(
            "VirtualMachine", vmname);

    if(vm==null)
    {
      System.out.println("No VM " + vmname + " found");
      si.getServerConnection().logout();
      return;
    }

    VirtualMachineCloneSpec cloneSpec = 
      new VirtualMachineCloneSpec();
    cloneSpec.setLocation(new VirtualMachineRelocateSpec());
    cloneSpec.setPowerOn(false);
    cloneSpec.setTemplate(false);

    Task task = vm.cloneVM_Task((Folder) vm.getParent(), 
        cloneName, cloneSpec);
    System.out.println("Launching the VM clone task. " +
    		"Please wait ...");

    String status = task.waitForMe();
    if(status==Task.SUCCESS)
    {
      System.out.println("VM got cloned successfully.");
    }
    else
    {
      System.out.println("Failure -: VM cannot be cloned");
    }
  }
}
