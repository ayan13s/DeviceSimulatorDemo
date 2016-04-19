package com.ibm.iotdemo.device;

public interface DeviceLocalInterface {

	public class DeviceException extends Exception {
	};
	
	public enum AuthenticationMethod { UIDPWD, OTP, CERTIFICATE, UID };
	
	public enum OTPStatus { SUCCESS, RETRY, FAILURE };
	
	public boolean startDevice() throws DeviceException;
	
	public boolean stopDevice() throws DeviceException;
	
	public AuthenticationMethod getAuthenticationMethodSupported(); 
	
	public boolean initAuth(AuthenticationMethod authMethod) throws DeviceException;
	
	public void sendOTPAuth(String otp);
	
	public OTPStatus getOTPAuthStatus();
	
	public void setOtpEntered(String otpEntered);
	
	public boolean login(Object... params) throws DeviceException;
	
	public boolean logout() throws DeviceException;
	
	public boolean startSimulation(SimulationCallbackInterface callback) throws DeviceException;
	
	public boolean stopSimulation() throws DeviceException;
}