package edu.sjsu.cmpe283.lab3;

import java.net.URL;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

/**
 * Sample code to show how to use the Managed Object APIs to power off VM.
 * @author Steve JIN (sjin@vmware.com)
 */


public class VMPoweron_Qi{

	public static void main(String[] args) throws Exception
	{
		ServiceInstance si = new ServiceInstance(new URL("https://130.65.132.106/sdk"), "administrator", "12!@qwQW", true);
		Folder rootFolder = si.getRootFolder();
		
		ManagedEntity[] mes = rootFolder.getChildEntity();  //get all the VM
		
		for(int i=0; i<mes.length; i++)
		{
			if(mes[i] instanceof Datacenter)
			{
				Datacenter dc = (Datacenter) mes[i];
				Folder vmFolder = dc.getVmFolder();
				ManagedEntity[] vms = vmFolder.getChildEntity();
				
				for(int j=0; j<vms.length; j++)
				{
					if(vms[j] instanceof VirtualMachine)
					{
						VirtualMachine vm = (VirtualMachine) vms[j];
						System.out.println((vm.getName()));
						VirtualMachineSummary summary = (VirtualMachineSummary) (vm.getSummary());
						System.out.println(summary.toString());
						VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vm.getRuntime();
						if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff
							&& "T06-VM02-Ubuntu1".equals(vm.getName()))
						{		
							Task task = vm.powerOnVM_Task(null);
							task.waitForMe();
							System.out.println("====================================");
							System.out.println("vm:" + vm.getName() + " powered on.");
						}
					}
				}
			}
		}
		si.getServerConnection().logout();
	}
}
