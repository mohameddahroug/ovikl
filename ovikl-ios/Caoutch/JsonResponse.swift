//
//  JsonResponse.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 08/12/2019.
//  Copyright © 2019 Caoutch. All rights reserved.
//

import Foundation
class JsonResponse: Codable{
    
    
    var code:Int!;
    var message:String!;
    var user:User!;
    var users:[User]!;
    var trips:[TCPResponse.Trip]!;
    var messages:[String:NewMessage]!;
    var prices:[String:Price]!;
    var newMessage:NewMessage!;
    var vehicles:[Vehicle]!;
    //var vehiclesImages:[String:VehicleImages]!;
    var config:Config!;
    var zone:Zone!
    /*required init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if let value = try? container.decode(Int.self, forKey: .code) {
           code = String(value)
        }
        if let value = try? container.decode(String.self, forKey: .message) {
           message = value
        }
        if let value = try? container.decode(User.self, forKey: .user) {
           user = value
        }
   }*/
  
}


class NewMessage: Codable{
      var _id:String!;
      var message:String!;
      var createTime:String!;
  }
class Price: Codable{
    var type:String!;
    var typeAr:String!;
    var prMin:Double!;
    var prBase:Double!;
    var prKM:Double!;
    var prMinute:Double!;
    var prLngKM:Double!;
    var prLngMinute:Double!;
    var lngKM:Double!;
    var cur:String!;
}
class Vehicle: Codable{
    var type:String!;
    var name:String!;
    var image:String!;
    var pointer:String!
    var selectedPointer:String!
}
class Server: Codable{
    var ip:String!;
    var port:Int!;
}


//class VehicleImages: Codable{
//    var type:String!;
//    var image:String!;
//    var pointer:String!;
//    var selectedPointer:String!;
//}

class Config: Codable{
    var minAndroidVersion:Int!=0;
    var minIOSVersion:Int!=0;
    var timeout:Int!=5000;
    //var timeoutLong=10000;
    var interval:Int!=10000;
    var fastInterval:Int!=3000;
    //var intervalLong=15000;
    //var fastIntervalLong=10000;
    var smallImage:Int!=100;
    var largeImage:Int!=500;
    
    
    /*func load(){
        let defaults = UserDefaults.standard
        if let jsonString = defaults.string(forKey: "config") {
            print("Config init",jsonString)
            let jsonData = jsonString.data(using: .utf8)!
            Constants.user  = try! JSONDecoder().decode(User.self, from: jsonData)
        }
    }*/
    
    
    func save(){
        do{
            let defaults = UserDefaults.standard
            let jsonEncoder = JSONEncoder()
            let jsonData = try jsonEncoder.encode(self)
            let json = String(data: jsonData, encoding: .utf8)
            defaults.set(json,forKey: "config")
        }
        catch{
            print(error.localizedDescription)
        }
    }
}

class Zone: Codable{
    var zone:String!
    var email:String!
    var mobile:String!
}
