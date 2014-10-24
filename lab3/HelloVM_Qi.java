package edu.sjsu.cmpe283.lab3;


import java.net.URL;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

public class HelloVM_Qi
{
	public static void main(String[] args) throws Exception
	{
		long start = System.currentTimeMillis();
		URL url = new URL("https://130.65.132.106/sdk");
		ServiceInstance si = new ServiceInstance(url, "administrator", "12!@qwQW", true);
		long end = System.currentTimeMillis();
		System.out.println("time taken:" + (end-start));
		Folder rootFolder = si.getRootFolder();
		String name = rootFolder.getName();      //data center name
		System.out.println("root:" + name);
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
												// get all the VMs
		if(mes==null || mes.length ==0)
		{
			return;
		}
		
		VirtualMachine vm = (VirtualMachine) mes[2]; 
		
		VirtualMachineConfigInfo vminfo = vm.getConfig();
		
		VirtualMachineCapability vmc = vm.getCapability();

		vm.getResourcePool();
		System.out.println("Hello " + vm.getName());
		System.out.println("GuestOS: " + vminfo.getGuestFullName());
		System.out.println("Multiple snapshot supported: " + vmc.isMultipleSnapshotsSupported());

		si.getServerConnection().logout();
	}

}