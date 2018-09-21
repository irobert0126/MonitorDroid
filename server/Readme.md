
# Server part of C2 in Android


## Components

1. File Upload and Command Forward Server
2. Configuration Server
3. Init Script


## Logics

### File Upload and Command Forward Server

File Upload 

1. Check Token 
2. File Storing
3. Trigger Processing Logic
4. Save processed data to a file.


Process Management

1. Read all files with process data and list them.
2. Search Logic


Command Forward Server

1. Authenticate the uploaded Commands.
2. Store the uploaded Commands into DB.
3. Update the periodic query's result.


### Configuration Server

1. Do a configure api call and save all configuration into a file.
2. Do a system information api to receive system information. 
1. Read configuration file.
2. Generate a Dockefile.
3. Generate a key using system IMEI
4. Encrypt the APK.
5. Provide a APK download api.
6. Provide a Dockerfile download api.


### Init Script
1. Read system information and upload to configuration server. 
2. Read configuration information and upload to configuration server. 
3. Download Dockerfile
4. Download APK
5. Build Server Image
6. Create a deployment script




