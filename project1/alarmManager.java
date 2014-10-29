package edu.sjsu.cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class alarmManager {
	
	
	//******* Set alarm if VM is power off ********  
	public static void setPowerOffAlarm() throws Exception {
		URL url = new URL(AvailablityManager.VCENTERURL);
		ServiceInstance si = new ServiceInstance(url, AvailablityManager.USERNAME, AvailablityManager.PASSWORD, true);
		
		AlarmManager alarmMgr = si.getAlarmManager();
		for (VirtualMachine vm : VMManager.getAllVMs()) {
			String vmname =vm.getName();
			AlarmSpec spec = createAlarmSpec("Vm_PowerOffAlarm." + vmname +"\n");
			Alarm[] alarms = alarmMgr.getAlarm(vm);
			for (Alarm alarm : alarms) {
				if (alarm.getAlarmInfo().getName().equals(spec.getName())) {
					alarm.removeAlarm();
				}
			}
			alarmMgr.createAlarm(vm, spec);
			if(vm.getSummary().getOverallStatus().toString().equals("yellow")){
				System.out.println("\n--------------------------");
				System.out.println("Set Power off Alarm for " + vm.getName() + " Yellow");
			}
		}
	}

	private static AlarmSpec createAlarmSpec(String alarmName) {
		AlarmSpec alarmSpec = new AlarmSpec();
		StateAlarmExpression stateAlarmExpression = createStateAlarmExpression();
		AlarmAction alarmAction = createAlarmTriggerAction(createPowerOffAction());
		alarmSpec.setExpression(stateAlarmExpression);
		alarmSpec.setName(alarmName);
		alarmSpec.setDescription("Monitor VM state and trigger some alarm actions");
		alarmSpec.setEnabled(true);
		AlarmSetting alarmSetting = new AlarmSetting();
		alarmSetting.setReportingFrequency(0);
		alarmSetting.setToleranceRange(0);
		alarmSpec.setSetting(alarmSetting);
		return alarmSpec;
	}
	
	private static StateAlarmExpression createStateAlarmExpression() {
		StateAlarmExpression stateAlarmExpression = new StateAlarmExpression();
		stateAlarmExpression.setType("VirtualMachine");
		stateAlarmExpression.setStatePath("runtime.powerState");
		stateAlarmExpression.setOperator(StateAlarmOperator.isEqual);
		stateAlarmExpression.setYellow("poweredOff");
		return stateAlarmExpression;
	}
	
	private static MethodAction createPowerOffAction() {
		MethodAction methodAction = new MethodAction();
		methodAction.setName("PowerOffVM_Task");
		MethodActionArgument argument = new MethodActionArgument();
		argument.setValue(null);
		methodAction.setArgument(new MethodActionArgument[] { argument });
		return methodAction;
	}
	
	private static AlarmTriggeringAction createAlarmTriggerAction(Action action) {
		AlarmTriggeringAction alarmTriggeringAction = new AlarmTriggeringAction();
		alarmTriggeringAction.setYellow2red(true);
		alarmTriggeringAction.setAction(action);
		return alarmTriggeringAction;
	}

}
