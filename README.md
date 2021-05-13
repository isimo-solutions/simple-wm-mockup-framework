# simple-wm-mockup-framework

This framework allows for [simple mocking](#Mocking) of webMethods services by replacing them with other implementations using naming conventions dependent on the list of profiles. This is a very simple and powerful tool allowing one to provide mockups for external systems and to test webMethods solutions in separation.

Additionally the framework provides the possibility to [record service calls](#Recording).

This simple webMethods Mockup framework can be installed as invoke manager entry inside of the invokemanager.cnf config file.

## Building the framework
In order to build the framework the system environment variable MOCKUPBUILDWMHOME must be set to an exisiting webMethods home directory. After that setting standard maven build can be started. Framework should be compatibie with any working webMethods Integration Server installation.

## <div id='Mocking'/>Service Mocking

### Configuration on the Integration Server
- stop IS
- copy the mockup framework JAR to the directory WMHOME/IntegrationServer/instances/default/lib/jars/custom on the target Integration Server
- edit the file WMHOME/IntegrationServer/instances/default/config/invokemanager.cnf to contain the following entry:

```
<?xml version="1.0" encoding="UTF-8"?>
<IDataXMLCoder version="1.0">
 <record javaclass="com.wm.data.ISMemDataImpl">
 <array name="processorArray" type="value" depth="1">
 <value>com.isimo.wm.mockupframework.MockingInterceptor</value>
 </array>
 </record>
</IDataXMLCoder>
```
- start IS

### How it works
The extension will take as a parameter the list of strings representing the names of the testing profiles. This parameter will be set using the Extended Properties of IS (watt.server.mockinginterceptor.profiles), this should be set to the list of string representing profiles, for example:

`watt.server.mockinginterceptor.profiles=test1,test2`

If the given testing profile is on the list then the implemented extension will try to call the following service instead of the actual one:


|Service | Name |
|-------|------|
| Actual Service | `<folder1>.<folder2>...<folderN>:<serviceName>`|
| Mockup Service | `<folder1>.<folder2>...<folderN>.mockup_<profile>:<serviceName>`|

If the mockup can't be found for any of the declared profiles, then the actual service will be called.

## <div id='Recording'/>Recording Service Calls

### Configuration on the Integration Server
- stop IS
- copy the mockup framework JAR to the directory WMHOME/IntegrationServer/instances/default/lib/jars/custom on the target Integration Server
- edit the file WMHOME/IntegrationServer/instances/default/config/invokemanager.cnf to contain the following entry:

```
<?xml version="1.0" encoding="UTF-8"?>
<IDataXMLCoder version="1.0">
 <record javaclass="com.wm.data.ISMemDataImpl">
 <array name="processorArray" type="value" depth="1">
 <value>com.isimo.wm.mockupframework.RecordingInterceptor</value>
 </array>
 </record>
</IDataXMLCoder>
```
- start IS
- create the file WMHOME/IntegrationServer/instances/default/config/recordingconfig.xml with the following content:


### recordingconfig.xml file

This file defines which services should be recorded.

```
<?xml version="1.0"?>
<recording
	<service>folder1.folder2.service1</service>
	...
	<service>folder1.folder2.servicen</service>
	<pattern>.*servicestype1$</pattern>
	...
	<pattern>.*servicestypeN$</pattern>
</recording>
```

This file can be modified when IS is running, there's no need to restart the server - changes to this file will be picked up automatically.

### Recording customization

Additionally the following IS Extended Settings can be set to customize the framework:

|Extended Setting|Description|Default Value|
|------|------|-----|
|watt.server.recordinginterceptor.config|Location of the recordingconfig.xml file|config/recordingconfig.xml|
|watt.server.recordinginterceptor.pipelinesdir|Location of the directory where recorded pipelines will be stored|pipeline/recording|
|watt.server.recordinginterceptor.config|Location of the recordingconfig.xml file|pipeline/recording|
|watt.server.recordinginterceptor.timestampformat|Timestamp format (as defined by Java SimpleDateFormat class)|yyyyMMdd_HHmmssSSS|
|watt.server.recordinginterceptor.filenameformat|Service name pattern where %SERVICE% is the service name, %TIMESTAMP% is the current timestamp, %SEQUENCE% is the unique sequence number with leadingzeros, %INOUT% is ('IN' for input pipeline and 'OUT' for output pipeline respectively| %SERVICE%\_%TIMESTAMP%\_%SEQUENCE%\_%INOUT%.xml|
