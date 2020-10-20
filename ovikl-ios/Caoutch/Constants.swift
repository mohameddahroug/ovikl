//
//  Constants.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 8/19/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import Foundation
import UIKit
class Constants{

    static var vehicles:[Vehicle] = [Vehicle]()
    static let NO_TRIP="NO_TRIP";
    static let PENDING="PENDING";
    static let RESERVED="RESERVED";
    static let STARTED="STARTED";
    static let FINISHED="FINISHED";
    static let CANCELED="CANCELED";
    static let TIMEOUT="TIMEOUT";
    static let TRIP_STATE="TRIP_STATE";
    
    static let ServiceToMainActivity="ServiceToMainActivity";
    static let MainActivityTOService="MainActivityTOService";
    static let driverHasTrip="driverHasTrip";
    static let cash="cash";
    static let visa="visa";
    //static let defaultColor=UIColor(red: 96/255, green: 125/255, blue: 139/255, alpha: 1);
    static let white=UIColor(red: 250/255, green: 250/255, blue: 250/255, alpha: 1);
    static let primaryTextColor=UIColor(red: 53/255, green:69/255, blue: 77/255, alpha: 1);
    static let colorPrimary=UIColor(red: 96/255, green: 125/255, blue: 139/255, alpha: 1);
    static let colorPrimaryDark=UIColor(red: 70/255, green: 91/255, blue: 102/255, alpha: 1);
    static let red=UIColor(red: 187/255, green: 0/255, blue: 0/255, alpha: 1);
    static let green=UIColor(red: 0/255, green: 114/255, blue: 2/255, alpha: 1);
    static let idImage="idImage";
    static let idImageSmall="idImageSmall";
    static let driverLicenseImage="driverLicenseImage";
    static let driverLicenseImageSmall="driverLicenseImageSmall";
    static let personalImage="personalImage";
    static let personalImageSmall="personalImageSmall";
    static let carLicenseImage="carLicenseImage";
    static let carLicenseImageSmall="carLicenseImageSmall";
    static let frontImage="frontImage";
    static let frontImageSmall="frontImageSmall";
    static let backImage="backImage";
    static let backImageSmall="backImageSmall";
    static let sideImage="sideImage";
    static let sideImageSmall="sideImageSmall";
    static var indexUrl="https://index.ovikl.com"
    static var url="https://ovikl.com"
    static let indexUrlDev="http://localhost:8080"
    static let urlDev="http://localhost"
    static var dateFormatter:DateFormatter{
        let d = DateFormatter();
        d.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        d.timeZone = TimeZone(identifier: "GMT");
        d.locale = Locale(identifier: "en_US_POSIX")
        return d;
    }
    
    static var dateFormatterShort:DateFormatter{
        let d = DateFormatter();
        d.dateFormat = "yyyy-MM-dd";
        //d.timeZone = TimeZone(identifier: "GMT");
        d.locale = Locale(identifier: "en_US_POSIX")
        return d;
    }
    
    static var dateFormatterTime:DateFormatter{
        let d = DateFormatter();
        d.dateFormat = "HH:mm:ss"
        //d.timeZone = TimeZone(identifier: "GMT");
        d.locale = Locale(identifier: "en_US_POSIX")
        return d;
    }
     static var dateFormatterLocal:DateFormatter{
        let d = DateFormatter();
        d.dateFormat = "yyyy-MM-dd HH:mm'";
        
        d.timeZone = TimeZone(identifier: (TimeZone.current.abbreviation() ?? ""));
        d.locale = Locale(identifier: "en_US_POSIX")
        return d;
    }
    
    static func getKey(s:String)->Int{
        var singleCharString = s as NSString
        
        let prime:Int=1907;
        var k:Int=0;
        for i in 0 ..< s.count{
            k=k+Int(singleCharString.character(at: i));
        }
        return (k%prime)+5903;
    }
    
    /*static func getKey(parameters: [String:String])->[String:String]{
        var dateFormatter=DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        dateFormatter.timeZone = TimeZone(identifier: "GMT");
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
        var par=parameters;
        if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
            par["ver"] = "i"+version;
        }
        if let lang = Locale.current.languageCode {
            par["lang"]=lang;
        }
        //var msgId=UUID().uuidString;
        var time = dateFormatter.string(from: Date());
        //par["msgId"]=msgId;
        par["time"]=time;
        //par["ver"]=version;
        //par["lang"]=lang;
        
        var keyString=""//+time+version+lang;
        if let str = parameters["time"] {keyString=keyString+str }
        if let str = parameters["ver"] {keyString=keyString+str }
        if let str = parameters["lang"] {keyString=keyString+str }
        if let str = parameters["lat"] {keyString=keyString+str }
        if let str = parameters["lng"] {keyString=keyString+str }
        if let str = parameters["type"] {keyString=keyString+str }
        if let str = parameters["userId"] {keyString=keyString+str }
        if let str = parameters["lastMessageId"] {keyString=keyString+str }
        if let str = parameters["user_id"] {keyString=keyString+str }
        if let str = parameters["id"] {keyString=keyString+str }
        if let str = parameters["driverRate"] {keyString=keyString+str }
        if let str = parameters["carRate"] {keyString=keyString+str }
        if let str = parameters["clientId"] {keyString=keyString+str }
        if let str = parameters["clientRate"] {keyString=keyString+str }
        if let str = parameters["driverId"] {keyString=keyString+str }
        let key = Constants.getKey(s:keyString);
        par["key"]=String(key) ;
        return par;
    }*/
    
   
      
}


