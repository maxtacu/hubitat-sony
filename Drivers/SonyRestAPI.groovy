/**
 *  Sony Soundbar RestfulAPI
 *  Hubitat Integration
 *  IMPORT URL: https://raw.githubusercontent.com/jonesalexr/hubitat/main/Drivers/SonyRestAPI.groovy
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 metadata {
  definition (name: "Sony REST API", namespace: "ajones", author: "Alex Jones") {
    capability "Switch"
    capability "Refresh"
    capability "Polling"
    capability "AudioVolume"
    //capability "MusicPlayer"
    command "UpdateAll"
    //command "getPowerStatus"
    //command "getSubLevel"
    //command "getSoundVolume"
    command "getVoiceMode"
    //command "getNightMode"
    //command "getClearVoiceStatus"
    //command "getSystemInfo"
    command "setSubLevel", ["number"]
    command "setNightModeOn"
    command "setNightModeOff"
    command "getSoundField"
    command "setSoundField", [[name:"Choose Soundfield", type: "ENUM", constraints: [
				"","clearAudio","movie","music","sports","game","standard","off"] ] ]
    attribute "SubLevel", "number"
    attribute "NightMode", "string"
    attribute "SoundField", "string"
    }

preferences {
        input("ipAddress", "string", title:"Sony IP Address", required:true, displayDuringSetup:true)
        input("ipPort", "string", title:"Sony Port (default: 100000)", defaultValue:10000, required:true, displayDuringSetup:true)
        input("PSK", "string", title:"PSK Passphrase", defaultValue:123456, required:false, displayDuringSetup:true)
        input("refreshInterval", "enum", title: "Refresh Interval in minutes", defaultValue: "10", required:true, displayDuringSetup:true, options: ["1","5","10","15","30"])
        input"logEnable", "bool", title: "Enable debug logging", defaultValue: true
    }
 }

 // Generic Private Functions -------

private postAPICall(lib,json) {
	def headers = [:]
        headers.put("HOST", "${settings.ipAddress}:${settings.ipPort}")
        //headers.put("Content-Type", "application/json")
        headers.put("X-Auth-PSK", "${settings.PSK}")
    
    def requestParams = [
		uri:  "http://${settings.ipAddress}:${settings.ipPort}" ,
        path: lib,
        headers: headers,
//requestContentType: "application/json",
		query: null,
		body: json,
	]
    log.debug "${requestParams}"
	httpPostJson(requestParams) { response ->
		def msg = ""
		if (response?.status == 200) {
			msg = "Success"
		}
		else {
			msg = "${response?.status}"
		}
		log.debug "Sony Response: ${msg} (${response.data})"
        jsonreturnaction(response)
	}
}

private jsonreturnaction(response){
    log.debug "ID is ${response.data.id}"
    log.debug "data result is ${response.data.result}"
    log.debug "data error is ${response.data.error}"

    String responsedataerror = response.data.error
    log.debug "dataerrorstring is ${responsedataerror}"

    if (responsedataerror != "null"){
    log.warn "data error is ${response.data.error}"
    }

  if (response.data?.id == 2) {
  	//Set the Global value of state.device on or off
    log.debug "Status is ${response.data.result[0]?.status}"
    state.device = (response.data.result[0]?.status == "active") ? "on" : "off"
    sendEvent(name: "switch", value: state.device, isStateChange: true)
    log.debug "Device State is '${state.device}'"
  }

  if (response.data?.id == 50) {
  	//Set the Global value of state.devicevolume
    log.debug "Volume is ${response.data.result[0][0]?.volume}"
    state.devicevolume = response.data.result[0][0]?.volume
       sendEvent(name: "volume", value: state.devicevolume, isStateChange: true)
    log.debug "DeviceVolume State is '${state.devicevolume}'"
  }

  if (response.data?.id == 55) {
  	//Set the Global value of state.sublevel
    log.debug "SubLevel is ${response.data.result[0][0]?.currentValue}"
    state.sublevel = response.data.result[0][0]?.currentValue
    sendEvent(name: "SubLevel", value: state.sublevel, isStateChange: true)
    log.debug "DeviceSublevel State is '${state.sublevel}'"
  }
  if (response.data?.id == 40) {
  	//Set the Global value of state.devicemute
    log.debug "Mute is ${response.data.result[0][0]?.mute}"
    state.devicemute = response.data.result[0][0]?.mute
    sendEvent(name: "mute", value: state.devicemute, isStateChange: true)
    log.debug "Devicemute State is '${state.devicemute}'"
  }
  if (response.data?.id == 99) {
  	//Set the Global value of systeminfo
    log.debug "bdAddr State is ${response.data.result[0]?.bdAddr}"
    state.bdAddr = response.data.result[0]?.bdAddr
    log.debug "macAddr State is ${response.data.result[0]?.macAddr}"
    state.macAddr = response.data.result[0]?.macAddr
    log.debug "version is State ${response.data.result[0]?.version}"
    state.version = response.data.result[0]?.version
    log.debug "wirelessMacAddr State is ${response.data.result[0]?.wirelessMacAddr}"
    state.wirelessMacAddr = response.data.result[0]?.wirelessMacAddr
  }
  if (response.data?.id == 61) {
  	//Set the Global value of state.nightmode
    log.debug "nightmode is ${response.data.result[0][0]?.currentValue}"
    state.nightmode = response.data.result[0][0]?.currentValue
    sendEvent(name: "NightMode", value: state.nightmode, isStateChange: true)
    log.debug "NightMode State is '${state.nightmode}'"
  }
    if (response.data?.id == 66) {
  	//Set the Global value of state.nightmode
    log.debug "soundfield is ${response.data.result[0][0]?.currentValue}"
    state.soundfield = response.data.result[0][0]?.currentValue
    sendEvent(name: "SoundField", value: state.soundfield, isStateChange: true)
    log.debug "SoundField State is '${state.soundfield}'"
  }
    else {log.debug "no id found for result action"}

}

//Button Commands
def on(){
    log.debug "on pushed"
    setPowerStatusOn()
}

def off(){
    log.debug "off pushed"
    setPowerStatusOff()
}

def UpdateAll(){
    log.debug "UpdateAll pushed"
    getPowerStatus()
    getSoundVolume()
    getSubLevel()
    getMuteStatus()
    getSystemInfo()
    getNightModeStatus()
    getSoundField()
}

def setVolume(level) {
    log.debug "set volume pushed with ${level}"
    setSoundVolume(level)
}

def volumeUp() {
    log.debug "volumeup pushed"
    def level="+1"
    setSoundVolume(level)
}

def volumeDown() {
    log.debug "volumeup pushed"
    def level="-1"
    setSoundVolume(level)
}

def mute(){
    log.debug "mute pushed"
    setMute()
}

def unmute(){
    log.debug "unmute pushed"
    setUnMute()
}

//API Commands

def getPowerStatus() {
    log.debug "Executing 'getPowerStatus' "
    def lib = "/sony/system"
    def json = "{\"id\":2,\"method\":\"getPowerStatus\",\"version\":\"1.1\",\"params\":[]}"
    postAPICall(lib,json)
}

def setPowerStatusOn() {
    log.debug "Executing 'setPowerStatusOn' "
    def lib = "/sony/system"
    def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":\"active\"}],\"id\":3}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getPowerStatus()
}

def setPowerStatusOff() {
    log.debug "Executing 'setPowerStatusOff' "
    def lib = "/sony/system"
    def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":\"off\"}],\"id\":4}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getPowerStatus()
}

def getSoundVolume() {
log.debug "Executing 'getSoundVolume' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getVolumeInformation\",\"version\":\"1.1\",\"params\":[{\"output\":\"\"}],\"id\":50}"
    postAPICall(lib,json)
}

def setSoundVolume(def Level) {
    log.debug "Executing 'setSoundVolume' with ${level} "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setAudioVolume\",\"version\":\"1.1\",\"params\":[{\"volume\":\"${Level}\",\"output\":\"\"}],\"id\":51}"
    postAPICall(lib,json)
    getSoundVolume()
}

def getSubLevel() {
  log.debug "Executing 'getSubLevel' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getSoundSettings\",\"version\":\"1.1\",\"params\":[{\"target\":\"subwooferLevel\"}],\"id\":55}"
    postAPICall(lib,json)
}

def setSubLevel(def Level) {
  log.debug "Executing 'setSubLevel' with ${Level}"
    def lib = "/sony/audio"
    def json = "{\"method\":\"setSoundSettings\",\"version\":\"1.1\",\"params\":[{\"settings\":[{\"value\":\"${Level}\",\"target\":\"subwooferLevel\"}]}],\"id\":56}"
    postAPICall(lib,json)
    getSubLevel()
}

def getMuteStatus(){
    log.debug "Executing 'getMuteStatus' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getVolumeInformation\",\"version\":\"1.1\",\"params\":[{\"output\":\"\"}],\"id\":40}"
    postAPICall(lib,json)
}

def setMute(){
    log.debug "Executing 'setMute' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setAudioMute\",\"id\":41,\"params\":[{\"mute\":\"on\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getMuteStatus()
}

def setUnMute(){
    log.debug "Executing 'setUnMute' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setAudioMute\",\"id\":42,\"params\":[{\"mute\":\"off\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getMuteStatus()
}

def getSystemInfo(){
    log.debug "Executing 'getSystemInfo' "
    def lib = "/sony/system"
    def json = "{\"method\":\"getSystemInformation\",\"id\":99,\"params\":[],\"version\":\"1.4\"}"
    postAPICall(lib,json)
}

def getNightModeStatus(){
    log.debug "Executing 'getNightModeStatus' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getSoundSettings\",\"id\":61,\"params\":[{\"target\":\"nightMode\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
}

def setNightModeOn(){
    log.debug "Executing 'setNightModeOn' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setSoundSettings\",\"id\":62,\"params\":[{\"settings\":[{\"value\":\"on\",\"target\":\"nightMode\"}]}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
        pauseExecution(2000)
    getNightModeStatus()
}

def setNightModeOff(){
    log.debug "Executing 'setNightModeOff' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setSoundSettings\",\"id\":63,\"params\":[{\"settings\":[{\"value\":\"off\",\"target\":\"nightMode\"}]}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
            pauseExecution(2000)
    getNightModeStatus()
}

def getClearVoiceStatus(){
    log.debug "Executing 'getClearVoiceStatus' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getSoundSettings\",\"id\":64,\"params\":[{\"target\":\"clearAudio\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
}

def setSoundField(def mode){
    log.debug "Executing 'setSoundField' "
    log.debug "variable is ${mode}"
    def lib = "/sony/audio"
    def json = "{\"method\":\"setSoundSettings\",\"id\":65,\"params\":[{\"settings\":[{\"value\":\"${mode}\",\"target\":\"soundField\"}]}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
        pauseExecution(2000)
    getSoundField()
}

def getSoundField(){
    log.debug "Executing 'getSoundField' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getSoundSettings\",\"id\":66,\"params\":[{\"target\":\"soundField\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
}