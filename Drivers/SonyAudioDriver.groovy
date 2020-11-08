/**
 *  Sony Soundbar RestfulAPI
 *  Hubitat Integration
 *  IMPORT URL: https://raw.githubusercontent.com/jonesalexr/hubitat/main/SonyAudioDriver.groovy
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
    capability "Refresh"}

preferences {
        input("ipAddress", "string", title:"Sony Soundbar IP Address", required:true, displayDuringSetup:true)
        input("ipPort", "string", title:"Soundbar Port (default: 10000)", defaultValue:10000, required:true, displayDuringSetup:true)
        input("refreshInterval", "enum", title: "Refresh Interval in minutes", defaultValue: "10", required:true, displayDuringSetup:true, options: ["1","5","10","15","30"])
    }
 }

// Generic Private Functions Not sure what im doing here -------
private getHostAddress() {
    def ip = settings.ipAddress
    def port = settings.ipPort
    return ip + ":" + port
}

private getDNI(String ipAddress, String port){
    log.debug "Generating DNI"
    String ipHex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    String portHex = String.format( '%04X', port.toInteger() )
    String newDNI = ipHex + ":" + portHex
    return newDNI
}

private apiGet(def apiCommand) {
    log.debug "Executing hubaction on " + getHostAddress() + apiCommand
    sendEvent(name: "hubactionMode", value: "local")

    def hubAction = new hubitat.device.HubAction(
        method: "GET",
        path: apiCommand,
        headers: [Host:getHostAddress()]
    )

    return hubAction
}

private sendJsonCommand(json) {
  def headers = [:]
  headers.put("HOST", "${getHostAddress}")
  headers.put("Content-Type", "application/json")

  def result = new hubitat.device.HubAction(
    method: 'POST',
    path: '/sony/system',
    body: json,
    headers: headers
  )
  result
}

// Basic button commands

def refresh() {
    def json = "{\"method\":\"getPowerStatus\",\"version\":\"1.1\",\"params\":[],\"id\":102}"
  	def result = sendJsonCommand(json)
}

def on() {

      def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":active}],\"id\":102}"
  	  def result = sendJsonCommand(json)
}

def off() {

      def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.1\",\"params\":[{\"status\":standby}],\"id\":102}"
  	  def result = sendJsonCommand(json)
}