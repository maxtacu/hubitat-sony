/**
 *  Sony Soundbar RestfulAPI
 *  Hubitat Integration
 *  IMPORT URL: https://raw.githubusercontent.com/jonesalexr/hubitat/main/Drivers/SonyAudioDriver.groovy
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
  definition (name: "Sony Soundbar API", namespace: "ajones", author: "Alex Jones") {
    capability "Switch"
    capability "Refresh"
    capability "Polling"
    command "UpdateAll"
    }

preferences {
        input name: "ip1", type: "number", range: "0..254", required: true, title: "Ip address 1", displayDuringSetup: true
		    input name: "ip2", type: "number", range: "0..254", required: true, title: "Ip address 2", displayDuringSetup: true
		    input name: "ip3", type: "number", range: "0..254", required: true, title: "Ip address 3", displayDuringSetup: true
		    input name: "ip4", type: "number", range: "0..254", required: true, title: "Ip address 4", displayDuringSetup: true
        input name: "device_port", type: "number", range: "0..99999", defaultValue: "10000", required: true, title: "Device Port", displayDuringSetup: true
        input("refreshInterval", "enum", title: "Refresh Interval in minutes", defaultValue: "10", required:false, displayDuringSetup:true, options: ["1","5","10","15","30"])
        input name: "device_psk", type: "text", title: "PSK Passphrase Set on your device", description: "Enter passphrase", required: false, displayDuringSetup: true
        input"logEnable", "bool", title: "Enable debug logging", defaultValue: true
    }
 }

// Generic Functions

def updated(){
	if (logEnable) log.debug("Updated...")
	unschedule()
	log.warn "debug logging is: ${logEnable == true}"
    log.warn "description logging is: ${txtEnable == true}"
    if (logEnable) runIn(1800,logsOff)
	state.device_poll_count = 0
	if (logEnable) log.debug( "Preferences Updated rebuilding IP Address, MAC address and Hex Network ID")
	ipaddress()
	iphex()
	refresh()
	runEvery1Minute(poll)
}

def ipaddress(){
	//Build an IP Address from the 4 input preferences
	if (logEnable) log.debug( "building IP address from Preferences")
	state.device_ip = "${ip1}" + "." + "${ip2}" + "." + "${ip3}" + "." + "${ip4}"
	if (logEnable) log.debug( "IP Address State Value Set to = ${state.device_ip}:${device_port}" )
}

def iphex(){
	//create a Hex of the IP this will need to be set as the Network ID
	//TO DO Set the Network IP automatically or Show the user the Value to set manually
	if (logEnable) log.debug( "Creating Hex of IP: ${state.device_ip}")

	
	String deviceipstring = state.device_ip
	String device_ip_hex = deviceipstring.tokenize( '.' ).collect {
		String.format( '%02x', it.toInteger() )
	}.join()

	//set the global value of state.ip_hex
	state.ip_hex = device_ip_hex
	if (logEnable) log.debug ("IP Hex stored Globaly as '${state.ip_hex}'")

	if (logEnable) log.debug( "Creating Hex of Port: ${device_port}")


    String deviceportstring = device_port
    String device_port_hex = deviceportstring.tokenize( '.' ).collect {
    	String.format( '%04x', it.toInteger() )
    }.join()

    //Set the Global Value of state.port_hex
    state.port_hex = device_port_hex
    if (logEnable) log.debug ("Port Hex stored Globaly as '${state.port_hex}'")

    if (logEnable) log.debug( "Please set your Device Network ID to the following to allow the state to be captured: ${state.ip_hex}:${state.port_hex}" )
    String netid = ("${state.ip_hex}:${state.port_hex}")
    if (logEnable) log.debug( "Netid ${netid}" )
    //device.deviceNetworkId = ("${netid}")
}

def parse(description) {
  //if (logEnable) log.debug ("Parsing '${description}'")
  def msg = parseLanMessage(description)
	//Set the Global Value of state.device_mac
	//if (logEnable) log.debug "${msg}"
    state.device_mac = msg.mac
    if (logEnable) log.debug ("MAC Address stored Globally as '${state.device_mac}'")
    //if (logEnable) log.debug "msg '${msg}'"
    //if (logEnable) log.debug "msg.json '${msg.json?.id}'"
    
  
  if (msg.json?.id == 2) {
  	//Set the Global value of state.device on or off
    state.device = (msg.json.result[0]?.status == "active") ? "on" : "off"
    sendEvent(name: "switch", value: state.device)
    if (logEnable) log.debug "Device is '${state.device}'"
    state.device_poll_count = 0
  }
}

private sendJsonRpcCommand(json) {
  def headers = [:]
  headers.put("HOST", "${state.device_ip}:${device_port}")
  headers.put("Content-Type", "application/json")
  headers.put("X-Auth-PSK", "${device_psk}")

  def result = new hubitat.device.HubAction(
    method: 'POST',
    path: '/sony/system',
    body: json,
    headers: headers
  )

  result
}

def installed() {
  if (logEnable) log.debug "Executing 'installed'"
  poll()
}

def WOLC() {
    if (logEnable) log.debug "Executing Wake on Lan"
	def result = new hubitat.device.HubAction (
  	  	"wake on lan ${state.device_mac}", 
   		hubitat.device.Protocol.LAN,
   		null,
    	[secureCode: "111122223333"]
	)
	return result
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

// Basic button commands

def on() {
  if (logEnable) log.debug "Executing 'on'"
  
  if (state.device == "polling"){
  	  WOLC()
        setPowerStatusOn()
        //poll()
  } else {
        setPowerStatusOn()
        //poll()
  }
}

def off() {
  if (logEnable) log.debug "Executing 'off'"
    setPowerStatusOff()
    //poll()
}

def refresh() {
  if (logEnable) log.debug "Executing 'refresh'"
  poll()
}

def poll() {
  //set state.device to 0ff
  if (logEnable) log.debug "poll count ${state.device_poll_count}"
  state.device = "polling"
  state.device_poll_count = (state.device_poll_count + 1)
  if (state.device_poll_count > 1 ) {
  	  sendEvent(name: "switch", value: "off")
  }
  if (logEnable) log.debug "Executing 'poll'"
    getPowerStatus()
}

def UpdateAll() {
    if (logEnable) log.debug("UpdateAllClicked.....")
    sendEvent(name: "switch", value: "off")
}

//API Commands


def getPowerStatus() {
    def lib = '/sony/system'
    def json = "{\"id\":2,\"method\":\"getPowerStatus\",\"version\":\"1.1\",\"params\":[]}"
    def result = sendJsonRpcCommand(json)
}

def setPowerStatusOn() {
    def lib = '/sony/system'
    def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":\"active\"}],\"id\":102}"
    def result = sendJsonRpcCommand(json)    
}

def setPowerStatusOff() {
    def lib = '/sony/system'
    def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":\"off\"}],\"id\":102}"
    def result = sendJsonRpcCommand(json)
}