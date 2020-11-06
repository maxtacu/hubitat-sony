/**
 *  Sony Soundbar RestfulAPI
 *  Hubitat Integration
 *  IMPORT URL: https://raw.githubusercontent.com/jonesalexr/hubitat/master/SonyAudioDriver.groovy
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
