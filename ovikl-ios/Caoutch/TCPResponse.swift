import Foundation
class TCPResponse: Codable{
    var event:String!
    var isEmpty:Bool!
    var user:User!
    var retry:Bool!
    var trip:TCPResponse.Trip!
    var tripMessage:TCPResponse.TripMessage!
    var tripArr:[TCPResponse.Trip]!
    var tripMessageArr:[TCPResponse.TripMessage]!
    var driverLocation:TCPResponse.Location!
    var _id:String!
    var type:String!
    var latitude:Double!
    var longitude:Double!
    var distance:Double!
    var duration:Double!
    var cost:Double!
    var carType:String!
    var msgId:String!
    var clientId:String!
    var driverId:String!
    var tripId:String!
    required init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        
        if values.contains(.event){
            event = try values.decode(String.self, forKey: .event)
        }
        if values.contains(._id){
            _id = try values.decode(String.self, forKey: ._id)
        }
        if values.contains(.type){
            type = try values.decode(String.self, forKey: .type)
        }
        if values.contains(.msgId){
            msgId = try values.decode(String.self, forKey: .msgId)
        }
        
        
        if values.contains(.user){
            user = try values.decode(User.self, forKey: .user)
        }
        
        if values.contains(.trip){
            trip = try values.decode(TCPResponse.Trip.self, forKey: .trip)
        }
        
        if values.contains(.tripMessage){
            tripMessage = try values.decode(TCPResponse.TripMessage.self, forKey: .tripMessage)
        }
        
        if values.contains(.driverLocation){
            do{
                driverLocation = try values.decode(TCPResponse.Location.self, forKey: .driverLocation)
            }
            catch{
                driverLocation = nil
            }
        }
        
        
        if values.contains(.tripArr){
            tripArr = try values.decode([TCPResponse.Trip].self, forKey: .tripArr)
        }
        
        if values.contains(.tripMessageArr){
            tripMessageArr = try values.decode([TCPResponse.TripMessage].self, forKey: .tripMessageArr)
        }
        
        
        
        
        do{
            if values.contains(.isEmpty){
                isEmpty = try values.decode(Bool.self, forKey: .isEmpty)
            }
        }
        catch{
            isEmpty = try Bool(values.decode(String.self, forKey: .isEmpty))
        }
        
        do{
            if values.contains(.retry){
                retry = try values.decode(Bool.self, forKey: .retry)
            }
        }
        catch{
            retry = try Bool(values.decode(String.self, forKey: .retry))
        }
        
        do{
            if values.contains(.latitude){
                latitude = try values.decode(Double.self, forKey: .latitude)
            }
        }
        catch{
            latitude = try Double(values.decode(String.self, forKey: .latitude))
        }
        do{
            if values.contains(.longitude){
                longitude = try values.decode(Double.self, forKey: .longitude)
            }
        }
        catch{
            longitude = try Double(values.decode(String.self, forKey: .longitude))
        }
        do{
            if values.contains(.distance){
                distance = try values.decode(Double.self, forKey: .distance)
            }
        }
        catch{
            distance = try Double(values.decode(String.self, forKey: .distance))
        }
        do{
            if values.contains(.duration){
                duration = try values.decode(Double.self, forKey: .duration)
            }
        }
        catch{
            duration = try Double(values.decode(String.self, forKey: .duration))
        }
        do{
            if values.contains(.cost){
                cost = try values.decode(Double.self, forKey: .cost)
            }
        }
        catch{
            cost = try Double(values.decode(String.self, forKey: .cost))
        }
        
        if values.contains(.carType){
            carType = try values.decode(String.self, forKey: .carType)
        }
        
        if values.contains(.clientId){
            clientId = try values.decode(String.self, forKey: .clientId)
        }
        
        if values.contains(.driverId){
            driverId = try values.decode(String.self, forKey: .driverId)
        }
        
        if values.contains(.tripId){
            tripId = try values.decode(String.self, forKey: .tripId)
        }
    }
    
    
    public class Trip: Codable{
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
//        var prLngKM:Double!;
//        var prLngMinute:Double!;
//        var lngKM:Double!;
        var cancelledBy:String!;
        var cur:String!;
        var distance:Double!;
        var duration:Double!;
        var cost:Double!;
        var lang:String!;
        var msgId:String!
        var client:User!
        var driver:User!
        var PENDING:Trip!
        var RESERVED:Trip!
        var STARTED:Trip!
        var FINISHED:Trip!
        var CANCELED:Trip!
        var locations:[Location]!
        var zone:String!;
        
        required init(from decoder: Decoder) throws {
            let values = try decoder.container(keyedBy: CodingKeys.self)
            if values.contains(._id){
                _id = try values.decode(String.self, forKey: ._id)
            }
            
            do{
                if values.contains(.createTime){
                    createTime = try values.decode(Date.self, forKey: .createTime)
                }
            }
            catch{
                let s = try values.decode(String.self, forKey: .createTime)
                createTime = Constants.dateFormatter.date(from: s)
            }
            
            
            do{
                if values.contains(.updateTime){
                    updateTime = try values.decode(Date.self, forKey: .updateTime)
                }
            }
            catch{
                let s = try values.decode(String.self, forKey: .updateTime)
                updateTime = Constants.dateFormatter.date(from: s)
            }
            
            if values.contains(.state){
                state = try values.decode(String.self, forKey: .state)
            }
            
            if values.contains(.clientId){
                           clientId = try values.decode(String.self, forKey: .clientId)
                       }
            if values.contains(.driverId){
                       driverId = try values.decode(String.self, forKey: .driverId)
                   }
            do{
                if values.contains(.clientLat){
                    clientLat = try values.decode(Double.self, forKey: .clientLat)
                }
            }
            catch{
                clientLat = try Double(values.decode(String.self, forKey: .clientLat))
            }
            
            do{
                if values.contains(.clientLng){
                    clientLng = try values.decode(Double.self, forKey: .clientLng)
                }
            }
            catch{
                clientLng = try Double(values.decode(String.self, forKey: .clientLng))
            }
            
            do{
                if values.contains(.driverLat){
                    driverLat = try values.decode(Double.self, forKey: .driverLat)
                }
            }
            catch{
                driverLat = try Double(values.decode(String.self, forKey: .driverLat))
            }
            
            do{
                if values.contains(.driverLng){
                    driverLng = try values.decode(Double.self, forKey: .driverLng)
                }
            }
            catch{
                driverLng = try Double(values.decode(String.self, forKey: .driverLng))
            }
            
            do{
                if values.contains(.prMin){
                    prMin = try values.decode(Double.self, forKey: .prMin)
                }
            }
            catch{
                prMin = try Double(values.decode(String.self, forKey: .prMin))
            }
            
            do{
                if values.contains(.prBase){
                    prBase = try values.decode(Double.self, forKey: .prBase)
                }
            }
            catch{
                prBase = try Double(values.decode(String.self, forKey: .prBase))
            }
            
            do{
                if values.contains(.prKM){
                    prKM = try values.decode(Double.self, forKey: .prKM)
                }
            }
            catch{
                prKM = try Double(values.decode(String.self, forKey: .prKM))
            }
            
            do{
                if values.contains(.prMinute){
                    prMinute = try values.decode(Double.self, forKey: .prMinute)
                }
            }
            catch{
                prMinute = try Double(values.decode(String.self, forKey: .prMinute))
            }
            
//            do{
//                if values.contains(.prLngKM){
//                    prLngKM = try values.decode(Double.self, forKey: .prLngKM)
//                }
//            }
//            catch{
//                prLngKM = try Double(values.decode(String.self, forKey: .prLngKM))
//            }
//            
//            do{
//                if values.contains(.prLngMinute){
//                    prLngMinute = try values.decode(Double.self, forKey: .prLngMinute)
//                }
//            }
//            catch{
//                prLngMinute = try Double(values.decode(String.self, forKey: .prLngMinute))
//            }
//            
//            do{
//                if values.contains(.lngKM){
//                    lngKM = try values.decode(Double.self, forKey: .lngKM)
//                }
//            }
//            catch{
//                lngKM = try Double(values.decode(String.self, forKey: .lngKM))
//            }
            
            if values.contains(.cancelledBy){
                cancelledBy = try values.decode(String.self, forKey: .cancelledBy)
            }
            
            if values.contains(.cur){
                cur = try values.decode(String.self, forKey: .cur)
            }
            
            do{
                if values.contains(.distance){
                    distance = try values.decode(Double.self, forKey: .distance)
                }
            }
            catch{
                distance = try Double(values.decode(String.self, forKey: .distance))
            }
            
            do{
                if values.contains(.duration){
                    duration = try values.decode(Double.self, forKey: .duration)
                }
            }
            catch{
                duration = try Double(values.decode(String.self, forKey: .duration))
            }
            
            do{
                if values.contains(.cost){
                    cost = try values.decode(Double.self, forKey: .cost)
                }
            }
            catch{
                cost = try Double(values.decode(String.self, forKey: .cost))
            }
            
            
            if values.contains(.lang){
                lang = try values.decode(String.self, forKey: .lang)
            }
            
            if values.contains(.msgId){
                msgId = try values.decode(String.self, forKey: .msgId)
            }
            
            if values.contains(.client){
                client = try values.decode(User.self, forKey: .client)
            }
            
            if values.contains(.driver){
                driver = try values.decode(User.self, forKey: .driver)
            }
            
            if values.contains(.locations){
                locations = try values.decode([TCPResponse.Location].self, forKey: .locations)
            }
        }
    }
    
    class TripMessage: Codable{
        
        var tripId:String!;
        var state:String!;
        var message:String!;
        var msgId:String!;
        var createTime:String!;
        var tripMessageId:String!;
        var senderId:String!;
    }
    class Location: Codable{
        var i:String!;
        var state:String!;
        var latitude:Double!;
        var longitude:Double!;
        var time:String!;
        var duration:Double!;
        var distance:Double!;
        required init(from decoder: Decoder) throws {
            let values = try decoder.container(keyedBy: CodingKeys.self)
            do{
                latitude = try values.decode(Double.self, forKey: .latitude)
            }
            catch{
                latitude = try Double(values.decode(String.self, forKey: .latitude))
            }
            do{
                longitude = try values.decode(Double.self, forKey: .longitude)
            }
            catch{
                longitude = try Double(values.decode(String.self, forKey: .longitude))
            }
            do{
                duration = try values.decode(Double.self, forKey: .duration)
            }
            catch{
                do{
                    duration = try Double(values.decode(String.self, forKey: .duration))
                }
                catch{
                    duration = nil
                }
            }
            do{
                distance = try values.decode(Double.self, forKey: .distance)
            }
            catch{
                do {
                    distance = try Double(values.decode(String.self, forKey: .distance))
                }
                catch{
                    distance = nil
                }
            }
        }
    }
    
    
  
}




