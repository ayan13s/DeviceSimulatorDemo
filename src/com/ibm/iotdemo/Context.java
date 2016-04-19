package com.ibm.iotdemo;

import com.ibm.iotdemo.device.DeviceDummy;
import com.ibm.iotdemo.device.DeviceLocalInterface;
import com.ibm.iotdemo.device.DeviceLocalInterface.AuthenticationMethod;

public class Context {
	public static Context singleton = new Context();

	protected DeviceLocalInterface[] prebuiltDevices = { new DeviceDummy(AuthenticationMethod.UIDPWD),
			new DeviceDummy(AuthenticationMethod.OTP), new DeviceDummy(AuthenticationMethod.CERTIFICATE) };

	protected DeviceLocalInterface selectedDevice;

	protected DemoController controller;

	protected Context() {

	}

	public void reset() {
		selectedDevice = null;
		controller = null;
	}

	public int getNumOfDevices() {
		return prebuiltDevices.length;
	}

	public DeviceLocalInterface selectDevice(int choice) {
		selectedDevice = prebuiltDevices[choice];
		return selectedDevice;
	}

	public DeviceLocalInterface getSelectedDevice() {
		return selectedDevice;
	}

	public DemoController getController() {
		return controller;
	}

	public void setController(DemoController controller) {
		this.controller = controller;
	}
}
