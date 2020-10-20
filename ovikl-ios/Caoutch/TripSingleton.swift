//
//  TripSingleton.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 8/3/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import Foundation
import CoreData

class TripSingelton{
    static var mInstance = TripSingelton();
    var _id:String!;
    var createTime:Date!;
    var updateTime:Date!;
    var state:String!;
    var clientId:String!;
    var driverId:String!;
    var clientLat:Double!;
    var clientLng:Double!;
    var driverLat:Double!;
    var driverLng:Double!;
    var prMin:Double!;
    var prBase:Double!;
    var prKM:Double!;
    var prMinute:Double!;
    /*var prLngKM:Double!;
    var prLngMinute:Double!;
    var lngKM:Double!;*/
    var cancelledBy:String!;
    var cur:String!;
    var distance:Double!;
    var duration:Double!;
    var cost:Double!;
    var lang:String!;
    var dateFormatter=DateFormatter();
    var msgId:String!
    var promoPercentage:Double!;
    var maxPromoAmount:Double!;
    var paymentMethod:String!;
    var stripeLast4:String!;
    var stripeCustomerId:String!;
    var appDelegate:AppDelegate!
    private init(){
        lang=Locale.preferredLanguages[0];
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        dateFormatter.timeZone = TimeZone(identifier: "GMT");
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
    }
    
    public func saveTrip(managedContext:NSManagedObjectContext){
        print("func saveTrip")
        let tripEntity = NSEntityDescription.entity(forEntityName: "Trip", in: managedContext)!
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>(entityName: "Trip")
        let trip:NSManagedObject ;
        fetchRequest.predicate = NSPredicate(format: "tripId = %@ AND state = %@",argumentArray: [_id, state])
        
        do {
            let results = try managedContext.fetch(fetchRequest) as? [NSManagedObject]
            if results?.count != 0{
                if let trip2=results?.first{
                    trip=trip2;
                }
                else{
                    trip = NSManagedObject(entity: tripEntity, insertInto: managedContext);
                }
            }
            else{
                trip = NSManagedObject(entity: tripEntity, insertInto: managedContext);
            }
        } catch {
            print("Fetch Failed: \(error)");
            trip = NSManagedObject(entity: tripEntity, insertInto: managedContext);
        }
        
        
        trip.setValue(_id, forKeyPath: "tripId");
        trip.setValue(createTime, forKeyPath: "createTime");
        trip.setValue(updateTime, forKeyPath: "updateTime");
        trip.setValue(state, forKeyPath: "state");
        trip.setValue(clientId, forKeyPath: "clientId");
        trip.setValue(driverId, forKeyPath: "driverId");
        trip.setValue(clientLat, forKeyPath: "clientLat");
        trip.setValue(clientLng, forKeyPath: "clientLng");
        trip.setValue(driverLat, forKeyPath: "driverLat");
        trip.setValue(driverLng, forKeyPath: "driverLng");
        trip.setValue(prMin, forKeyPath: "prMin");
        trip.setValue(prBase, forKeyPath: "prBase");
        trip.setValue(prKM, forKeyPath: "prKM");
        trip.setValue(prMinute, forKeyPath: "prMinute");
        /*trip.setValue(prLngKM, forKeyPath: "prLngKM");
        trip.setValue(prLngMinute, forKeyPath: "prLngMinute");
        trip.setValue(lngKM, forKeyPath: "lngKM");*/
        trip.setValue(cancelledBy, forKeyPath: "cancelledBy");
        trip.setValue(cur, forKeyPath: "cur");
        trip.setValue(distance, forKeyPath: "distance");
        trip.setValue(duration, forKeyPath: "duration");
        trip.setValue(cost, forKeyPath: "cost");
        trip.setValue(msgId, forKeyPath: "msgId");
        trip.setValue(promoPercentage, forKeyPath: "promoPercentage");
        trip.setValue(maxPromoAmount, forKeyPath: "maxPromoAmount");
        trip.setValue(paymentMethod, forKeyPath: "paymentMethod");
        trip.setValue(stripeLast4, forKeyPath: "stripeLast4");
        trip.setValue(stripeCustomerId, forKeyPath: "stripeCustomerId");
        do {
            try managedContext.save()
            print("trip \(_id) \(state) is saved")
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    
    }
    
    public func setTrip(trip:[String:Any]){
        if let _id=trip["_id"] as? String{self._id=_id};
        if let createTimeStr=trip["createTime"] as? String,let createTime=dateFormatter.date(from: createTimeStr) {self.createTime=createTime};
        if let updateTimeStr=trip["updateTime"] as? String,let updateTime=dateFormatter.date(from: updateTimeStr) {self.updateTime=updateTime};
        if let state=trip["state"] as? String{self.state=state};
        if let clientId=trip["clientId"] as? String{self.clientId=clientId};
        if let driverId=trip["driverId"] as? String{self.driverId=driverId};
        if let clientLat=trip["clientLat"] as? String{self.clientLat=Double(clientLat)};
        if let clientLng=trip["clientLng"] as? String{self.clientLng=Double(clientLng)};
        if let driverLat=trip["driverLat"] as? String{self.driverLat=Double(driverLat)};
        if let driverLng=trip["driverLng"] as? String{self.driverLng=Double(driverLng)};
        if let prMin=trip["prMin"] as? Double{self.prMin=prMin};
        if let prBase=trip["prBase"] as? Double{self.prBase=prBase};
        if let prKM=trip["prKM"] as? Double{self.prKM=prKM};
        if let prMinute=trip["prMinute"] as? Double{self.prMinute=prMinute};
//        if let prLngKM=trip["prLngKM"] as? Double{self.prLngKM=prLngKM};
//        if let prLngMinute=trip["prLngMinute"] as? Double{self.prLngMinute=prLngMinute};
//        if let lngKM=trip["lngKM"] as? Double{self.lngKM=lngKM};
        if let cancelledBy=trip["cancelledBy"] as? String{self.cancelledBy=cancelledBy};
        
        if let distance=trip["distance"] as? Double{self.distance=distance};
        if let duration=trip["duration"] as? Double{self.duration=duration};
        if let cost=trip["cost"] as? Double{self.cost=cost};
        if let msgId=trip["msgId"] as? String{self.msgId=msgId};
        
        if let cur=trip[lang+"cur"] as? String{
            self.cur=cur
        }
        else if let cur=trip["cur"] as? String{
            self.cur=cur
        }
        if let promoPercentage=trip["promoPercentage"] as? Double{self.promoPercentage=promoPercentage};
        if let maxPromoAmount=trip["maxPromoAmount"] as? Double{self.maxPromoAmount=maxPromoAmount};
        if let paymentMethod=trip["paymentMethod"] as? String{self.paymentMethod=paymentMethod};
        if let stripeLast4=trip["stripeLast4"] as? String{self.stripeLast4=stripeLast4};
        if let stripeCustomerId=trip["stripeCustomerId"] as? String{self.stripeCustomerId=stripeCustomerId};
    }
    
    public func setObject(trip:NSManagedObject){
        if let _id=trip.value(forKey:"tripId") as? String{self._id=_id};
        if let createTime=trip.value(forKey:"createTime") as? Date {self.createTime=createTime};
        if let updateTime=trip.value(forKey:"updateTime") as? Date {self.updateTime=updateTime};
        if let state=trip.value(forKey:"state") as? String{self.state=state};
        if let clientId=trip.value(forKey:"clientId") as? String{self.clientId=clientId};
        if let driverId=trip.value(forKey:"driverId") as? String{self.driverId=driverId};
        if let clientLat=trip.value(forKey:"clientLat") as? Double{self.clientLat=clientLat};
        if let clientLng=trip.value(forKey:"clientLng") as? Double{self.clientLng=clientLng};
        if let driverLat=trip.value(forKey:"driverLat") as? Double{self.driverLat=driverLat};
        if let driverLng=trip.value(forKey:"driverLng") as? Double{self.driverLng=driverLng};
        if let prMin=trip.value(forKey:"prMin") as? Double{self.prMin=prMin};
        if let prBase=trip.value(forKey:"prBase") as? Double{self.prBase=prBase};
        if let prKM=trip.value(forKey:"prKM") as? Double{self.prKM=prKM};
        if let prMinute=trip.value(forKey:"prMinute") as? Double{self.prMinute=prMinute};
//        if let prLngKM=trip.value(forKey:"prLngKM") as? Double{self.prLngKM=prLngKM};
//        if let prLngMinute=trip.value(forKey:"prLngMinute") as? Double{self.prLngMinute=prLngMinute};
//        if let lngKM=trip.value(forKey:"lngKM") as? Double{self.lngKM=lngKM};
        if let cancelledBy=trip.value(forKey:"cancelledBy") as? String{self.cancelledBy=cancelledBy};
        if let cur=trip.value(forKey:"cur") as? String{self.cur=cur};
        if let distance=trip.value(forKey:"distance") as? Double{self.distance=distance};
        if let duration=trip.value(forKey:"duration") as? Double{self.duration=duration};
        if let cost=trip.value(forKey:"cost") as? Double{self.cost=cost};
        if let msgId=trip.value(forKey:"msgId") as? String{self.msgId=msgId};
        if let promoPercentage=trip.value(forKey:"promoPercentage") as? Double{self.promoPercentage=promoPercentage};
        if let maxPromoAmount=trip.value(forKey:"maxPromoAmount") as? Double{self.maxPromoAmount=maxPromoAmount};
        if let paymentMethod=trip.value(forKey:"paymentMethod") as? String{self.paymentMethod=paymentMethod};
        if let stripeLast4=trip.value(forKey:"stripeLast4") as? String{self.stripeLast4=stripeLast4};
        if let stripeCustomerId=trip.value(forKey:"stripeCustomerId") as? String{self.stripeCustomerId=stripeCustomerId};
    }
    
    
    public func setResponseTrip(trip:TCPResponse.Trip){
        if trip._id != nil{
            _id=trip._id
        }
        if trip.createTime != nil{
            createTime=trip.createTime
        }
        if trip.updateTime != nil{
            updateTime=trip.updateTime
        }
        if trip.state != nil{
            state=trip.state
        }
        if trip.clientId != nil{
            clientId=trip.clientId
        }
        if trip.driverId != nil{
            driverId=trip.driverId
        }
        if trip.clientLat != nil{
            clientLat=trip.clientLat
        }
        if trip.clientLng != nil{
            clientLng=trip.clientLng
        }
        if trip.driverLat != nil{
            driverLat=trip.driverLat
        }
        if trip.driverLng != nil{
            driverLng=trip.driverLng
        }
        if trip.prMin != nil{
            prMin=trip.prMin
        }
        if trip.prBase != nil{
            prBase=trip.prBase
        }
        if trip.prKM != nil{
            prKM=trip.prKM
        }
        if trip.prMinute != nil{
            prMinute=trip.prMinute
        }
//        prLngKM=trip.prLngKM
//        prLngMinute=trip.prLngMinute
//        lngKM=trip.lngKM
        if trip.cancelledBy != nil{
            cancelledBy=trip.cancelledBy
        }
        if trip.cur != nil{
            cur=trip.cur
        }
        if trip.distance != nil{
            distance=trip.distance
        }
        if trip.duration != nil{
            duration=trip.duration
        }
        if trip.cost != nil{
            cost=trip.cost
        }
        if trip.msgId != nil{
            msgId=trip.msgId
        }
    }
    
    
    
    
    
    public func getMap()->[String: Any]{
        var parameters: [String: Any]=[:];
        
        parameters["type"]=appDelegate.user.type
        parameters["auth_id"]=appDelegate.user._id
        parameters["time"]=Constants.dateFormatter.string(from: Date());
        parameters["msgId"]=UUID().uuidString;
        parameters["lang"] = Locale.current.languageCode
        parameters["ver"] = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
        
        if let _=_id {parameters["_id"]=_id;}
        //if let _=createTime {parameters["createTime"]=dateFormatter.string(from: createTime);}
        //if let _=updateTime {parameters["updateTime"]=dateFormatter.string(from: updateTime);}
        if let _=state {parameters["state"]=state;}
        if let _=clientId {parameters["clientId"]=clientId;}
        if let _=driverId {parameters["driverId"]=driverId;}
        if let _=clientLat {parameters["clientLat"]=String(clientLat);}
        if let _=clientLng {parameters["clientLng"]=String(clientLng);}
        if let _=driverLat {parameters["driverLat"]=String(driverLat);}
        if let _=driverLng {parameters["driverLng"]=String(driverLng);}
        /*
        if let _=prMin {parameters["prMin"]=prMin;}
        if let _=prBase {parameters["prBase"]=prBase;}
        if let _=prKM {parameters["prKM"]=prKM;}
        if let _=prMinute {parameters["prMinute"]=prMinute;}
        if let _=prLngKM {parameters["prLngKM"]=prLngKM;}
        if let _=prLngMinute {parameters["prLngMinute"]=prLngMinute;}
        if let _=lngKM {parameters["lngKM"]=lngKM;}*/
        if let _=cur {parameters["cur"]=cur;}
         
        if let _=cancelledBy {parameters["cancelledBy"]=cancelledBy;}
        if let _=distance {parameters["distance"]=distance;}
        if let _=duration {parameters["duration"]=duration;}
        if let _=cost {parameters["cost"]=cost;}
//        if let _=promoPercentage {parameters["promoPercentage"]=promoPercentage;}
//        if let _=maxPromoAmount {parameters["maxPromoAmount"]=maxPromoAmount;}
//        if let _=paymentMethod {parameters["paymentMethod"]=paymentMethod;}
//        if let _=stripeLast4 {parameters["stripeLast4"]=stripeLast4;}
//        if let _=stripeCustomerId {parameters["stripeCustomerId"]=stripeCustomerId;}
        return parameters
    }
    
    public func reset(){
        
        _id=nil;
        createTime=nil;
        updateTime=nil;
        //startTime=nil;
        //endTime=nil;
        state=nil;
        clientId=nil;
        driverId=nil;
        clientLat=nil;
        clientLng=nil;
        driverLat=nil;
        driverLng=nil;
        prMin=nil;
        prBase=nil;
        prKM=nil;
        prMinute=nil;
//        prLngKM=nil;
//        prLngMinute=nil;
//        lngKM=nil;
        cur=nil;
        cancelledBy=nil;
        distance=nil;
        duration=nil;
        cost=nil;
        msgId=nil;
        promoPercentage=nil;
        maxPromoAmount=nil;
        paymentMethod=nil;
        stripeLast4=nil;
        stripeCustomerId=nil;
    }
}
