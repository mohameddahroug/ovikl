//
//  swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 7/29/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import UIKit
import CoreData
import Alamofire
import UserNotifications
import GoogleMobileAds
import CoreLocation
import MapKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?
    var tripSingleton=TripSingelton.mInstance;
    var timer:Timer!
    var isLogin=false;
    var socketOpened=false;
    var server:String!;
    var port:Int!
    var iosToken = ""
    var user = User();
    var config = Config();
    var isViewControllerforeground=false
    var isOnline=true
    var viewController:ViewController?
    let jsonDecoder=JSONDecoder()
    let jsonEncoder = JSONEncoder()
    var queue = [[String:Any]]();
    var inputStream:InputStream!
    var outputStream:OutputStream!
    var lastRecievedTCP:Date!
    var locationManager: CLLocationManager?
    var mostRecentLocation:CLLocation!
    var lastLocation:CLLocation!;
    var lastLocationTime:Date!;
    var driverTripLocation:CLLocation!;
    var distance:Double=0;
    var duration:Double=0;
    var cost:Double = 0;
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        print("AppDelegate func application didFinishLaunchingWithOptions")
        //GMSServices.provideAPIKey("AIzaSyAtIIpyXo2Gmy_XPeGIb0RiILTYgeJb84c")
        //GADMobileAds.configure(withApplicationID: "ca-app-pub-6615275988084929~4953378785")
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        // Register with APNs
        UIApplication.shared.registerForRemoteNotifications()
        tripSingleton.appDelegate=self
        jsonDecoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
        let manager = CLLocationManager()
        if self.user.isClient(){
            manager.desiredAccuracy = kCLLocationAccuracyBest
        }
        else{
            manager.desiredAccuracy=kCLLocationAccuracyBestForNavigation
        }
        
        manager.delegate = self
        locationManager=manager
        return true
    }
    
    /*func application(_ application: UIApplication,
                     performFetchWithCompletionHandler completionHandler:
        @escaping (UIBackgroundFetchResult) -> Void) {
        print("func application performFetchWithCompletionHandler")
        
        //let user=User.mInstance;
        //let appDelegate = UIApplication.shared.delegate as! AppDelegate
        //((window?.rootViewController as? UINavigationController)?.topViewController as? ViewController)?.login(false)
        completionHandler(.newData)
     
    }
    
    func showNotification(_ title:String, _ message:String){
        
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = message
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger) // Schedule the notification.
        let center = UNUserNotificationCenter.current()
        center.add(request) { (error : Error?) in
            if let theError = error {
                // Handle any errors
                print("Error in notification \(theError)")
            }
        }
    }*/

    func applicationWillResignActive(_ application: UIApplication) {
        print("AppDelegate func application applicationWillResignActive")
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
        
    }

    func applicationDidEnterBackground(_ application: UIApplication) {
        print("AppDelegate func application applicationDidEnterBackground")
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        let center = UNUserNotificationCenter.current()
        // Request permission to display alerts and play sounds.
        center.requestAuthorization(options: [.alert, .sound])
        { (granted, error) in
            // Enable or disable features based on authorization.
        }
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        print("AppDelegate func application applicationWillEnterForeground")
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        print("AppDelegate func application applicationDidBecomeActive")
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
        if viewController != nil{
            startTimer()
        }
    }

    func applicationWillTerminate(_ application: UIApplication) {
        print("AppDelegate func application applicationWillTerminate")
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        // Saves changes in the application's managed object context before the application terminates.
        
        //self.saveContext()
    }

    // MARK: - Core Data stack

    lazy var persistentContainer: NSPersistentContainer = {
        /*
         The persistent container for the application. This implementation
         creates and returns a container, having loaded the store for the
         application to it. This property is optional since there are legitimate
         error conditions that could cause the creation of the store to fail.
        */
        let container = NSPersistentContainer(name: "Caoutch")
        container.loadPersistentStores(completionHandler: { (storeDescription, error) in
            if let error = error as NSError? {
                // Replace this implementation with code to handle the error appropriately.
                // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                 
                /*
                 Typical reasons for an error here include:
                 * The parent directory does not exist, cannot be created, or disallows writing.
                 * The persistent store is not accessible, due to permissions or data protection when the device is locked.
                 * The device is out of space.
                 * The store could not be migrated to the current model version.
                 Check the error message to determine what the actual problem was.
                 */
                fatalError("Unresolved error \(error), \(error.userInfo)")
            }
        })
        return container
    }()

    // MARK: - Core Data Saving support

    func saveContext () {
        print("AppDelegate func application saveContext")
        if timer != nil{
               timer.invalidate()
               timer=nil
        }
        let context = persistentContainer.viewContext
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                // Replace this implementation with code to handle the error appropriately.
                // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                let nserror = error as NSError
                fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
            }
        }
    }
    
    
  
    func application(_ application: UIApplication,
                didRegisterForRemoteNotificationsWithDeviceToken
                    deviceToken: Data) {
        print("AppDelegate didRegisterForRemoteNotificationsWithDeviceToken")
        iosToken = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("AppDelegate deviceToken "+iosToken)
        
        if user._id == nil{
            load()
        }
        saveToken()
        
    }

    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        print("AppDelegate continue userActivity")
        return true;
    }
    
    
    func application(_ application: UIApplication,
                didFailToRegisterForRemoteNotificationsWithError
                    error: Error) {
       // Try again later.
        print("AppDelegate didFailToRegisterForRemoteNotificationsWithError",error.localizedDescription)
    }

    
    func saveToken(){
           print("AppDelegate saveToken")
           if user._id != nil && iosToken != "" && (user.iosToken == nil || user.iosToken != iosToken) {
               var parameters: [String: Any]=[:];
               parameters["_id"]=user._id
               parameters["hashedKey"]=user.hashedKey
               parameters["iosToken"]=iosToken
               parameters["fcmToken"]=""
               
               
               if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
                   parameters["ver"] = "i"+version;
               }
               if let lang = Locale.current.languageCode {
                   parameters["lang"]=lang;
               }
            parameters["time"]=Constants.dateFormatter.string(from: Date());
               
               Alamofire.request(Constants.indexUrl+"/register2/", method: .post, parameters: parameters).responseData { response in
                   
                   if let jsonData = response.data{
                       print(String(decoding: jsonData, as: UTF8.self))
                       let decoder = JSONDecoder()
                       //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                       
                       do{
                           let json = try decoder.decode(JsonResponse.self, from: jsonData)
                           print(json.code)
                           if(json.code==200){
                               if(json.user != nil){
                                self.user=json.user;
                                self.save()
                                  
                               }
                           }
                       }
                       catch let error {
                           print(error)
                       }
                   }
                   
                   
               }
           }
       }
     
    
    func load(){
           let defaults = UserDefaults.standard
           if let jsonString = defaults.string(forKey: "user") {
               print("User init",jsonString)
               let jsonData = jsonString.data(using: .utf8)!
               do{
               user = try! JSONDecoder().decode(User.self, from: jsonData)
               }
               catch{
                   user = User();
               }
           }
           
       }
       
       
    func save(){
           do{
               let defaults = UserDefaults.standard
               let jsonEncoder = JSONEncoder()
               let jsonData = try jsonEncoder.encode(user)
               let json = String(data: jsonData, encoding: .utf8)
               defaults.set(json,forKey: "user")
           }
           catch{
               print(error.localizedDescription)
           }
       }
       
       
        func reset(){
             let defaults = UserDefaults.standard
             defaults.removeObject(forKey: "user")
             user=User()
             save()
         }
    
    
    func getNewParameters()->[String:Any]{
          var parameters=[String:Any]();
          parameters["type"]=user.type
          parameters["auth_id"]=user._id
          parameters["time"]=Constants.dateFormatter.string(from: Date());
          parameters["msgId"]=UUID().uuidString;
          parameters["lang"] = Locale.current.languageCode
          parameters["ver"] = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
          return parameters
      }
    
    
    func disconnectTCP(){
        print("func disconnectTCP");
        if inputStream != nil {
            inputStream.close()
        }
        if outputStream != nil{
            outputStream.close()
        }
        if user.isClient() || user.isAdmin() || !isOnline {
            locationManager?.stopUpdatingLocation();
        }
        socketOpened=false
        isLogin=false
        server=nil
        port=nil
        lastRecievedTCP=nil
        
        //loadingView.isHidden=false
    }
    
    @objc func connectTCP(){
        print("func connectTCP");
        var inp :InputStream?
        var out :OutputStream?
        Stream.getStreamsToHost(withName:server, port: port, inputStream: &inp, outputStream: &out)
        
        inputStream = inp!
        outputStream = out!
        if server != "localhost"{
            inputStream.setProperty(kCFStreamSocketSecurityLevelNegotiatedSSL, forKey: Stream.PropertyKey.socketSecurityLevelKey)
            outputStream.setProperty(kCFStreamSocketSecurityLevelNegotiatedSSL, forKey: Stream.PropertyKey.socketSecurityLevelKey)
        }
        inputStream.delegate=self
        outputStream.delegate=self
        inputStream.schedule(in:.current, forMode: .default)
        outputStream.schedule(in:.current, forMode: .default)
        inputStream.open()
        outputStream.open()
        /*var readByte :UInt8 = 0
         var message:String=""
         DispatchQueue.main.async {
         while self.inputStream.hasBytesAvailable {
         self.inputStream.read(&readByte, maxLength: 1)
         
         let s = String(cString: &readByte)
         
         if s == "\n"{
         print(message)
         }
         else{
         message.append(s)
         }
         }
         }*/
        
        
        // buffer is a UInt8 array containing bytes of the string "Jonathan Yaniv.".
        var parameters=getNewParameters()
        parameters["event"]="login"
        parameters["_id"]=user._id
        parameters["hashedKey"]=user.hashedKey
        if tripSingleton._id != nil {
          parameters["tripId"]=tripSingleton._id
        }
        writeTCP(parameters)
        
        
    }
    
    func writeTCP(_ parameters:[String:Any]?){
        do{
            if outputStream != nil {
                if parameters == nil{
                    let s = "{}"
                    if let jsonData=s.data(using: .utf8){
                        let pointer=UnsafeMutablePointer<UInt8>.allocate(capacity: jsonData.count)
                        jsonData.copyBytes(to: pointer, count: jsonData.count)
                        let dataPointer = UnsafePointer<UInt8>(pointer)
                        outputStream.write(dataPointer, maxLength: jsonData.count)
                    }
                }
                else{
                    var jsonData = try JSONSerialization.data(withJSONObject: parameters)
                    jsonData.append(contentsOf: [10])
                    let pointer=UnsafeMutablePointer<UInt8>.allocate(capacity: jsonData.count)
                    jsonData.copyBytes(to: pointer, count: jsonData.count)
                    let dataPointer = UnsafePointer<UInt8>(pointer)
                    outputStream.write(dataPointer, maxLength: jsonData.count)
                }
            
            }
            
            
        }
        catch{
            print(error.localizedDescription)
        }
    }
    
    
    func addToQueue(_ parameters:[String:Any]){
        /*var par=parameters;
         var msgId=UUID().uuidString;
         var time = dateFormatter.string(from: Date());
         par["msgId"]=msgId;
         par["time"]=time;
         par["ver"]=version;
         par["lang"]=lang;*/
        
        
        queue.append(parameters);
        sentToServer();
    }
    
    
    
    func sentToServer(){
        print("func sentToServer");
        if queue.count>0&&socketOpened&&isLogin{
            if let par = queue.first{
                queue.removeFirst()
                if par["event"] != nil {
                    writeTCP(par)
                }
            }
        }
    }
    
    func connectServer(lat:String,lng:String){
           print("func connectServer"+" "+lat+" "+lng);
           var parameters = getNewParameters()
           parameters["lat"] = lat;
           parameters["lng"] = lng;
           
           
           // All three of these calls are equivalent
           Alamofire.request(Constants.indexUrl+"/tcp_server", method: .post, parameters: parameters).responseJSON { response in
               
               if let json = response.result.value as? [String: Any]{
                   print("JSON: \(json)");
                   if let code=json["code"] as? Double ,code==200, let server=json["server"] as? [String: Any],
                       let ip=server["ip"] as? String,let port=server["port"] as? Int{
                       if(self.server==nil){
                           self.server=ip;
                           self.port=port
                           self.connectTCP();
                       }
                   }
               }
               
           }
       }
       
    
    @objc func beat(){
        print("beat")
        if (user.isClient() || user.isAdmin()) && !isViewControllerforeground {
            return
        }
        else if user.isDriver() && !isOnline {
            return
        }
        else if(server==nil && mostRecentLocation != nil){
            //disconnectTCP()
            connectServer(lat: String(mostRecentLocation.coordinate.latitude), lng: String(mostRecentLocation.coordinate.longitude));
        }
            /*else if(!socketOpened && server != nil && isOnline){
             connectTCP()
             }*/
        else if(isLogin && socketOpened && isOnline && queue.count == 0){
            if user.isDriver() && mostRecentLocation != nil {
                var parameters=getNewParameters();
                parameters["event"]="location";
                parameters["_id"]=user._id
                parameters["type"]=user.type
                parameters["latitude"]=String(mostRecentLocation.coordinate.latitude);
                parameters["longitude"]=String(mostRecentLocation.coordinate.longitude);
                if user.isDriver(){
                    parameters["carType"]=user.carType
                }
                if tripSingleton._id != nil{
                    parameters["tripId"]=tripSingleton._id
                    if user.isDriver() {
                        parameters["clientId"]=tripSingleton.clientId
                    }
                    else{
                        parameters["driverId"]=tripSingleton.driverId
                    }
                }
                if  tripSingleton._id != nil{
                    let newLocationTime=Date()
                    //distance=distance+mostRecentLocation.distance(from: lastLatLng)
                    let tripLocationEntity = NSEntityDescription.entity(forEntityName: "TripLocation", in: persistentContainer.viewContext)!
                    let tripLocation:NSManagedObject = NSManagedObject(entity: tripLocationEntity, insertInto: persistentContainer.viewContext);
                    tripLocation.setValue(tripSingleton._id, forKey: "tripId")
                    tripLocation.setValue(tripSingleton.state, forKey: "state")
                    tripLocation.setValue(newLocationTime, forKey: "time")
                    tripLocation.setValue(mostRecentLocation.coordinate.latitude, forKey: "latitude")
                    tripLocation.setValue(mostRecentLocation.coordinate.longitude, forKey: "longitude")
                    if tripSingleton.state==Constants.STARTED,
                        let lastLocation=lastLocation,let lastLocationTime=lastLocationTime{
                        duration=duration+newLocationTime.timeIntervalSince1970 - lastLocationTime.timeIntervalSince1970// in seconds
                        distance=distance+mostRecentLocation.distance(from: lastLocation)
                        
                        //                        if ((distance / 1000) < tripSingleton.lngKM) {
                        cost = tripSingleton.prBase + ((distance / 1000) * tripSingleton.prKM) + ((duration / 60) * tripSingleton.prMinute);
                        //                        } else {
                        //                            cost = tripSingleton.prBase + ((distance / 1000) * tripSingleton.prLngKM) + ((duration / 60) * tripSingleton.prLngMinute);
                        //                        }
                        if (cost < tripSingleton.prMin) {
                            cost = tripSingleton.prMin;
                        }
                    }
                    if tripSingleton.promoPercentage != nil && tripSingleton.promoPercentage>0 {
                        
                        if tripSingleton.maxPromoAmount != nil && tripSingleton.maxPromoAmount>0 {
                            var promoCost=cost*(tripSingleton.promoPercentage)/100;
                            if promoCost < tripSingleton.maxPromoAmount {
                                cost=cost-promoCost;
                            }
                            else{
                                cost=cost-tripSingleton.maxPromoAmount;
                            }
                        }
                        else{
                            cost=cost*(100-tripSingleton.promoPercentage)/100;
                        }
                    }
                    
                    if distance>0 || duration>0 || cost>0{
                        parameters["distance"]=String(format: "%.2f", distance)
                        parameters["duration"]=String(format: "%.2f", duration*1000)
                        parameters["cost"]=String(format: "%.2f", cost)
                        tripLocation.setValue(distance, forKey: "distance")
                        tripLocation.setValue(duration, forKey: "duration")
                        if let viewController = viewController,
                            let tripDistanceTextField = viewController.tripDistanceTextField,
                            let tripDurationTextField = viewController.tripDurationTextField,
                            let tripCostTextField = viewController.tripCostTextField
                            {
                            tripDistanceTextField.text=String(format:"%.02f",distance/1000)+" "+NSLocalizedString("km",comment:"")
                            tripDurationTextField.text=String(format:"%.02f",duration/60)+" "+NSLocalizedString("minutes",comment:"")
                            tripCostTextField.text=String(format:"%.02f",cost)+" "+tripSingleton.cur
                        }
                    }
                    
                    lastLocation=mostRecentLocation;
                    lastLocationTime=newLocationTime;
                    do{
                        try persistentContainer.viewContext.save()
                    }
                    catch{
                        print(error.localizedDescription)
                    }
                }
                writeTCP(parameters)
            }
            else if user.isClient() && tripSingleton._id == nil{
                
                if let viewController = viewController, let clientLocationMarker=viewController.clientLocationMarker{
                     var parameters=getNewParameters();
                     parameters["event"]="location";
                     parameters["_id"]=user._id
                     parameters["type"]=user.type
                     parameters["latitude"]=String(clientLocationMarker.coordinate.latitude);
                     parameters["longitude"]=String(clientLocationMarker.coordinate.longitude);
                     writeTCP(parameters)
                }
                else if(socketOpened && server != nil && isOnline){
                    writeTCP(nil)
                }
            }
                
            else if(socketOpened && server != nil && isOnline){
                writeTCP(nil)
            }
            
            
        }
    }
    
}




extension AppDelegate: CLLocationManagerDelegate {
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        print("func CLLocationManagerDelegate locationManager")
        guard let mostRecentLocation = locations.last else {
            return
        }
        
        if let viewController = viewController{
            //let camera = GMSCameraPosition.camera(withLatitude: mostRecentLocation.coordinate.latitude, longitude: mostRecentLocation.coordinate.longitude, zoom: defaultZoom)
            
            //mapView.camera=camera
            let coordinateRegion = MKCoordinateRegion(center: mostRecentLocation.coordinate, latitudinalMeters: viewController.regionRadius, longitudinalMeters: viewController.regionRadius)
            viewController.mapView.setRegion(coordinateRegion, animated: true)
            // Creates a marker in the center of the map.
            
            if self.user.isClient(){
                if viewController.clientLocationMarker==nil {
                    viewController.clientLocationMarker = PointAnnotation()
                    viewController.clientLocationMarker.coordinate = CLLocationCoordinate2D(latitude: mostRecentLocation.coordinate.latitude, longitude: mostRecentLocation.coordinate.longitude)
                    //clientLocationMarker.title = "Sydney"
                    //clientLocationMarker.snippet = "Australia"
                    viewController.clientLocationMarker.icon=UIImage(named: "client_pointer");
                    viewController.clientLocationMarker._id=self.user._id
                    viewController.mapView.addAnnotation(viewController.clientLocationMarker)
                    print("locationManager",mostRecentLocation.coordinate)
                    
                }
                else if viewController.cameraMoveEnable{
                    viewController.clientLocationMarker.coordinate = CLLocationCoordinate2D(latitude: mostRecentLocation.coordinate.latitude, longitude: mostRecentLocation.coordinate.longitude)
                }
                if server != nil && self.isLogin{
                    manager.stopUpdatingLocation();
                   
                }
            }
            else if self.user.isAdmin(){
                if server != nil && self.isLogin{
                    manager.stopUpdatingLocation();
                }
            }
            
            self.mostRecentLocation=mostRecentLocation
            /*if beatOnce {
                beatOnce=false
                beat()
            }*/
        }
    }
    
    func startTimer(){
        if timer == nil{
           if user.isDriver(){
               timer = Timer.scheduledTimer(timeInterval: Double(config.interval/1000), target: self, selector: #selector(beat), userInfo: nil, repeats: true)
              timer.fire()
           }
           else{
               timer = Timer.scheduledTimer(timeInterval: Double(config.timeout/1000), target: self, selector: #selector(beat), userInfo: nil, repeats: true)
              timer.fire()
           }
       }
       else{
           beat()
       }

    }
    
    func stopTimer(){
        if timer != nil{
            timer.invalidate()
            timer=nil
        }
    }
}


extension AppDelegate:StreamDelegate{
    
    func stream(_ aStream: Stream, handle eventCode: Stream.Event) {
        if let l = lastRecievedTCP, Int(Date().timeIntervalSince1970-l.timeIntervalSince1970) > (self.config.timeout/1000)+3 {
            viewController?.disconnect()
        }
        switch eventCode {
        case Stream.Event.endEncountered:
            print("TCP End Encountered")
            //inputStream.close()
            //outputStream.close()
            //aStream.close()
            //disconnect()
            break
        case Stream.Event.openCompleted:
            print("TCP Open Completed")
            self.socketOpened=true
            break
        case Stream.Event.hasSpaceAvailable:
            print("TCP Has Space Available")
            
        case Stream.Event.hasBytesAvailable:
            lastRecievedTCP=Date()
            DispatchQueue.main.async {
                print("TCP Has Bytes Available")
                var readByte :UInt8 = 0
                //var message:String=""
                var jsonData=Data()
                while self.inputStream.hasBytesAvailable {
                    self.inputStream.read(&readByte, maxLength: 1)
                    jsonData.append(contentsOf: [readByte])
                    //let s = String(cString: &readByte)
                    //print(s)
                    
                    /*if readByte == 10{
                     let s = String(data: jsonData, encoding: .utf8)
                     print(s)
                     //let jsonData = message.data(using: .utf8)!
                     let response  = try! jsonDecoder.decode(TCPResponse.self, from: jsonData)
                     handleResponse(response)
                     }
                     else{
                     jsonData.append(contentsOf: [readByte])
                     }*/
                }
                if jsonData.count>1{
                    
                    var  arr = jsonData.split(separator: 10)
                    for a in arr {
                        let s = String(data: a, encoding: .utf8)
                        print(s)
                        //let jsonData = message.data(using: .utf8)!
                        let response  = try! self.jsonDecoder.decode(TCPResponse.self, from: a)
                        self.handleResponse(response)
                    }
                }
            }
            break
        case Stream.Event.errorOccurred:
            print("TCP Error Occured")
            inputStream.close()
            outputStream.close()
            aStream.close()
            viewController?.disconnect()
            break
        default:
            print("TCP Default")
            break
        }
        
    }
    
    func handleResponse(_ response:TCPResponse){
        if(response != nil&&response.event != nil) {
            if (response.event=="login"){
                viewController?.loginResponse(response);
            }
            else if (response.event=="onNewDriverLocation"&&self.isLogin){
                viewController?.onNewDriverLocation(response);
            }
            else if (response.event=="onOffDriverLocation"){
                viewController?.onOffDriverLocation(response);
            }
            else if (response.event=="onDriverConfirmTrip"){
                viewController?.onDriverConfirmTrip(response);
            }
            else if (response.event=="onDriverCancelTrip"){
                viewController?.onDriverCancelTrip(response);
            }
            else if (response.event=="onDriverStartTrip"){
                viewController?.onDriverStartTrip(response);
            }
            else if (response.event=="onDriverFinishTrip"){
                viewController?.onDriverFinishTrip(response);
            }
            else if (response.event=="onNewClientLocation"){
                viewController?.onNewClientLocation(response);
            }
            else if (response.event=="onOffClientLocation"){
                 viewController?.onOffClientLocation(response);
            }
            else if (response.event=="onClientCancelTrip"){
                viewController?.onClientCancelTrip(response);
            }
            else if (response.event=="onDriverNewTrip"){
                viewController?.onDriverNewTrip(response);
            }
            else if (response.event=="tripMessage"){
                viewController?.tripMessage(response);
            }
                
            else if response.event=="selectedDriver" {
                print("func ack selectedDriver");
                do {
                    if let driverId=response.trip.driverId ,let viewController=viewController, let driverMarker=viewController.drivers[driverId]{
                        tripSingleton.setResponseTrip(trip: response.trip)
                        if tripSingleton.state == Constants.driverHasTrip{
                            viewController.resetVisibility(removeMarker: false)
                            viewController.mapView.removeAnnotation(driverMarker)
                            viewController.drivers.removeValue(forKey: driverId);
                            viewController.addMessage(message: NSLocalizedString("The Driver has another trip",comment:""),senderId: response.trip.driverId,msgId: response.msgId)
                        }
                        else{
                            tripSingleton.saveTrip(managedContext: self.persistentContainer.viewContext)
                            viewController.addMessage(message: NSLocalizedString("Waiting for driver acceptance",comment:""), senderId: response.trip.clientId,msgId: response.msgId)
                            viewController.loadingView.isHidden=true
                        }
                    }
                }
                catch {
                    print(error.localizedDescription)
                }
            }
            else if response.event=="userInfo" {
                print("func ack userInfo");
                do {
                    
                    
                    if let id=response.user._id, let viewController=viewController{
                        if self.user.isClient()||self.user.isAdmin(){
                            viewController.driversInfo[id]=response.user
                        }
                        else{
                            viewController.clientInfoArray=response.user
                            if viewController.clientLocationMarker != nil{
                                viewController.mapView.removeAnnotation(viewController.clientLocationMarker)
                                viewController.mapView.addAnnotation(viewController.clientLocationMarker)
                            }
                        }
                    }
                    
                    
                    
                }
                catch {
                    print(error.localizedDescription)
                }
            }
            else if response.event=="finishTrip" {
                print("func ack finishTrip");
                do {
                    
                    tripSingleton.setResponseTrip(trip: response.trip)
                    tripSingleton.saveTrip(managedContext: self.persistentContainer.viewContext)
                    
                    var messageStr=NSLocalizedString("The trip is finished",comment:"");
                    messageStr=messageStr+"\n"+NSLocalizedString("cost",comment:"")+": " + String(format:"%.2f", tripSingleton.cost) + " " + tripSingleton.cur;
                    //                    if (tripSingleton.distance  < tripSingleton.lngKM) {
                    messageStr=messageStr+"\n"+NSLocalizedString("duration",comment:"")+": " + String(format:"%.2f", tripSingleton.duration) + " " + NSLocalizedString("minutes",comment:"")+" * "+String(tripSingleton.prMinute);
                    messageStr=messageStr+"\n"+NSLocalizedString("distance",comment:"") + ": " + String(format:"%.2f", tripSingleton.distance) + " " + NSLocalizedString("km",comment:"")+" * "+String(tripSingleton.prKM);
                    //                    }
                    //                    else{
                    //                        messageStr=messageStr+"\n"+NSLocalizedString("duration",comment:"")+": " + String(format:"%.2f", tripSingleton.duration) + " " + NSLocalizedString("minutes",comment:"")+" * "+String(tripSingleton.prLngMinute);
                    //                        messageStr=messageStr+"\n"+NSLocalizedString("distance",comment:"") + ": " + String(format:"%.2f", tripSingleton.distance) + " " + NSLocalizedString("km",comment:"")+" * "+String(tripSingleton.prLngKM);
                    //                    }
                    
                    if (tripSingleton.cost <= tripSingleton.prMin) {
                        messageStr=messageStr+"\n"+NSLocalizedString("minimum cost",comment:"")+": "+String(tripSingleton.prMin);
                    }
                    
                    if tripSingleton.promoPercentage != nil && tripSingleton.promoPercentage>0 {
                        messageStr = messageStr + "\n" + NSLocalizedString("promo",comment:"") + ": " + String(format:"%.2f", tripSingleton.promoPercentage)+"%";
                        if tripSingleton.maxPromoAmount != nil && tripSingleton.maxPromoAmount>0 {
                            messageStr = messageStr + " " + NSLocalizedString("upto",comment:"") + " " + String(format:"%.2f", tripSingleton.maxPromoAmount)+" " + tripSingleton.cur;
                        }
                    }
                    
                    if let viewController=viewController{
                        viewController.addMessage(message: messageStr, senderId: tripSingleton.driverId,msgId: response.msgId)
                        viewController.performSegue(withIdentifier: "lastTripSegue", sender: self)
                        viewController.resetVisibility(removeMarker: true)
                        viewController.disconnect()
                    }
                    
                    
                }
                catch {
                    print(error.localizedDescription)
                }
            }
            else if response.event=="driverConfirmed" || response.event=="driverCancel" ||
                response.event=="startTrip" || response.event=="clientCancel" {
                print("func ack \(response.event)");
                do {
                    tripSingleton.setResponseTrip(trip: response.trip)
                    tripSingleton.saveTrip(managedContext: self.persistentContainer.viewContext)
                    
                    if response.event=="driverConfirmed" {
                        viewController?.addMessage(message: NSLocalizedString("The driver accepted the trip",comment:""), senderId: tripSingleton.driverId,msgId: response.msgId)
                    }
                    else if response.event=="driverCancel" {
                        viewController?.addMessage(message: NSLocalizedString("The driver cancelled the trip",comment:""), senderId: tripSingleton.driverId,msgId: response.msgId)
                        viewController?.resetVisibility(removeMarker: true)
                    }
                    else if response.event=="clientCancel" {
                        viewController?.addMessage(message: NSLocalizedString("The client cancelled the trip",comment:""), senderId: tripSingleton.clientId,msgId: response.msgId)
                        viewController?.resetVisibility(removeMarker: true)
                    }
                    else if response.event=="startTrip" {
                        viewController?.addMessage(message: NSLocalizedString("The driver arrived to start point",comment:""), senderId: tripSingleton.driverId,msgId: response.msgId)
                    }
                    
                    
                    
                }
                catch {
                    print(error.localizedDescription)
                }
            }
            else if response.event == "disconnect"{
                viewController?.disconnect()
                exit(0)
            }
            
        }
        self.sentToServer()
    }
    
}
