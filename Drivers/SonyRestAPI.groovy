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
    command "UpdateAll"
    command "getPowerStatus"
    command "getSubLevel"
    command "getSoundVolume"
    command "getVoiceMode"
    command "setSubLevel", ["number"]
    //command "setSoundVolume", ["number"]
    }

preferences {
        input("ipAddress", "string", title:"Sony IP Address", required:true, displayDuringSetup:true)
        input("ipPort", "string", title:"Sony Port (default: 100000)", defaultValue:10000, required:true, displayDuringSetup:true)
        input("refreshInterval", "enum", title: "Refresh Interval in minutes", defaultValue: "10", required:true, displayDuringSetup:true, options: ["1","5","10","15","30"])
        input"logEnable", "bool", title: "Enable debug logging", defaultValue: true
    }
 }

 // Generic Private Functions -------

private postAPICall(lib,json) {
	def requestParams = [
		uri:  "http://${settings.ipAddress}:${settings.ipPort}" ,
        path: lib,
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
    log.debug "result is ${response.data.result}"
  if (response.data?.id == 2) {
  	//Set the Global value of state.device on or off
    log.debug "Status is ${response.data.result[0]?.status}"
    state.device = (response.data.result[0]?.status == "active") ? "on" : "off"
    sendEvent(name: "switch", value: state.device, isStateChange: true)
    log.debug "Device State is '${state.device}'"
  }

  if (response.data?.id == 78) {
  	//Set the Global value of state.devicevolume
    log.debug "Volume is ${response.data.result[0]?.volume}"
    state.devicevolume = response.data.result[0]?.volume
    log.debug "DeviceVolume is '${state.devicevolume}'"
  }

  if (response.data?.id == 59) {
  	//Set the Global value of state.sublevel
    log.debug "SubLevel is ${response.data.result[0]?.currentValue}"
    state.sublevel = response.data.result[0]?.currentValue
    log.debug "DeviceSublevel is '${state.sublevel}'"
  }
  if (response.data?.id == 600) {
  	//Set the Global value of state.devicemute
    log.debug "Mute is ${response.data.result[0]?.mute}"
    state.devicemute = (response.data.result[0]?.mute == "on") ? "muted: : "unmuted"
    sendEvent(name: "mute", value: state.devicemute, isStateChange: true)
    log.debug "Devicemute is '${state.devicemute}'"
    
  }
    else {
    log.debug "no id found for result action"
    }

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
    def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":\"active\"}],\"id\":102}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getPowerStatus()
}

def setPowerStatusOff() {
    log.debug "Executing 'setPowerStatusOff' "
    def lib = "/sony/system"
    def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":\"off\"}],\"id\":102}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getPowerStatus()
}

def setSubLevel(def Level) {
  log.debug "Executing 'setSubLevel' with ${Level}"
    def lib = "/sony/audio"
    def json = "{\"method\":\"setSoundSettings\",\"version\":\"1.1\",\"params\":[{\"settings\":[{\"value\":\"${Level}\",\"target\":\"subwooferLevel\"}]}],\"id\":56}"
    postAPICall(lib,json)
    getSubLevel()
}

def getSubLevel() {
  log.debug "Executing 'getSubLevel' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getSoundSettings\",\"version\":\"1.1\",\"params\":[{\"target\":\"subwooferLevel\"}],\"id\":59}"
    postAPICall(lib,json)
}

def getSoundVolume() {
log.debug "Executing 'getSoundVolume' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getVolumeInformation\",\"version\":\"1.1\",\"params\":[{\"output\":\"\"}],\"id\":78}"
    postAPICall(lib,json)
}

def setSoundVolume(def Level) {
    log.debug "Executing 'setSoundVolume' with ${level} "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setAudioVolume\",\"version\":\"1.1\",\"params\":[{\"volume\":\"${Level}\",\"output\":\"\"}],\"id\":98}"
    postAPICall(lib,json)
    getSoundVolume()
}

def setMute(){
    log.debug "Executing 'setMute' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setAudioMute\",\"id\":601,\"params\":[{\"mute\":\"on\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getMuteStatus()
}

def setUnMute(){
    log.debug "Executing 'setUnMute' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"setAudioMute\",\"id\":602,\"params\":[{\"mute\":\"off\"}],\"version\":\"1.1\"}"
    postAPICall(lib,json)
    pauseExecution(2000)
    getMuteStatus()
}

def getMuteStatus(){
    log.debug "Executing 'getMuteStatus' "
    def lib = "/sony/audio"
    def json = "{\"method\":\"getVolumeInformation\",\"version\":\"1.1\",\"params\":[{\"output\":\"\"}],\"id\":600}"
    postAPICall(lib,json)
}