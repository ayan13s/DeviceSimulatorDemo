package com.ibm.iotdemo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.iotdemo.device.AccelerationSimulationUtility;
import com.ibm.iotdemo.device.CoordinatesSimulationUtility;
import com.ibm.iotdemo.device.DeviceLocalInterface;
import com.ibm.iotdemo.device.DeviceLocalInterface.AuthenticationMethod;
import com.ibm.iotdemo.device.DeviceLocalInterface.DeviceException;
import com.ibm.iotdemo.device.DeviceLocalInterface.OTPStatus;
import com.ibm.iotdemo.device.MotionSimulationUtility;
import com.ibm.iotdemo.device.SimulationCallbackInterface;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class DemoController {

	public class DataSent {
		public SimpleStringProperty timestamp;
		public SimpleStringProperty dataSent;
		public SimpleStringProperty sendStatus;

		public StringProperty timestampProperty() {
			return timestamp;
		}

		public StringProperty dataSentProperty() {
			return dataSent;
		}

		public StringProperty sendStatusProperty() {
			return sendStatus;
		}
	}

	public class CommandReceived {
		public SimpleStringProperty timestamp;
		public SimpleStringProperty commandReceived;
		public SimpleStringProperty commandStatus;

		public StringProperty timestampProperty() {
			return timestamp;
		}

		public StringProperty commandReceivedProperty() {
			return commandReceived;
		}

		public StringProperty commandStatusProperty() {
			return commandStatus;
		}

	}

	@FXML
	private Button buttonStartDeviceUidPwd;
	@FXML
	private Button buttonStartDeviceOtp;
	@FXML
	private Button buttonStartDeviceCert;

	@FXML
	private Button buttonStopDeviceUidPwd;
	@FXML
	private Button buttonStopDeviceOtp;
	@FXML
	private Button buttonStopDeviceCert;

	@FXML
	private ToggleGroup deviceTypeGroup;
	@FXML
	private RadioButton rdoUidPwdDevice;
	@FXML
	private RadioButton rdoOtpDevice;
	@FXML
	private RadioButton rdoCertDevice;
	@FXML
	private Text startActionStatus;

	@FXML
	private TextField fieldUsername;
	@FXML
	private TextField fieldPassword;
	@FXML
	private TextField fieldOtp;
	@FXML
	private Text submitActionStatus;

	@FXML
	private TableView<DataSent> tableSent;
	@FXML
	private TableView<CommandReceived> tableReceived;

	private ObservableList<DataSent> dataSent = FXCollections.observableArrayList();
	private ObservableList<CommandReceived> commandReceived = FXCollections.observableArrayList();

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:mm:ss");
	private OTPStatus otpStat = null;

	@FXML
	protected void handleStartButton(ActionEvent event) {

		Button buttonClicked = (Button) event.getSource();

		DeviceLocalInterface selectedDevice = null;

		switch (buttonClicked.getId()) {
		case "buttonStartDeviceUidPwd":
			selectedDevice = Context.singleton.selectDevice(0);
			break;
		case "buttonStartDeviceOtp":
			selectedDevice = Context.singleton.selectDevice(1);
			break;
		case "buttonStartDeviceCert":
			selectedDevice = Context.singleton.selectDevice(2);
			break;
		}

		if (null == selectedDevice) {
			startActionStatus.setText("No device selected.");
			return;
		}

		buttonClicked.setDisable(true);
		startActionStatus.setText("Waiting for device to start...");

		boolean deviceStarted = false;

		try {
			deviceStarted = selectedDevice.startDevice();
		} catch (DeviceException exc) {
			System.out.println("Cannot start device: " + exc);
		}

		if (!deviceStarted) {
			startActionStatus.setText("Cannot start device.");
			buttonClicked.setDisable(false);
			return;// null;
		}

		DemoController controller = null;

		switch (selectedDevice.getAuthenticationMethodSupported()) {
		case UIDPWD:
			controller = changeScene("SceneUidPwdAuthentication.fxml", buttonClicked.getScene());
			break;
		case OTP:
			try {
				boolean deviceAuthenticated = selectedDevice.login(null, null);
				if (deviceAuthenticated)
					selectedDevice.initAuth(AuthenticationMethod.OTP);
				if (null != fieldOtp)
					fieldOtp.clear();
				controller = changeScene("SceneOtpAuthentication.fxml", buttonClicked.getScene());
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case CERTIFICATE:
			controller = changeScene("SceneCertAuthentication.fxml", buttonClicked.getScene());
			break;
		}

		if (null != controller) {
			Context.singleton.setController(controller);
			controller.initializeAuthenticationScene();
		}
		return;
	}

	@FXML
	protected void handleStartButtonAction(ActionEvent event) {

		Button buttonClicked = (Button) event.getSource();
		if (rdoUidPwdDevice.isSelected())
			Context.singleton.selectDevice(0);
		else if (rdoOtpDevice.isSelected())
			Context.singleton.selectDevice(1);
		else if (rdoCertDevice.isSelected())
			Context.singleton.selectDevice(2);

		if (null == Context.singleton.getSelectedDevice()) {
			startActionStatus.setText("No device selected.");
			return;
		}

		buttonClicked.setDisable(true);
		startActionStatus.setText("Waiting for device to start...");

		boolean deviceStarted = false;

		try {
			deviceStarted = Context.singleton.getSelectedDevice().startDevice();
		} catch (DeviceException exc) {
			System.out.println("Cannot start device: " + exc);
		}

		if (!deviceStarted) {
			startActionStatus.setText("Cannot start device.");
			buttonClicked.setDisable(false);
			return;
		}

		switch (Context.singleton.getSelectedDevice().getAuthenticationMethodSupported()) {
		case UIDPWD:
			changeScene("SceneUidPwdAuthentication.fxml", buttonClicked.getScene());
			break;

		case OTP:
			if (null != fieldOtp)
				fieldOtp.clear();
			changeScene("SceneOtpAuthentication.fxml", buttonClicked.getScene());
			break;

		case CERTIFICATE:
			changeScene("SceneCertAuthentication.fxml", buttonClicked.getScene());
			break;
		}
		return;
	}

	@FXML
	protected void handleAuthButtonAction(ActionEvent event) {

		boolean deviceAuthenticated = false;
		Button buttonClicked = (Button) event.getSource();
		DeviceLocalInterface selectedDevice = Context.singleton.getSelectedDevice();
		if (null == selectedDevice) {
			submitActionStatus.setText("No device selected.");
			return;
		}
		buttonClicked.setDisable(true);
		submitActionStatus.setText("Waiting for authentication...");

		try {
			switch (selectedDevice.getAuthenticationMethodSupported()) {
			case UIDPWD:
				deviceAuthenticated = selectedDevice.login(fieldUsername.getText(), fieldPassword.getText());
				break;
			case OTP:
				while (true) {
					try {
						System.out.println("Waiting for input...");
						Thread.sleep(20);
						System.out.println("Enter OTP from server : ");
						if (fieldOtp.getText() != null && !fieldOtp.getText().equals("")) {
							System.out.println("OTP entered in device - " + fieldOtp.getText());
							selectedDevice.setOtpEntered(fieldOtp.getText());
							break;
						}
					} catch (InterruptedException ie) {
						// If this thread is interrupted by another thread
						ie.printStackTrace();
					}
				}
				Thread.sleep(2000);
				otpStat = selectedDevice.getOTPAuthStatus();
				if (otpStat == OTPStatus.RETRY) {
					submitActionStatus.setText("OTP authentication failed.. retrying...");
					Thread.sleep(400);
					if (null != fieldOtp)
						fieldOtp.clear();
					buttonClicked.setDisable(false);
					selectedDevice.initAuth(AuthenticationMethod.OTP);
					return;
				} else if (otpStat == OTPStatus.FAILURE)
					deviceAuthenticated = false;
				else if (otpStat == OTPStatus.SUCCESS)
					deviceAuthenticated = true;
				break;

			case CERTIFICATE:
				deviceAuthenticated = selectedDevice.login(null, null);
				break;
			}
		} catch (DeviceException exc) {
			System.out.println("Cannot authenticate device: " + exc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!deviceAuthenticated) {
			System.out.println("Cannot authenticate device: wrong user id or password");
			submitActionStatus.setText("Cannot authenticate device.");
			buttonClicked.setDisable(false);
			return;
		}
		submitActionStatus.setText("Device authenticated. Starting to send data...");
		DemoController controller = changeScene("SceneSimulation.fxml", buttonClicked.getScene());

		if (null != controller) {
			Context.singleton.setController(controller);
			controller.initializeSimulationScene();
		}
		return;
	}

	@FXML
	protected void handleStopButton(ActionEvent event) {

		stopSimulation();
		Context.singleton.reset();
		changeScene("SceneDeviceStart.fxml", ((Control) event.getSource()).getScene());
	}

	@FXML
	protected void handleExitButtonAction(ActionEvent event) {

		stopSimulation();
		Context.singleton.reset();
		changeScene("SceneDeviceStart.fxml", ((Control) event.getSource()).getScene());
	}

	protected DemoController changeScene(String nextSceneFileName, Scene currentScene) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(nextSceneFileName));
			loader.load();
			Stage stage = (Stage) currentScene.getWindow();
			Scene scene = new Scene(loader.getRoot());
			stage.setScene(scene);
			stage.show();
			return loader.getController();
		} catch (IOException exc) {
			exc.printStackTrace();
			return null;
		}

	}

	protected void initializeAuthenticationScene() {

		DeviceLocalInterface selectedDevice = Context.singleton.getSelectedDevice();
		switch (selectedDevice.getAuthenticationMethodSupported()) {
		case UIDPWD:
			buttonStopDeviceUidPwd.setDisable(false);
			break;
		case OTP:
			buttonStopDeviceOtp.setDisable(false);
			break;
		case CERTIFICATE:
			buttonStopDeviceCert.setDisable(false);
			break;
		}
		return;
	}

	protected void initializeSimulationScene() {

		DeviceLocalInterface selectedDevice = Context.singleton.getSelectedDevice();
		switch (selectedDevice.getAuthenticationMethodSupported()) {
		case UIDPWD:
			buttonStopDeviceUidPwd.setDisable(false);
			break;
		case OTP:
			buttonStopDeviceOtp.setDisable(false);
			break;
		case CERTIFICATE:
			buttonStopDeviceCert.setDisable(false);
			break;
		}
		startSimulation();
		return;
	}

	protected void startSimulation() {

		boolean simulationStarted = false;
		DeviceLocalInterface selectedDevice = Context.singleton.getSelectedDevice();
		try {
			tableSent.setItems(dataSent);
			tableReceived.setItems(commandReceived);
			simulationStarted = selectedDevice.startSimulation(new SimulationCallback());
		} catch (DeviceException exc) {
			System.out.println("Cannot perform simulation: " + exc);
		}
		if (!simulationStarted)
			submitActionStatus.setText("Could not simulate sending data.");
		return;
	}

	protected void stopSimulation() {

		DeviceLocalInterface selectedDevice = Context.singleton.getSelectedDevice();
		if (null != selectedDevice) {
			try {
				selectedDevice.stopSimulation();
				selectedDevice.logout();
				selectedDevice.stopDevice();
			} catch (DeviceException exc) {
				// Ignore exceptions
			}
		}
		return;
	}

	protected void finalize() {
		stopSimulation();
	}

	protected class SimulationCallback implements SimulationCallbackInterface {
		@Override
		public void onDataGenerated(Object... params) {
		}

		@Override
		public void onDataSent(Object... params) {

			String currentTimestamp = dateFormat.format(new Date());
			System.out.println(currentTimestamp + ": Data sent: " + ((params.length > 0) ? params[0] : "") + ", "
					+ ((params.length > 1) ? params[1] : "") + ", " + ((params.length > 2) ? params[2] : "") + ", "
					+ ((params.length > 3) ? params[3] : "") + ", " + ((params.length > 4) ? params[4] : ""));
			if (params[0] instanceof AccelerationSimulationUtility) {
				dataSent.add(new DataSent() {
					{
						timestamp = new SimpleStringProperty(currentTimestamp);
						dataSent = new SimpleStringProperty("Acceleration: (" + String.format("%.2f", params[1]) + ","
								+ String.format("%.2f", params[2]) + "," + String.format("%.2f", params[3]) + ")");
						sendStatus = new SimpleStringProperty(((boolean) params[4]) ? "Sent" : "Not sent");
					}
				});
			} else if (params[0] instanceof MotionSimulationUtility) {
				dataSent.add(new DataSent() {
					{
						timestamp = new SimpleStringProperty(currentTimestamp);
						dataSent = new SimpleStringProperty("Motion: (" + String.format("%.1f", params[1]) + ","
								+ String.format("%.1f", params[2]) + "," + String.format("%.1f", params[3]) + ")");
						sendStatus = new SimpleStringProperty(((boolean) params[4]) ? "Sent" : "Not sent");
					}
				});
			} else if (params[0] instanceof CoordinatesSimulationUtility) {
				dataSent.add(new DataSent() {
					{
						timestamp = new SimpleStringProperty(currentTimestamp);
						dataSent = new SimpleStringProperty("Latitude: " + String.format("%.4f", params[1])
								+ ", Longitude: " + String.format("%.4f", params[2]));
						sendStatus = new SimpleStringProperty(((boolean) params[3]) ? "Sent" : "Not sent");
					}
				});
			}
		}

		@Override
		public void onDataReceived(Object... params) {

			String currentTimestamp = dateFormat.format(new Date());

			if (params == null)
				return;
			System.out.println(currentTimestamp + ": Data Received: " + ((params.length > 0) ? params[0] : "") + ", "
					+ ((params.length > 1) ? params[1] : ""));
			commandReceived.add(new CommandReceived() {
				{
					timestamp = new SimpleStringProperty(currentTimestamp);
					commandReceived = new SimpleStringProperty(String.valueOf(params[0]));
					commandStatus = new SimpleStringProperty(((boolean) params[1]) ? "Valid" : "Invalid");
				}
			});
		}

		@Override
		public void onSimulationStarted(Object... params) {
		}

		@Override
		public void onSimulationStopped(Object... params) {
		}
	}
}
