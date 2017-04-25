# DeviceSimulatorDemo
This is a sample device simulator demo program to demonstrate below mentioned IOT Security options.
1. User Id/Password authentication
2. One time password(OTP) authentication
3. Server unique id authentication
4. Message payload authentication

Following steps should be followed to run the program.
1. Broker application should be in running state. Please download the broker application from https://github.com/ayan13s/BrokerAppDemo.git  . Compile and run AppTest file as a java program. Detailed steps for configuring and executing the broker app are given in the readme file of broker app.
2. Download the DeviceSimulatorDemo codebase and import in eclipse(or similar IDEs) as a JAVA program(Java 8 is needed to run it). Jars from lib folder should be added in the classpath.
3. Update DeviceConfig/device.conf file with respective Watson IOT Platform settings and security options. Security options should be in sync with Broker app security options(example - if OTP is enabledin device app then it should be enabled in broker app as well).
4. Run the com.ibm.iotdemo.Main class program to get the starting menu.

Please find the associated developerworks article at the below link - 
https://www.ibm.com/developerworks/library/iot-trs-secure-iot-solutions2/
