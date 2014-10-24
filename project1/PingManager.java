package edu.sjsu.cmpe283.project1;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;

public class PingManager {

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
				//System.out.println("ping " + ip + " success");
				return true;
			}else{
				//System.out.println("ping " + ip + "  not success");
				return false;
			}
			
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
