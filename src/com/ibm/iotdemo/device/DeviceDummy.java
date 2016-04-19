package com.ibm.iotdemo.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.ibm.iotdemo.mqtt.MqttHandler;
import com.ibm.iotdemo.mqtt.MqttUtil;
import com.ibm.iotdemo.mqtt.ServerAuthVO;
import com.ibm.iotdemo.security.util.IOTSecurityUtil;

public class DeviceDummy implements DeviceLocalInterface {

	protected AuthenticationMethod method;

	protected OTPStatus otpStatus;

	protected AccelerationSimulationUtility accelUtil;
	protected MotionSimulationUtility motionUtil;
	protected CoordinatesSimulationUtility coordUtil;

	protected SimulationCallbackInterface callback;
	private Map<String, ServerAuthVO> hmServerAuth = new HashMap<String, ServerAuthVO>();
	protected Timer accelSimulationtimer;
	protected Timer motionSimulationtimer;
	protected Timer coordSimulationtimer;

	private MqttHandler handler = null;
	private String strAuthStorageLocation = null;
	private String deviceIdentifier = null;
	private int otpRetryCount = 0;
	private boolean isSSL = false;
	private boolean isOTPCapable = false;
	private boolean isUIDValidationNeeded = false;
	private boolean isUIDStoragePossible = false;
	private boolean isEncryption = false;
	private String org = null;
	private String id = null;
	private String authmethod = null;
	private String authtoken = null;
	private String otpEntered = null;
	private String strEncryption = null;
	private final String strKey = "AyanMukhAyanMukh";
	private final String uniqueParam = "myuniqueivparam1";

	public String getOtpEntered() {
		return otpEntered;
	}

	public void setOtpEntered(String otpEntered) {
		this.otpEntered = otpEntered;
	}

	public DeviceDummy(AuthenticationMethod method) {
		this.method = method;
	}

	public MqttHandler getHandler() {
		return handler;
	}

	@Override
	public boolean startDevice() throws DeviceException {
		Properties props = MqttUtil.readProperties("DeviceConfig/device.conf");

		org = props.getProperty("org");
		id = props.getProperty("deviceid");
		deviceIdentifier = id;
		authmethod = "use-token-auth";
		authtoken = props.getProperty("token");
		// isSSL property
		String sslStr = props.getProperty("isSSL");
		String otpCapable = props.getProperty("isOTPCapableDevice");
		String uidValidationNeeded = props.getProperty("isUIDValidation");
		String strOTPRetryCount = props.getProperty("otpRetryCount");
		String uidStoragePossible = props.getProperty("isUIDStorageCapable");
		strAuthStorageLocation = props.getProperty("uidLocation");
		strEncryption = props.getProperty("isEncryption");

		otpRetryCount = Integer.valueOf(strOTPRetryCount);

		if (sslStr.equals("T")) {
			isSSL = true;
		}
		if (otpCapable.equals("T")) {
			isOTPCapable = true;
		}
		if (uidValidationNeeded.equals("T")) {
			isUIDValidationNeeded = true;
		}
		if (uidStoragePossible.equals("T")) {
			isUIDStoragePossible = true;
		}
		if (isUIDStoragePossible && strAuthStorageLocation == null) {
			System.out.println("uidLocation is missing...");
			System.exit(1);
		}
		if (strEncryption.equals("T")) {
			isEncryption = true;
		}

		System.out.println("org: " + org);
		System.out.println("id: " + id);
		System.out.println("authmethod: " + authmethod);
		System.out.println("authtoken: " + authtoken);
		System.out.println("isSSL: " + isSSL);
		System.out.println("isOTPCapable: " + isOTPCapable);
		System.out.println("isUIDValidationNeeded: " + isUIDValidationNeeded);

		return true;
	}

	@Override
	public boolean stopDevice() throws DeviceException {
		logout();
		return true;
	}

	@Override
	public AuthenticationMethod getAuthenticationMethodSupported() {
		return method;
	}

	@Override
	public boolean login(Object... params) throws DeviceException {

		System.out.println("User name entered - " + params[0]);
		System.out.println("Password entered - " + params[1]);
		if (null != params[0] && null != params[1]) {
			id = params[0].toString();
			authtoken = params[1].toString();
		}
		String serverHost = org + MqttUtil.SERVER_SUFFIX;

		// Format: d:<orgid>:<type-id>:<divice-id>
		String clientId = "d:" + org + ":" + MqttUtil.DEFAULT_DEVICE_TYPE + ":" + id;

		handler = new DeviceMqttHandler();

		try {
			handler.connect(serverHost, clientId, authmethod, authtoken, isSSL);

			// Subscribe the Command events
			// iot-2/cmd/<cmd-type>/fmt/<format-id>
			handler.subscribe("iot-2/cmd/" + MqttUtil.DEFAULT_CMD_ID + "/fmt/json", 0);
		} catch (Exception e) {
			return false;
		}
		if (isUIDValidationNeeded)
			initiateUIDAuth();
		return true;
	}

	@Override
	public boolean logout() throws DeviceException {
		stopSimulation();
		return true;
	}

	@Override
	public boolean startSimulation(SimulationCallbackInterface callback) throws DeviceException {

		this.callback = callback;
		accelUtil = new AccelerationSimulationUtility();

		if (null != callback)
			callback.onSimulationStarted(accelUtil);

		(accelSimulationtimer = new Timer()).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Object[] data = accelUtil.getNextData();
				if (null != callback)
					callback.onDataGenerated(accelUtil, data[0], data[1], data[2]);
				// Presume data has been sent successfully to the IoT Foundation
				new sendMessageToServer("data", accelUtil.convertDataToJason(data)).start();
				if (null != callback)
					callback.onDataSent(accelUtil, data[0], data[1], data[2], true);
			}
		}, 0, 1000);

		motionUtil = new MotionSimulationUtility();

		if (null != callback)
			callback.onSimulationStarted(motionUtil);

		(motionSimulationtimer = new Timer()).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Object[] data = motionUtil.getNextData();
				if (null != callback)
					callback.onDataGenerated(motionUtil, data[0], data[1], data[2]);
				// Presume data has been sent successfully to the IoT Foundation
				new sendMessageToServer("data", motionUtil.convertDataToJason(data)).start();
				if (null != callback)
					callback.onDataSent(motionUtil, data[0], data[1], data[2], true);
			}
		}, 0, 1000);

		coordUtil = new CoordinatesSimulationUtility(22.0, 88.0);

		if (null != callback)
			callback.onSimulationStarted(coordUtil);

		(coordSimulationtimer = new Timer()).scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Object[] data = coordUtil.getNextData();
				if (null != callback)
					callback.onDataGenerated(coordUtil, data[0], data[1]);
				// Presume data has been sent successfully to the IoT Foundation
				new sendMessageToServer("data", coordUtil.convertDataToJason(data)).start();
				if (null != callback)
					callback.onDataSent(coordUtil, data[0], data[1], true);
			}
		}, 0, 10000);

		return true;
	}

	@Override
	public boolean stopSimulation() throws DeviceException {

		if (null != accelUtil)
			accelUtil.reset();
		if (null != accelSimulationtimer)
			accelSimulationtimer.cancel();
		if (null != callback)
			callback.onSimulationStopped(accelUtil);

		if (null != motionUtil)
			motionUtil.reset();
		if (null != motionSimulationtimer)
			motionSimulationtimer.cancel();
		if (null != callback)
			callback.onSimulationStopped(motionUtil);

		if (null != coordUtil)
			coordUtil.reset();
		if (null != coordSimulationtimer)
			coordSimulationtimer.cancel();
		if (null != callback)
			callback.onSimulationStopped(coordUtil);

		accelUtil = null;
		motionUtil = null;
		coordUtil = null;

		return true;
	}

	@Override
	public boolean initAuth(AuthenticationMethod method) throws DeviceException {
		if (method == AuthenticationMethod.OTP)
			initiateOTPAuth();
		else if (method == AuthenticationMethod.UID)
			initiateOTPAuth();
		return true;
	}

	public void initiateOTPAuth() {
		// Create the request for OTP
		JSONObject idObj1 = new JSONObject();
		try {
			idObj1.put("event", "server_otp_request");
			idObj1.put("deviceId", deviceIdentifier);
		} catch (JSONException e1) {
			System.out.println("Exception occured");
			e1.printStackTrace();
		}
		new sendMessageToServer("server_otp_request", idObj1).start();
		System.out.println("otp request sent....");

	}

	@Override
	public void sendOTPAuth(String inOTP) {
		JSONObject idObj = new JSONObject();
		try {
			idObj.put("event", "device_otp_response");
			idObj.put("deviceid", deviceIdentifier);
			idObj.put("otp", inOTP);
		} catch (JSONException e1) {
			System.out.println("Exception occured");
			e1.printStackTrace();
		}

		new sendMessageToServer("device_otp_response", idObj).start();
		System.out.println("otp sent....");
	}

	@Override

	public OTPStatus getOTPAuthStatus() {
		return otpStatus;
	}

	public class sendMessageToServer extends Thread {
		private String eventss = null;
		private JSONObject objtd = null;

		public sendMessageToServer(String events, JSONObject obj1) {
			this.objtd = obj1;
			this.eventss = events;
		}

		public void run() {
			// Publish command to one specific device
			// iot-2/type/<type-id>/id/<device-id>/cmd/<cmd-id>/fmt/<format-id>
			if (isEncryption)
				handler.publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID + "/fmt/json",
						IOTSecurityUtil.encryptEncodeString(objtd.toString(), strKey, uniqueParam), false, 0);
			else
				handler.publish("iot-2/evt/" + MqttUtil.DEFAULT_EVENT_ID + "/fmt/json", objtd.toString(), false, 0);
			try {
				sendMessageToServer.currentThread().join();
			} catch (Exception te) {
				te.printStackTrace();
			}
		}

	}

	private void initiateUIDAuth() {
		// Create the request for server unique id
		JSONObject idObj = new JSONObject();
		try {
			idObj.put("event", "server_uid_request");
			idObj.put("deviceId", deviceIdentifier);
		} catch (JSONException e1) {
			System.out.println("Exception occured");
			e1.printStackTrace();
		}
		new sendMessageToServer("server_uid_request", idObj).start();
	}

	/**
	 * This class implements as the device MqttHandler
	 * 
	 */
	public class DeviceMqttHandler extends MqttHandler {

		@Override
		public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
			super.messageArrived(topic, mqttMessage);

			// Check whether the event is a command event from app
			if (topic.equals("iot-2/cmd/" + MqttUtil.DEFAULT_CMD_ID + "/fmt/json")) {

				byte[] rawPayload = mqttMessage.getPayload();
				JSONObject jsonObject = null;
				if (isEncryption) {
					String payload = IOTSecurityUtil.decryptDecodeString(rawPayload, strKey, uniqueParam);
					System.out.println("String payload - " + payload);
					// Parse the payload in Json Format
					jsonObject = new JSONObject(payload);
				} else {
					jsonObject = new JSONObject(new String(rawPayload));
				}

				String cmd = jsonObject.getString("cmd");

				if (cmd != null && cmd.equals("server_uid_response")) {
					try {
						String strServerUID = jsonObject.getString("uid");
						String strKey = jsonObject.getString("appid");
						System.out.println("Unique id received from server - " + strServerUID);
						/*
						 * Save the unique id in memory as well as in file For
						 * very small devices if there is no storage capacity
						 * then uid request would be sent to server after every
						 * restart
						 */
						ServerAuthVO sAuth = new ServerAuthVO();
						sAuth.setAppKey(strKey);
						sAuth.setUid(strServerUID);
						sAuth.setFromFile(false);
						hmServerAuth.put(strKey, sAuth);
						if (isUIDStoragePossible) {
							sAuth.setFromFile(true);
							boolean success = IOTSecurityUtil.saveAuthObjInFile(strAuthStorageLocation, sAuth);
							if (success) {
								System.out.println("Server Auth object has been stored into file");

							}
						}
					} catch (Exception ee) {
						System.out.println("Error in server UID response processing");
						ee.printStackTrace();
					}

				} else if (cmd != null && cmd.equals("server_otp_response")) {
					String inOTP = null;
					System.out.println("Please enter the OTP received in registered email within 5 mins...");
					// TBD- Following piece of code should be replaced with a
					// proper timer
					while (true) {
						try {
							Thread.sleep(20);
							if (getOtpEntered() != null) {
								break;
							}
						} catch (InterruptedException ie) {
							// If this thread is interrupted by another thread
							ie.printStackTrace();
						}
					}
					JSONObject idObj = new JSONObject();
					try {
						idObj.put("event", "device_otp_response");
						idObj.put("deviceid", deviceIdentifier);
						idObj.put("otp", getOtpEntered());
					} catch (JSONException e1) {
						System.out.println("Exception occured");
						e1.printStackTrace();
					}
					new sendMessageToServer("device_otp_response", idObj).start();
					setOtpEntered(null);

				} else if (cmd != null && cmd.equals("server_otp_validate")) {
					boolean isOTPValid1 = jsonObject.getBoolean("isOTPValid");
					boolean isTimeOut1 = jsonObject.getBoolean("isTimeOut");
					if (isOTPValid1 && !isTimeOut1) {
						otpStatus = OTPStatus.SUCCESS;
						System.out.println("OTP Validation complete");

					} else if (!isOTPValid1) {
						System.out.println("Server sent invalid OTP");
						otpStatus = OTPStatus.FAILURE;
					} else if (isTimeOut1) {
						System.out.println("Server sent otp timeout");
						otpStatus = OTPStatus.FAILURE;
					} else {
						System.out.println("OTP Validation failed.. Shutting down..");
						System.exit(1);
					}
					if (otpRetryCount > 0 && otpStatus == OTPStatus.FAILURE) {
						System.out.println("OTP Validation failed... retrying..");
						otpStatus = OTPStatus.RETRY;
						otpRetryCount--;
					} else if (otpRetryCount <= 0) {
						System.out.println("Retry limit exceeded... ");
						otpStatus = OTPStatus.FAILURE;
					}

				} else if (cmd != null && cmd.equals("command")) {
					String text = jsonObject.getString("text");
					String uIDSent = jsonObject.getString("uid");
					String appIDSent = jsonObject.getString("appid");
					System.out.println("Data received from IOTF app");
					if (isUIDValidationNeeded) {
						ServerAuthVO svo = hmServerAuth.get(appIDSent);
						if (svo != null) {
							System.out.println(uIDSent);
							System.out.println(svo.getUid());

							if (null != uIDSent && svo.getUid().equals(uIDSent)) {
								System.out.println("UID matching with server is successful... executing command");
								if (null != callback)
									callback.onDataReceived(text, true, appIDSent);
								System.out.println("Received reset instructions from server.. resetting count to 0");
							} else {
								System.out.println("UID matching with server is unsuccessful... invalid command");
								if (null != callback)
									callback.onDataReceived(text, false, appIDSent);

							}
						}
					} else {
						if (null != callback)
							callback.onDataReceived(text, true, appIDSent);
						System.out.println("Received reset instructions from server.. uid validation not needed");
					}

				}

			}
		}
	}

}
