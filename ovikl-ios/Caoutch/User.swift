//
//  User.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 8/1/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import Foundation
class User: Codable{
    
    
    
    //var id;
    var _id:String!;
    var fcmToken:String!;
    var type:String!;
    //var name;
    var email:String!;
    var clientStatus:String!;
    var driverStatus:String!;
    var adminStatus:String!;
    var carType:String!;
    //var stripeCustomerId;
    //var paymentMethod;
    //var stripeLast4;
    var ads:Int! = 1;
    //public Integer stripe=0;
    //var userName;

    var firstName:String!;
    var lastName:String!;
    //var password;
    var mobile:String!;
    var emailVerified:Bool! = false;
    var mobileVerified:Bool! = false;

    var driverLicenseNumber:String!;
    var carLicenseNumber:String!;
    var carManufacturer:String!;
    var carModel:String!;
    var carMadeYear:String!;
    var carColor:String!;
    var carNumber:String!;
    var idNumber:String!;
    var hashedKey:String!;
    var createDate:Date!;
    var driverRate:Float!;
    var clientRate:Float!;
    var carRate:Float!;
    var totalDistance:Float!;
    var claimsCount:Int!;
    var tripsCount:Int!;
    var totalHours:Float!;
    var iosToken:String!
    
    var images:Images!
    var cost:Cost!
    var zone:String!
    var zoneContact:Zone!;
    
    func isClient()->Bool{
        if(type=="client"){
            return true;
        }
        else{
            return false;
        }
    }
    
    
    func isDriver()->Bool{
        if(type=="driver"){
            return true;
        }
        else{
            return false;
        }
    }
    
   func isAdmin()->Bool{
       if(type=="admin"||type=="super_admin"){
           return true;
       }
       else{
           return false;
       }
   }
    
   func isSuperAdmin()->Bool{
        if(type=="admin"||type=="super_admin"){
            return true;
        }
        else{
            return false;
        }
    }
    
    func getStatus()->String!{
        if type == "client"{
            return clientStatus
        }
        else if type == "driver"{
            return driverStatus
        }
        else{
            return adminStatus
        }
    }
    
    func setStatus(s:String){
        
        if type == "client"{
             clientStatus=s
        }
        else if type == "driver"{
             driverStatus=s
        }
        else{
             adminStatus=s
        }
    }
    
}


class Images: Codable{
    var driverLicenseImage:String!;
    var driverLicenseImageSmall:String!;
    var idImage:String!;
    var idImageSmall:String!;
    var personalImage:String!;
    var personalImageSmall:String!;
    var carLicenseImage:String!;
    var carLicenseImageSmall:String!;
    var frontImage:String!;
    var frontImageSmall:String!;
    var sideImage:String!;
    var sideImageSmall:String!;
    var backImage:String!;
    var backImageSmall:String!;
}

class Cost: Codable{
    var base:Double!;
    var km:Double!;
    var minimum:Double!;
    var minute:Double!;
    var currency:String!
    
    
    required init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        do{
            base = try values.decode(Double.self, forKey: .base)
        }
        catch{
            base = try Double(values.decode(String.self, forKey: .base))
        }
        
        do{
            km = try values.decode(Double.self, forKey: .km)
        }
        catch{
            km = try Double(values.decode(String.self, forKey: .km))
        }
        
        do{
            minimum = try values.decode(Double.self, forKey: .minimum)
        }
        catch{
            minimum = try Double(values.decode(String.self, forKey: .minimum))
        }
        
        do{
            minute = try values.decode(Double.self, forKey: .minute)
        }
        catch{
            minute = try Double(values.decode(String.self, forKey: .minute))
        }
        
        currency = try values.decode(String.self, forKey: .currency)
    }
}
