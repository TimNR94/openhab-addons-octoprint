# OctoPrint Binding

This binding can be used for connecting the openHAB system to a 3D printer via an [octoprint](https://octoprint.org/) server.
It provides functionalities to monitor sensor values like temperatures and print job progress, to control the printer's actuators and to manage much more.
All in all it provides access to OctoPrint's functionalities via the openHAB system.

## Supported Things

The binding is tested with OctoPi, a Raspberry Pi distribution of OctoPrint, installed as operating system on a Raspberry Pi 3B.
As 3D printer, a Printrbot Simple was used, to test the implementation.
Further testing with other versions of OctoPrint or different printers have not been made.


## Thing Configuration

To manually configure a OctoPrint thing, at least you need the ip of the server and an api key to access the server via a secure connection.
All other configuration parameters are optional and can be used for personalization.

### OctoPrint Thing Configuration

| Name            | Type    | Description                                | Default | Required | Advanced |
|-----------------|---------|--------------------------------------------|---------|----------|----------|
| ip              | text    | Hostname or IP address of the device       | N/A     | yes      | no       |
| password        | text    | Password to access the device              | N/A     | no       | no       |
| apiKey          | text    | APIKey for secure connection to the server | N/A     | yes      | no       |
| serialPort      | text    | The printer's serial port                  | N/A     | no       | yes      |
| baudRate        | integer | The printer's baud rate                    | 115200  | no       | yes      |
| printerProfile  | integer | The printer's profile                      | N/A     | no       | yes      |
| refreshInterval | integer | Interval the device is polled in sec.      | 10      | no       | yes      |

## Channels

| Channel                   | Type     | Read/Write | Description                                         |
|---------------------------|----------|------------|-----------------------------------------------------|
| serverVersion             | String   | R          | Version of the OctoPrint server                     |
| serverSafeModeState       | String   | R          | Safe mode state of the OctoPrint server             |
| serverConnectionState     | String   | R          | Connection state of the OctoPrint server            |
| printJobState             | String   | R          | State of the current print job                      |
| printJobFileName          | String   | R          | File name of the current print job                  |
| printJobFileOrigin        | String   | R          | File path of the current print job                  |
| printJobFileSize          | Number   | R          | File size of the current print job                  |
| printJobFileDate          | DateTime | R          | Last edit of the current print job file             |
| printJobStart             | String   | RW         | Start the print job of the selected file            |
| printJobCancel            | String   | RW         | Cancel the current print job                        |
| printJobPause             | String   | RW         | Pause/resume the current print job                  |
| printJobRestart           | String   | RW         | Start the print job of the selected file            |
| printJobEstPrintTime      | Number   | R          | Estimated print time of the current print job       |
| printJobProgress          | Number   | R          | Percentage of completion of current print job       |
| printJobCurrentPrintTime  | Number   | R          | Elapsed print time of the current print job         |
| printJobEstTimeLeft       | Number   | R          | Estimated print time left for the current print job |
| printerState              | String   | R          | State of the printer                                |
| printerJogX               | Number   | RW         | Move printer head in x axis direction               |
| printerJogY               | Number   | RW         | Move printer head in y axis direction               |
| printerJogZ               | Number   | RW         | Move printer head in z axis direction               |
| printerHomingX            | String   | RW         | Homing of the printer's x axis                      |
| printerHomingY            | String   | RW         | Homing of the printer's y axis                      |
| printerHomingZ            | String   | RW         | Homing of the printer's z axis                      |
| printerHomingXYZ          | String   | RW         | Homing of the printer's x, y and z axis             |
| printerToolSelect         | Number   | RW         | Select a printer tool                               |
| printerToolFlowrate       | Number   | RW         | Apply a printer tool flowrate                       |
| printerToolTempActual     | Number   | R          | Actual temperature of the printer tool              |
| printerToolTempTarget     | Number   | RW         | Target temperature of the printer tool              |
| printerToolTempOffset     | Number   | RW         | Temperature offset of the printer tool              |
| printerBedTempActual      | Number   | R          | Actual temperature of the printer bed               |
| printerBedTempTarget      | Number   | RW         | Target temperature of the printer bed               |
| printerBedTempOffset      | Number   | RW         | Temperature offset of the printer bed               |
| printerChamberTempActual  | Number   | R          | Actual temperature of the printer chamber           |
| printerChamberTempTarget  | Number   | RW         | Target temperature of the printer chamber           |
| printerChamberTempOffset  | Number   | RW         | Temperature offset of the printer chamber           |
| SDCardState               | Switch   | R          | State of the printers SD card                       |
