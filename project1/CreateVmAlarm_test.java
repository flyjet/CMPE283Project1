package edu.sjsu.cmpe283.project1;


import java.net.URL;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.GroupAlarmAction;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.SendEmailAction;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;



/**
 * http://vijava.sf.net
 * @author Steve Jin
 */

public class CreateVmAlarm_test
{
  public static void main(String[] args) throws Exception 
  {
	  URL url = new URL("https://130.65.132.106/sdk");
	  ServiceInstance si = new ServiceInstance(url,"administrator", "12!@qwQW", true);


    String vmname = "T06-VM02-Ubuntu1";
    InventoryNavigator inv = new InventoryNavigator(
        si.getRootFolder());
    VirtualMachine vm = (VirtualMachine)inv.searchManagedEntity(
            "VirtualMachine", vmname);
    if(vm==null)
    {
      System.out.println("Cannot find the VM " + vmname 
        + "\nExisting...");
      si.getServerConnection().logout();
      return;
    }
    
    AlarmManager alarmMgr = si.getAlarmManager();
    
    AlarmSpec spec = new AlarmSpec();
    
    StateAlarmExpression expression = 
      createStateAlarmExpression();
    AlarmAction emailAction = createAlarmTriggerAction(
        createEmailAction());
    AlarmAction methodAction = createAlarmTriggerAction(
        createPowerOnAction());
    GroupAlarmAction gaa = new GroupAlarmAction();

    gaa.setAction(new AlarmAction[]{emailAction, methodAction});
    spec.setAction(gaa);
    spec.setExpression(expression);
    spec.setName("VmPowerStateAlarm");
    spec.setDescription("Monitor VM state and send email " +
    		"and power it on if VM powers off");
    spec.setEnabled(true);    
    
    AlarmSetting as = new AlarmSetting();
    as.setReportingFrequency(0); //as often as possible
    as.setToleranceRange(0);
    
    spec.setSetting(as);
    
    alarmMgr.createAlarm(vm, spec);    //??
    
    si.getServerConnection().logout();
  }

  static StateAlarmExpression createStateAlarmExpression()
  {
    StateAlarmExpression expression = 
      new StateAlarmExpression();
    expression.setType("VirtualMachine");
    expression.setStatePath("runtime.powerState");
    expression.setOperator(StateAlarmOperator.isEqual);
    expression.setRed("poweredOff");
    return expression;
  }

  static MethodAction createPowerOnAction() 
  {
    MethodAction action = new MethodAction();
    action.setName("PowerOnVM_Task");
    MethodActionArgument argument = new MethodActionArgument();
    argument.setValue(null);
    action.setArgument(new MethodActionArgument[] { argument });
    return action;
  }
  
  static SendEmailAction createEmailAction() 
  {
    SendEmailAction action = new SendEmailAction();
    action.setToList("caoqisd@hotmail.com");
    action.setSubject("Alarm - {alarmName} on {targetName}\n");
    action.setBody("Description:{eventDescription}\n"
        + "TriggeringSummary:{triggeringSummary}\n"
        + "newStatus:{newStatus}\n"
        + "oldStatus:{oldStatus}\n"
        + "target:{target}");
    return action;
  }

  static AlarmTriggeringAction createAlarmTriggerAction(
      Action action) 
  {
    AlarmTriggeringAction alarmAction = 
      new AlarmTriggeringAction();
    alarmAction.setYellow2red(true);
    alarmAction.setAction(action);
    return alarmAction;
  }
}