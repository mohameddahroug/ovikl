//
//  ViewController.swift
//  Ovikl
//
//  Created by Mohamed Dahroug on 7/29/18.
//  Copyright © 2018 Ovikl. All rights reserved.
//

import UIKit
import Foundation
import CoreLocation
//import SocketIO
import Alamofire
import CoreData
import UserNotifications
import GoogleMobileAds
import MapKit

class ViewController: UIViewController2/*,MenuViewControllerDelegate*/ {
    
    @IBOutlet weak var mapView: MKMapView!
    @IBOutlet weak var chatView: UIView!
    
    @IBOutlet weak var tripDistanceTextField: UITextField!
    @IBOutlet weak var tripDurationTextField: UITextField!
    @IBOutlet weak var tripCostTextField: UITextField!
    @IBOutlet weak var costStackView: UIStackView!
    @IBOutlet weak var buttonsStackView: UIView!
    
    @IBOutlet weak var activityIndicatorView: UIActivityIndicatorView!
    @IBOutlet weak var okButton: UIButton!
    @IBOutlet weak var arrivedButton: UIButton!
    @IBOutlet weak var finishButton: UIButton!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var chatButton: UIButton!
    @IBOutlet weak var mapButton: UIButton!
    @IBOutlet weak var callButton: UIButton!
    
    @IBOutlet weak var backItem: UIBarButtonItem!
    @IBOutlet weak var messageTextField: UITextField!
    @IBOutlet weak var tableView: UITableView!
    
    
    @IBOutlet weak var loadingView: UIView!
    @IBOutlet weak var loadingLabel: UILabel!
    
    @IBOutlet weak var expectedView: UIView!
    @IBOutlet weak var expectedKmTextField: TextFieldStack!
    @IBOutlet weak var expectedDurationTextField: TextFieldStack!
    
  
    
    @IBOutlet weak var myLocationButton: UIButton!
    @IBOutlet weak var openMapButton: UIButton!
    
    @IBOutlet weak var onlineItem: UIBarButtonItem!
    @IBOutlet weak var switchItem: UISwitch!
    @IBOutlet weak var onlineTextItem: UIBarButtonItem!
    
    
    
 
    
    let tripSingleton=TripSingelton.mInstance;
    //var server:String!;
    //var port:Int!
    let url:String=Constants.url+"/server";
    let session = URLSession(configuration: URLSessionConfiguration.default);
    //var socketOpened=false;
    var version="i1";
    var lang="en";
    //    var manager:SocketManager!;
    //    var socket:SocketIOClient!;
    //var appDelegate:AppDelegate!;
    //var managedContext:NSManagedObjectContext!;
    //var isLogin=false;
  
    //var dateFormatter=DateFormatter();
    //var dateFormatterShort=DateFormatter();
    var clientLocationMarker:PointAnnotation!;
    var drivers=[String:PointAnnotation]();
    public var driversInfo=[String:User]();
    var clients=[String:PointAnnotation]();
    var cameraMoveEnable=false;
    var unreadedMessagesCount = 0;

    
    var pointerCGSize=CGSize(width: 136/2, height: 192/2);
    
    var optionsViewController : OptionsViewController?
    var menuViewController : MenuViewController?
    var clientInfoArray:User!
    //var lastCoordinate: CLLocationCoordinate2D?;
    //let defaultZoom:Float=15
    let regionRadius: CLLocationDistance = 1500
    var ltr=true
    let defaults = UserDefaults.standard
    var maxPromoAmount:Double=0.0;
    var promoPercentage:Double=0.0;
    var promoCode:String!;
    
    let jsonDecoder=JSONDecoder()
    let jsonEncoder = JSONEncoder()
    
    var images=[String:UIImage]()
    var showExpectedTrip=true
    
    var driverInfoView:UIView!
    var driverInfoViewUser:User!
    var beatOnce=true;
    //var timer:Timer!
    //var background=false

    
    
    
    
    override func viewDidLoad() {
        print("func ViewController viewDidLoad");
        #if DEBUG
            Constants.url=Constants.urlDev
            Constants.indexUrl=Constants.indexUrlDev
        #endif
        super.viewDidLoad()
        
        if appDelegate.user._id == nil{
            appDelegate.load()
        }

        
        
        if appDelegate.user._id != nil && appDelegate.user.type == "driver" && appDelegate.user.driverStatus == "pending"{
            self.performSegue(withIdentifier: "RegisterCarSegue2", sender: self)
            self.navigationController!.viewControllers.removeAll();
            return;
        }
        
        
        
        if appDelegate.user._id == nil {
            self.performSegue(withIdentifier: "logout", sender: self)
            self.navigationController!.viewControllers.removeAll();
            
            return;
        }
        jsonDecoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
        
        /*let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self, selector: #selector(appMovedToBackground), name: UIApplication.willResignActiveNotification, object: nil)
        
        notificationCenter.addObserver(self, selector: #selector(appMovedToForeground), name: UIApplication.didBecomeActiveNotification, object: nil)*/
        
        costStackView.isHidden=true
        buttonsStackView.isHidden=true
        
        activityIndicatorView.startAnimating()
        loadingView.isHidden=false
        loadingLabel.text=NSLocalizedString("connecting",comment:"");
        //        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        //        dateFormatter.timeZone = TimeZone(identifier: "GMT");
        //        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
        //        dateFormatterShort.dateFormat = "yyyy-MM-dd";
        
        let textAttributes = [NSAttributedString.Key.foregroundColor:Constants.colorPrimary]
        navigationController?.navigationBar.titleTextAttributes = textAttributes
        
        if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
            self.version = "i"+version;
        }
        if let lang = Locale.current.languageCode {
            self.lang=lang;
        }
        
        
        
        
        //mapView.showsUserLocation = true
        mapView.delegate=self;
        mapView.showsUserLocation=true
        //mapView.setUserTrackingMode(.follow, animated: true)
        myLocationButton.isHidden=false
        //mapView.settings.myLocationButton = true
        
        if let menuViewController = self.storyboard?.instantiateViewController(withIdentifier: "menuViewController") as? MenuViewController{
            menuViewController.providesPresentationContextTransitionStyle = true
            menuViewController.definesPresentationContext = true
            menuViewController.modalPresentationStyle = UIModalPresentationStyle.overCurrentContext
            menuViewController.modalTransitionStyle = UIModalTransitionStyle.crossDissolve
            menuViewController.delegate = self
            menuViewController.lang=lang
            self.menuViewController=menuViewController
        }
        
        if let optionsViewController = self.storyboard?.instantiateViewController(withIdentifier: "optionsViewController") as? OptionsViewController{
            optionsViewController.providesPresentationContextTransitionStyle = true
            optionsViewController.definesPresentationContext = true
            optionsViewController.modalPresentationStyle = UIModalPresentationStyle.overCurrentContext
            optionsViewController.modalTransitionStyle = UIModalTransitionStyle.crossDissolve
            optionsViewController.delegate = self
            optionsViewController.lang=lang
            self.optionsViewController=optionsViewController
        }
        
        if backItem.isEnabled==false{
            backItem.title=""
        }
        
        
        
        if (UIApplication.shared.userInterfaceLayoutDirection == UIUserInterfaceLayoutDirection.rightToLeft) {
            ltr=false
        }
        tableView.delegate = self
        tableView.dataSource = self
        
        //resetVisibility(removeMarker: true)
        
        
        
        costStackView.layer.cornerRadius=8.0
        buttonsStackView.layer.cornerRadius=8.0
        loadingView.layer.cornerRadius=8.0
        chatView.layer.cornerRadius=8.0
        loadingLabel.layer.masksToBounds=true
        loadingLabel.layer.cornerRadius=8.0
        activityIndicatorView.layer.cornerRadius=8.0
        //buttonsStackView.inter
        okButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(okTrip)))
        arrivedButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(arrivedTrip)))
        finishButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(finishTrip)))
        cancelButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(cancelTrip)))
        chatButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(showChat)))
        mapButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(hideChat)))
        callButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(dail)))
        myLocationButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(showMyLocation)))
        openMapButton.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(openMapNav)))
        mapView.addGestureRecognizer(UITapGestureRecognizer(target: self, action:  #selector(mapTapped)))
        expectedView.layer.cornerRadius=10
        
        if appDelegate.user.isClient(){
            onlineItem.isEnabled=false
            onlineItem.title=""
            onlineTextItem.isEnabled=false
            onlineTextItem.title=""
            switchItem.isHidden=true
        }
       
        
    }
    
    
    func getImage(_ urlString:String,scale:Bool){
        do{
            let url = URL(string: Constants.url+urlString)
            //let data = try Data(contentsOf: url!)
            //imageView.image = UIImage(data: data)
            
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Image")
            request.predicate = NSPredicate(format: "id = %@",
                                            argumentArray: [urlString])
            request.fetchLimit=1
            request.returnsObjectsAsFaults = false
            
            let result = try managedContext.fetch(request)
            if result.count==0{
                
                DispatchQueue.global().async { [weak self] in
                    if let data = try? Data(contentsOf: url!) {
                        if let image = UIImage(data: data),let managedContext=self!.managedContext {
                            DispatchQueue.main.async {
                                
                                if scale{
                                    let size: CGSize = CGSize(width:28, height:40)
                                    let rect = CGRect(x:0, y:0, width:size.width, height:size.height)
                                    UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
                                    image.draw(in: rect)
                                    let im = UIGraphicsGetImageFromCurrentImageContext()
                                    UIGraphicsEndImageContext()
                                    self?.images[urlString] = im
                                    let imageEntity = NSEntityDescription.entity(forEntityName: "Image", in: managedContext)!
                                    let imageRecord:NSManagedObject = NSManagedObject(entity: imageEntity, insertInto: managedContext);
                                    imageRecord.setValue(urlString, forKey: "id")
                                    imageRecord.setValue(im?.pngData(), forKey: "image")
                                    imageRecord.setValue(Date(), forKey: "createTime")
                                }
                                else{
                                    self?.images[urlString] = image
                                    let imageEntity = NSEntityDescription.entity(forEntityName: "Image", in: managedContext)!
                                    let imageRecord:NSManagedObject = NSManagedObject(entity: imageEntity, insertInto: managedContext);
                                    imageRecord.setValue(urlString, forKey: "id")
                                    imageRecord.setValue(image.pngData(), forKey: "image")
                                    imageRecord.setValue(Date(), forKey: "createTime")
                                }
                                
                                print(urlString+" set in cache")
                                
                                do {
                                    try self?.managedContext.save()
                                }
                                catch let error as NSError {
                                    print("Could not save. \(error), \(error.userInfo)")
                                }
                            }
                        }
                    }
                }
            }
            else{
                print(urlString+" get from cache")
                for data in result as! [NSManagedObject] {
                    if let i=data.value(forKey: "image") as? Data{
                        self.images[urlString] = UIImage(data: i)
                    }
                }
                
                
            }
        }
        catch{
            
        }
        
    }
    
    
    override func loaded() {
        print("func loaded")
        super.loaded()
        
        if appDelegate.user.isDriver() && appDelegate.user.driverStatus == "blocked"{
            logout()
           
        }
        else if appDelegate.user.isClient() && appDelegate.user.clientStatus == "blocked"{
            logout()
            
        }
        else{
            appDelegate.startTimer()
           
            if CLLocationManager.locationServicesEnabled() {
                switch CLLocationManager.authorizationStatus() {
                    case .notDetermined:
                        print("CLLocationManager notDetermined")
                        if appDelegate.user.isDriver(){
                            appDelegate.locationManager?.requestAlwaysAuthorization()
                        }
                        else{
                            appDelegate.locationManager?.requestWhenInUseAuthorization()
                        }
                    case  .restricted, .denied:
                        print("CLLocationManager restricted")
                        let alert = UIAlertController(title: NSLocalizedString("Location",comment:""), message: NSLocalizedString("Please allow Ovikl to access this device location",comment:""), preferredStyle: UIAlertController.Style.alert)
                        alert.addAction(UIAlertAction(title: NSLocalizedString("OK",comment:""), style: UIAlertAction.Style.default, handler: { action in
                            if let url = URL(string: UIApplication.openSettingsURLString) {
                                // If general location settings are enabled then open location settings for the app
                                UIApplication.shared.open(url, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
                            }
                        }))
                        
                        self.present(alert, animated: true, completion: nil)
                        return;
                    case  .authorizedWhenInUse:
                        print("CLLocationManager authorizedWhenInUse")
                    case .authorizedAlways:
                        print("CLLocationManager authorizedAlways")
                }
            }
            else {
                print("Location services are not enabled")
                let alert = UIAlertController(title: NSLocalizedString("Location",comment:""), message: NSLocalizedString("Please allow Ovikl to access this device location",comment:""), preferredStyle: UIAlertController.Style.alert)
                alert.addAction(UIAlertAction(title: NSLocalizedString("OK",comment:""), style: UIAlertAction.Style.default, handler: { action in
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        // If general location settings are enabled then open location settings for the app
                        UIApplication.shared.open(url, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
                    }
                }))
                self.present(alert, animated: true, completion: nil)
                return;
            }
            
            appDelegate.locationManager?.startUpdatingLocation();
            if appDelegate.user.isDriver(){
                appDelegate.locationManager?.allowsBackgroundLocationUpdates = true
                if #available(iOS 11.0, *) {
                  appDelegate.locationManager?.showsBackgroundLocationIndicator=true
                }
            }
            if appDelegate.user.isClient() || appDelegate.user.isAdmin(){
                for s in Constants.vehicles{
                    getImage(s.image,scale:false)
                    getImage(s.pointer,scale:true)
                    getImage(s.selectedPointer,scale:true)
                }
            }
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Trip")
            request.predicate = NSPredicate(format: "clientId = %@ or driverId= %@", argumentArray: [appDelegate.user._id,appDelegate.user._id])
            request.sortDescriptors = [NSSortDescriptor(key: "updateTime", ascending: false)]
            request.fetchLimit=1
            request.returnsObjectsAsFaults = false
            do {
                let result = try managedContext.fetch(request)
                if result.count>0,let data = result[0] as? NSManagedObject{
                    tripSingleton.setObject(trip:data)
                    let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripLocation")
                    request.predicate = NSPredicate(format: "tripId = %@", tripSingleton._id)
                    request.sortDescriptors = [NSSortDescriptor(key: "time", ascending: false)]
                    request.fetchLimit=1
                    request.returnsObjectsAsFaults = false
                    let result = try managedContext.fetch(request)
                    if result.count>0,let data = result[0] as? NSManagedObject{
                        if let latitude=data.value(forKey:"latitude") as? Double,
                            let longitude=data.value(forKey:"longitude") as? Double,
                            let time=data.value(forKey:"time") as? Date{
                            appDelegate.lastLocation = CLLocation(latitude: latitude,longitude: longitude)
                            appDelegate.lastLocationTime=time
                        }
                        if let distance=data.value(forKey:"distance") as? Double{
                            appDelegate.distance=distance
                        }
                        else{
                            appDelegate.distance=0
                        }
                        if let duration=data.value(forKey:"duration") as? Double{
                            appDelegate.duration=duration
                        }
                        else{
                            appDelegate.duration=0
                        }
                    }
                }
                if tripSingleton.state != nil && (tripSingleton.state==Constants.CANCELED || tripSingleton.state==Constants.FINISHED){
                    tripSingleton.reset()
                }
            }
            catch{
                print(error.localizedDescription)
            }
            
            /*locationManager.requestWhenInUseAuthorization()
             if appDelegate.user.isDriver(){
             locationManager.requestAlwaysAuthorization()
             }
             locationManager.desiredAccuracy = kCLLocationAccuracyBest;*/
            if appDelegate.user.emailVerified==nil || !appDelegate.user.emailVerified {
                showToast(message:NSLocalizedString("Please check your email to activate the account",comment: ""))
            }
        }
    }
    
    
    
    
    override func viewWillAppear(_ animated: Bool) {
        print("func ViewController viewWillAppear");
        if appDelegate.user.isClient(){
            resetVisibility(removeMarker: true)
            activityIndicatorView.startAnimating()
            loadingView.isHidden=false
            loadingLabel.text=NSLocalizedString("connecting",comment:"");
        }
        super.viewWillAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        print("func ViewController viewWillDisappear");
        messageTextField.endEditing(true)
        if appDelegate.user.isClient() || appDelegate.user.isAdmin() || !appDelegate.isLogin{
            disconnect()
        }
        else if appDelegate.isOnline{
            appDelegate.locationManager?.allowsBackgroundLocationUpdates = true
            
            if #available(iOS 11.0, *) {
              appDelegate.locationManager?.showsBackgroundLocationIndicator=true
            }
        }
        super.viewWillDisappear(animated)
        appDelegate.viewController=nil
    }
    
    
    
    override func viewDidDisappear(_ animated: Bool) {
        print("func ViewController viewDidDisappear");
        super.viewDidDisappear(animated)
        appDelegate.isViewControllerforeground=false
        //appMovedToBackground()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        print("func ViewController viewDidAppear");
        super.viewDidAppear(animated)
        appDelegate.isViewControllerforeground=true
        appDelegate.viewController=self
        //appMovedToForeground()
        let b = UserDefaults.standard.bool(forKey: "online")
        if UserDefaults.standard.value(forKey: "online")==nil{
            UserDefaults.standard.set(switchItem.isOn, forKey: "online")
        }
        else{
            switchItem.isOn=b
            super.loaded()
            if appDelegate.user.isDriver()||appDelegate.user.isAdmin(){
                onlineItem.isEnabled=true
                switchItem.isHidden=false
                if switchItem.isOn{
                    onlineTextItem.title=NSLocalizedString("Online",comment: "")
                }
                else{
                    onlineTextItem.title=NSLocalizedString("Offline", comment: "")
                }
            }
        }
        if switchItem.isOn{
            refreshUser()
        }
        else{
            loadingLabel.text=NSLocalizedString("You are offline now",comment: "")
            activityIndicatorView.stopAnimating()
        }
    }
    
    /*@objc func appMovedToForeground() {
        print("func ViewController appMovedToForeground")
        appDelegate.isViewControllerforeground=true
        if appDelegate.user.isClient(){
            resetVisibility(removeMarker: true)
            activityIndicatorView.startAnimating()
            loadingView.isHidden=false
            loadingLabel.text=NSLocalizedString("connecting",comment:"");
        }
        /*if server != nil && !isLogin{
         resetVisibility(removeMarker: true)
         activityIndicatorView.startAnimating()
         loadingView.isHidden=false
         loadingLabel.text=NSLocalizedString("connecting",comment:"");
         connectTCP()
         }*/
        
    }
    
    @objc func appMovedToBackground() {
        print("func ViewController appMovedToBackground")
        appDelegate.isViewControllerforeground=false
        messageTextField.endEditing(true)
        if appDelegate.user.isClient() || appDelegate.user.isAdmin() || !appDelegate.isLogin{
            disconnect()
        }
        else if appDelegate.isOnline{
            locationManager.allowsBackgroundLocationUpdates = true
            if #available(iOS 11.0, *) {
              locationManager.showsBackgroundLocationIndicator=true
            }
        }
    }*/
    
    func backgroundCall(){
        
    }
    
    
    @IBAction func options(_ sender: Any) {
        print("func options");
        if let optionsViewController=optionsViewController{
            self.present(optionsViewController, animated: true, completion: nil)
            optionsViewController.cancelItem.isHidden=cancelButton.isHidden
            optionsViewController.chatItem.isHidden=chatButton.isHidden
            optionsViewController.callItem.isHidden=callButton.isHidden
            optionsViewController.okItem.isHidden=okButton.isHidden
            optionsViewController.arrivedItem.isHidden=arrivedButton.isHidden
            optionsViewController.finishItem.isHidden=finishButton.isHidden
            optionsViewController.mapItem.isHidden=mapButton.isHidden
            
            if appDelegate.user.isClient() && tripSingleton._id==nil{
                if cameraMoveEnable {
                    optionsViewController.changeLocationItem.isHidden=true
                    optionsViewController.confirmLocationItem.isHidden=false
                }
                else{
                    optionsViewController.changeLocationItem.isHidden=false
                    optionsViewController.confirmLocationItem.isHidden=true
                }
            }
            else{
                optionsViewController.changeLocationItem.isHidden=true
                optionsViewController.confirmLocationItem.isHidden=true
            }
            
            if (appDelegate.user.isClient()){
                if tripSingleton._id == nil{
                    optionsViewController.expectedItem.isHidden=false
                }
                else{
                    optionsViewController.expectedItem.isHidden=true
                }
                if unreadedMessagesCount>0{
                    optionsViewController.chatItem.setTitle(NSLocalizedString("chat with driver",comment:"")+"(" + String(unreadedMessagesCount) + ")", for: .normal)
                }
                else{
                    optionsViewController.chatItem.setTitle(NSLocalizedString("chat with driver",comment:""), for: .normal)
                }
            }
            else{
                if unreadedMessagesCount>0{
                    optionsViewController.chatItem.setTitle(NSLocalizedString("chat with client", comment:"")+"(" + String(unreadedMessagesCount) + ")", for: .normal)
                }
                else{
                    optionsViewController.chatItem.setTitle(NSLocalizedString("chat with client", comment:""), for: .normal)
                }
            }
            
        }
        
    }
    
    @IBAction func openMenu(_ sender: Any) {
        print("func openMenu");
        if let menuViewController=menuViewController{
            self.present(menuViewController, animated: true, completion: nil)
            switchItem.isOn=appDelegate.isOnline
            
        }
    }
    
    @IBAction func sendAction(_ sender: UIButton) {
        print("func sendAction");
        if let msg=messageTextField.text{
            if msg.count>0{
                var map=getNewParameters()
                map["event"]="tripMessage"
                map["senderId"]=appDelegate.user._id
                map["tripId"]=tripSingleton._id
                map["state"]=tripSingleton.state
                map["clientId"]=tripSingleton.clientId
                map["driverId"]=tripSingleton.driverId
                map["message"]=msg
                appDelegate.addToQueue(map)
                messageTextField.text=""
            }
        }
    }
    
    @IBAction func expectedViewOk(_ sender: UIButton) {
        if expectedKmTextField.isValid() && expectedDurationTextField.isValid(){
            expectedView.isHidden=true
            expectedDurationTextField.endEditing(true)
        }
    }
    
    @IBAction func expectedViewCancel(_ sender: UIButton) {
        expectedView.isHidden=true
        expectedKmTextField.textField.text=""
        expectedDurationTextField.textField.text=""
        expectedKmTextField.endEditing(true)
        expectedDurationTextField.endEditing(true)
    }
    
   
    
    
    
    
    
    @IBAction func backItemAction(_ sender: UIBarButtonItem) {
        print("func backItemAction");
        hideChat()
    }
    
    
    @IBAction func switchAction(_ sender: UISwitch) {
        if sender.isOn{
            online()
            onlineTextItem.title=NSLocalizedString("Online",comment: "")
            if appDelegate.user.isDriver(){
                appDelegate.locationManager?.allowsBackgroundLocationUpdates = true
                if #available(iOS 11.0, *) {
                  appDelegate.locationManager?.showsBackgroundLocationIndicator=true
                }
            }
        }
        else{
            offline()
            onlineTextItem.title=NSLocalizedString("Offline",comment: "")
            if appDelegate.user.isDriver(){
                appDelegate.locationManager?.allowsBackgroundLocationUpdates = false
                if #available(iOS 11.0, *) {
                  appDelegate.locationManager?.showsBackgroundLocationIndicator=false
                }
            }
        }
    }
    
    
    func resetVisibility(removeMarker:Bool) {
        print("func resetVisibility");
        okButton.isHidden=true;
        cancelButton.isHidden=true;
        chatButton.isHidden=true;
        callButton.isHidden=true;
        mapButton.isHidden=true;
        arrivedButton.isHidden=true;
        finishButton.isHidden=true;
        openMapButton.isHidden=true;
        backItem.isEnabled=false
        backItem.title=""
        
        chatView.isHidden=true
        mapView.isHidden=false
        buttonsStackView.isHidden=true
        //hide keyboard
        chatView.endEditing(true)
        //todo: reset chat
        chatButton.setImage(UIImage(named: "chat_free"), for: .normal)
        /*let subViews = chatScrollView.subviews
         for subview in subViews{
         subview.removeFromSuperview()
         }*/
        unreadedMessagesCount = 0
        appDelegate.lastLocationTime = nil
        appDelegate.lastLocation=nil
        clientInfoArray=nil;
        costStackView.isHidden=true
        tripDurationTextField.text=""
        tripDistanceTextField.text=""
        tripCostTextField.text=""
        cameraMoveEnable=false
        if removeMarker{
            if (drivers.count>0) {
                for m in drivers {
                    mapView.removeAnnotation(m.value)
                }
                drivers.removeAll();
            }
            
            if (clients.count>0) {
                for m in clients {
                    mapView.removeAnnotation(m.value)
                }
                clients.removeAll();
            }
        }
        
            activityIndicatorView.startAnimating()
            loadingView.isHidden=false
            if !appDelegate.isLogin {
                loadingLabel.text=NSLocalizedString("connecting",comment:"");
            }
            else if appDelegate.user.isClient(){
                loadingLabel.text=NSLocalizedString("search_driver",comment:"");
            }
            else if appDelegate.user.isDriver(){
                loadingLabel.text=NSLocalizedString("wait_requests",comment:"");
            }
            else if appDelegate.user.isAdmin(){
                loadingLabel.text=NSLocalizedString("monitor_zone",comment:"");
                activityIndicatorView.stopAnimating()
            }
        
        if (appDelegate.user.isClient() ) {
            if (clientLocationMarker != nil) {
                
                var map = getNewParameters();
                map["event"]="location"
                map["_id"]=appDelegate.user._id
                map["type"]=appDelegate.user.type
                map["latitude"]=String(clientLocationMarker.coordinate.latitude)
                map["longitude"]=String(clientLocationMarker.coordinate.longitude)
                appDelegate.addToQueue(map)
            }
            else {
                appDelegate.locationManager?.startUpdatingLocation();
            }
        }
        
        mapView.showsUserLocation=true
        //mapView.settings.myLocationButton = true
        //mapView.selectedMarker = nil
        
        tripSingleton.reset();
        appDelegate.driverTripLocation=nil;
        appDelegate.distance=0;
        appDelegate.duration=0;
        appDelegate.cost = 0;
        clientInfoArray=nil
        tableView.reloadData()
        
        if appDelegate.user.isDriver()||appDelegate.user.isAdmin(){
            onlineItem.isEnabled=true
            switchItem.isHidden=false
            if switchItem.isOn{
                onlineTextItem.title=NSLocalizedString("Online",comment: "")
            }
            else{
                onlineTextItem.title=NSLocalizedString("Offline", comment: "")
            }
        }
        
    }
    
    
    
    
    func getNewParameters()->[String:Any]{
        var parameters=[String:Any]();
        parameters["type"]=appDelegate.user.type
        parameters["auth_id"]=appDelegate.user._id
        parameters["time"]=Constants.dateFormatter.string(from: Date());
        parameters["msgId"]=UUID().uuidString;
        parameters["lang"] = Locale.current.languageCode
        parameters["ver"] = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
        return parameters
    }
    
    func disconnect(){
        print("func on socket disconnect")
        
        self.activityIndicatorView.startAnimating()
        self.loadingView.isHidden=false
        self.loadingLabel.text=NSLocalizedString("connecting",comment:"");
        self.buttonsStackView.isHidden=true
        appDelegate.socketOpened=false;
        appDelegate.isLogin=false
        appDelegate.stopTimer()
        appDelegate.locationManager?.stopUpdatingLocation();
        appDelegate.disconnectTCP()
    }
    
    /*func connectSocket(){
     print("func connectSocket");
     manager = SocketManager(socketURL: URL(string: server)!, config: [.log(true), .compress])
     socket = manager.defaultSocket
     socket.on(clientEvent: .connect) {data, ack in
     print("func on socket connected")
     self.socketOpened=true;
     self.login(true);
     }
     
     socket.on(clientEvent: .disconnect) {data, ack in
     print("func on socket disconnect")
     self.activityIndicatorView.startAnimating()
     self.loadingView.isHidden=false
     self.loadingLabel.text=NSLocalizedString("connecting",comment:"");
     self.buttonsStackView.isHidden=true
     self.socketOpened=false;
     }
     socket.on(clientEvent: .error) {data, ack in
     print("func on socket error")
     }
     socket.on(clientEvent: .reconnect) {data, ack in
     print("func on socket reconnect")
     }
     
     socket.on(clientEvent: .reconnectAttempt) {data, ack in
     print("func on socket reconnectAttempt")
     self.activityIndicatorView.startAnimating()
     self.loadingView.isHidden=false
     self.loadingLabel.text=NSLocalizedString("connecting",comment:"");
     self.buttonsStackView.isHidden=true
     }
     
     
     
     if(appDelegate.user.isClient()) {
     socket.on("onNewDriverLocation"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onNewDriverLocation(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onOffDriverLocation"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onOffDriverLocation(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onDriverConfirmTrip"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onDriverConfirmTrip(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onDriverCancelTrip"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onDriverCancelTrip(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onDriverStartTrip"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onDriverStartTrip(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onDriverFinishTrip"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onDriverFinishTrip(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     }
     else if(appDelegate.user.isDriver()){
     socket.on("onNewClientLocation"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onNewClientLocation(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onOffClientLocation"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onOffClientLocation(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onClientCancelTrip"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onClientCancelTrip(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     socket.on("onDriverNewTrip"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.onDriverNewTrip(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     }
     socket.on("tripMessage"){data, ack in
     if let jsonString = data[0] as? String{
     do {
     if let dataData = jsonString.data(using: .utf8),
     let jsonArray = try JSONSerialization.jsonObject(with: dataData, options : .allowFragments) as? [String: Any]{
     self.tripMessage(jsonArray);
     }
     } catch {
     print(error.localizedDescription)
     }
     }
     }
     
     socket.connect()
     }*/
    
    func loginResponse(_ response:TCPResponse){
        print("func loginResponse");
        
        if response.retry != nil && response.retry{
            print("loginResponse retry");
            disconnect()
            exit(0)
        }
        else if response.user != nil{
            //appDelegate.user=response.user
            if (response.user.isDriver() && response.user.driverStatus != "active") ||
                (response.user.isClient() && response.user.clientStatus != "active") ||
                (response.user.isAdmin() && response.user.adminStatus != "active"){
                let alert = UIAlertController(title: NSLocalizedString("Inactive account",comment:""), message: NSLocalizedString("Your account is not active, contact with support",comment:""), preferredStyle: UIAlertController.Style.alert)
                alert.addAction(UIAlertAction(title: NSLocalizedString("OK",comment:""), style: UIAlertAction.Style.default, handler: { action in
                    self.performSegue(withIdentifier: "supportSegue", sender: self)
                }))
                
                self.present(alert, animated: true, completion: nil)
                return;
                
            }
        }
        if response.tripArr != nil {
            for trip in response.tripArr.reversed() {
                self.tripSingleton.setResponseTrip(trip: trip);
                self.tripSingleton.saveTrip(managedContext: self.managedContext);
            }
        }
        else{
            //self.resetVisibility(removeMarker: true);
        }
        
        if response.tripMessageArr != nil{
            do{
                for tripMessageA in response.tripMessageArr.reversed() {
                    
                    let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripMessage")
                    request.predicate = NSPredicate(format: "tripId = %@ and state = %@ and senderId = %@ and tripMessageId = %@",
                                                    argumentArray: [tripMessageA.tripId,tripMessageA.state,tripMessageA.senderId,tripMessageA.tripMessageId])
                    request.fetchLimit=1
                    request.returnsObjectsAsFaults = false
                    
                    let result = try self.managedContext.fetch(request)
                    if result.count==0{
                        let tripMessageEntity = NSEntityDescription.entity(forEntityName: "TripMessage", in: self.managedContext)!
                        let tripMessage:NSManagedObject = NSManagedObject(entity: tripMessageEntity, insertInto: self.managedContext);
                        tripMessage.setValue(tripMessageA.tripId, forKey: "tripId")
                        tripMessage.setValue(tripMessageA.state, forKey: "state")
                        tripMessage.setValue(tripMessageA.message, forKey: "message")
                        tripMessage.setValue(tripMessageA.msgId, forKey: "msgId")
                        tripMessage.setValue(tripMessageA.createTime, forKey: "createTime")
                        tripMessage.setValue(tripMessageA.tripMessageId, forKey: "tripMessageId")
                        tripMessage.setValue(tripMessageA.senderId, forKey: "senderId")
                    }
                    
                }
                try self.managedContext.save()
            }
            catch{
                
            }
        }
        if response.driverLocation != nil {
            tripSingleton.driverLat=response.driverLocation.latitude
            tripSingleton.driverLng=response.driverLocation.longitude
        }
        
        if self.tripSingleton.state==Constants.CANCELED || self.tripSingleton.state==Constants.FINISHED {
            self.tripSingleton.reset()
        }
        
        
        appDelegate.isLogin=true;
        self.activityIndicatorView.startAnimating()
        self.loadingView.isHidden=false
        if appDelegate.user.isClient(){
            self.loadingLabel.text=NSLocalizedString("search_driver",comment:"");
        }
        else{
            self.loadingLabel.text=NSLocalizedString("wait_requests",comment:"");
            //locationManager.startUpdatingLocation();
        }
        
        setMarkers()
    
    }
    
    
    
    
    
    
    func showNotification(_ title:String, _ message:String, msgId:String){
        
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = message
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: msgId, content: content, trigger: trigger) // Schedule the notification.
        let center = UNUserNotificationCenter.current()
      
        center.add(request) { (error : Error?) in
            if let theError = error {
                // Handle any errors
                print("Error in notification \(theError)")
            }
        }
    }
    
    func setMarkers() {
        print("func setMarkers");
        if (tripSingleton._id == nil) {
            resetVisibility(removeMarker: true)
            if appDelegate.user.isClient() && showExpectedTrip{
                expectedView.isHidden=false
                showExpectedTrip=false
            }
        }
        else{
            onlineItem.isEnabled=false
            onlineItem.title=""
            onlineTextItem.title=""
            switchItem.isHidden=true
            if (appDelegate.user.isClient()) {
                if (tripSingleton.driverLat != nil) {
                    
                    if let driverMarker=drivers[tripSingleton.driverId]{
                        driverMarker.coordinate = CLLocationCoordinate2D(latitude: tripSingleton.driverLat, longitude: tripSingleton.driverLng)
                    }
                    else{
                        
                        if let driver=driversInfo[tripSingleton.driverId],let type=driver.carType {
                            let driverMarker = PointAnnotation()
                            driverMarker.coordinate = CLLocationCoordinate2D(latitude: tripSingleton.driverLat, longitude: tripSingleton.driverLng)
                            
                            for v in Constants.vehicles{
                                if v.type == type, images[v.selectedPointer] != nil{
                                    driverMarker.icon=images[v.selectedPointer]
                                    driverMarker._id=tripSingleton.driverId
                                    mapView.addAnnotation(driverMarker)
                                    drivers[tripSingleton.driverId]=driverMarker
                                }
                            }
                        }
                        else{
                            var parameters=getNewParameters();
                            parameters["event"]="userInfo";
                            parameters["user_id"]=tripSingleton.driverId;
                            appDelegate.addToQueue(parameters)
                        }
                        
                        
                        
                        
                    }
                    //mapView.camera=GMSCameraPosition.camera(withLatitude: tripSingleton.driverLat, longitude: tripSingleton.driverLng, zoom: defaultZoom)
                    let location = CLLocation(latitude: tripSingleton.driverLat, longitude: tripSingleton.driverLng)
                    let coordinateRegion = MKCoordinateRegion(center: location.coordinate,
                                                              latitudinalMeters: regionRadius, longitudinalMeters: regionRadius)
                    mapView.setRegion(coordinateRegion, animated: true)
                }
                
                if (clientLocationMarker == nil) {
                    clientLocationMarker = PointAnnotation()
                    clientLocationMarker.coordinate = CLLocationCoordinate2D(latitude: tripSingleton.clientLat, longitude: tripSingleton.clientLng)
                    clientLocationMarker.icon=UIImage(named: "client_pointer");
                    clientLocationMarker._id=tripSingleton.clientId
                } else {
                    clientLocationMarker.coordinate = CLLocationCoordinate2D(latitude: tripSingleton.clientLat, longitude: tripSingleton.clientLng)
                }
                
            }
            else if (appDelegate.user.isDriver()) {
                var parameters=getNewParameters();
                parameters["event"]="userInfo";
                parameters["user_id"]=tripSingleton.clientId;
                appDelegate.addToQueue(parameters)
                if let clientMarker=clients[tripSingleton.clientId]{
                    clientMarker.icon=UIImage(named: "client_order_pointer");
                    clientMarker._id=tripSingleton.clientId
                    mapView.removeAnnotation(clientMarker)
                    mapView.addAnnotation(clientMarker)
                    clients[clientMarker._id]=clientMarker
                }
                else {
                    
                    let clientMarker = PointAnnotation()
                    clientMarker.coordinate = CLLocationCoordinate2D(latitude: tripSingleton.clientLat, longitude: tripSingleton.clientLng)
                    clientMarker.icon=UIImage(named: "client_order_pointer");
                    clientMarker._id=tripSingleton.clientId
                    clients[tripSingleton.clientId]=clientMarker
                    mapView.addAnnotation(clientMarker)
                    clients[clientMarker._id]=clientMarker
                }
                
                
            }
            
            if (Constants.PENDING==tripSingleton.state) {
                setPending();
            }
            else if (Constants.RESERVED==tripSingleton.state) {
                setReserved();
            }
            else if (Constants.STARTED==tripSingleton.state) {
                setStarted();
            }
            else if tripSingleton.state==Constants.FINISHED{
                resetVisibility(removeMarker: false)
            }
            else if tripSingleton.state==Constants.CANCELED{
                resetVisibility(removeMarker: false)
            }
        }
    }
    
    
    
    
    
    func onNewDriverLocation(_ response:TCPResponse){
        print( "func onNewDriverLocation");
        
        if driversInfo[response._id]==nil{
            var parameters=getNewParameters();
            parameters["event"]="userInfo";
            parameters["user_id"]=response._id;
            appDelegate.addToQueue(parameters)
        }
        if tripSingleton._id == nil{
            activityIndicatorView.stopAnimating()
            loadingView.isHidden=false
            if appDelegate.user.isClient(){
                loadingLabel.text=NSLocalizedString("select_driver",comment:"");
            }
            
        }
        if tripSingleton._id != nil && tripSingleton.driverId != nil && response._id==tripSingleton.driverId {
            tripSingleton.driverLat=response.latitude;
            tripSingleton.driverLng=response.longitude;
            let tripLocationEntity = NSEntityDescription.entity(forEntityName: "TripLocation", in: self.managedContext)!
            let tripLocation:NSManagedObject = NSManagedObject(entity: tripLocationEntity, insertInto: self.managedContext);
            tripLocation.setValue(tripSingleton._id, forKey: "tripId");
            tripLocation.setValue(tripSingleton.state, forKey: "state");
            tripLocation.setValue(response.latitude, forKey: "latitude");
            tripLocation.setValue(response.longitude, forKey: "longitude");
            tripLocation.setValue(Date(), forKey: "time");
            do {
                try self.managedContext.save()
            } catch let error as NSError {
                print("Could not save. \(error), \(error.userInfo)")
            }
        }
        if tripSingleton._id != nil && tripSingleton.driverId != nil && response._id==tripSingleton.driverId {
            if response.distance != nil {
                tripDistanceTextField.text=String(format:"%.02f",response.distance/1000)+" "+NSLocalizedString("km",comment:"")
            }
            if response.duration != nil {
                tripDurationTextField.text=String(format:"%.02f",response.duration/60000)+" "+NSLocalizedString("minutes",comment:"")
            }
            if response.cost != nil{
                tripCostTextField.text=String(format:"%.02f",response.cost)+" "+tripSingleton.cur
            }
        }
        if let driverMarker=drivers[response._id]{
            if appDelegate.user.isAdmin() {
                  mapView.removeAnnotation(driverMarker)
                  drivers.removeValue(forKey: response._id);
              }
        }
        if let driverMarker=drivers[response._id]{
            driverMarker.coordinate = CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude)
        }
        else{
            if (tripSingleton.driverId != nil && response._id==tripSingleton.driverId) {
                //driverMarker.icon=UIImage(named: "car_pointer_selected");
                if let driver=driversInfo[tripSingleton.driverId],let type=driver.carType{
                    let driverMarker = PointAnnotation()
                    driverMarker.coordinate = CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude)
                    
                    for v in Constants.vehicles{
                        if v.type == type, images[v.selectedPointer] != nil{
                            driverMarker.icon=images[v.selectedPointer]
                            driverMarker._id=tripSingleton.driverId
                            mapView.addAnnotation(driverMarker)
                            drivers[tripSingleton.driverId]=driverMarker
                        }
                    }
                }
            }
            else if (appDelegate.user.isAdmin()&&response.tripId != nil) {
                    //driverMarker.icon=UIImage(named: "car_pointer_selected");
                    if let driver=driversInfo[response._id],let type=driver.carType{
                        let driverMarker = PointAnnotation()
                        driverMarker.coordinate = CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude)
                        
                        for v in Constants.vehicles{
                            if v.type == type, images[v.selectedPointer] != nil{
                                driverMarker.icon=images[v.selectedPointer]
                                driverMarker._id=response._id
                                mapView.addAnnotation(driverMarker)
                                drivers[response._id]=driverMarker
                            }
                        }
                    }
                }
            else{
                //driverMarker.icon=UIImage(named: "car_pointer");
                if let type=response.carType{
                    let driverMarker = PointAnnotation()
                    driverMarker.coordinate = CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude)
                    for v in Constants.vehicles{
                        if v.type == type, images[v.pointer] != nil{
                            driverMarker.icon=images[v.pointer]
                            driverMarker._id=response._id
                            mapView.addAnnotation(driverMarker)
                            drivers[response._id]=driverMarker
                        }
                    }
                }
            }
        }
        
        if tripSingleton.driverId != nil && response._id==tripSingleton.driverId {
            //mapView.camera=GMSCameraPosition.camera(withLatitude: response.latitude, longitude: response.longitude, zoom: mapView.camera.zoom)
            /*let location = CLLocation(latitude: response.latitude, longitude: response.longitude)
            let coordinateRegion = MKCoordinateRegion(center: location.coordinate,
                                                      latitudinalMeters: regionRadius, longitudinalMeters: regionRadius)
            mapView.setRegion(coordinateRegion, animated: true)*/
            mapView.setCenter(CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude), animated: true)
        }
        
        
        
    }
    func onOffDriverLocation(_ response:TCPResponse){
        print( "func onOffDriverLocation");
        
        if let driverId=response._id,
            let driverMarker=drivers[driverId]{
            if  tripSingleton._id == nil {
                mapView.removeAnnotation(driverMarker)
                drivers.removeValue(forKey: driverId);
            }
            if drivers.count==0{
                activityIndicatorView.startAnimating()
                loadingView.isHidden=false
                if appDelegate.user.isClient(){
                    loadingLabel.text=NSLocalizedString("search_driver",comment:"");
                }
            }
        }
        
    }
    func onDriverConfirmTrip(_ response:TCPResponse){
        print( "func onDriverConfirmTrip");
        if let trip=response.trip{
            if  tripSingleton._id != nil && tripSingleton._id == trip._id {
                tripSingleton.setResponseTrip(trip: trip)
                tripSingleton.saveTrip(managedContext: self.managedContext);
                addMessage(message: NSLocalizedString("The driver confirm the trip",comment:""),senderId: trip.driverId,msgId: response.msgId)
                
            }
        }
    }
    
    func onDriverCancelTrip(_ response:TCPResponse){
        print( "func onDriverCancelTrip");
        if let trip=response.trip,
            let driverMarker=drivers[trip.driverId]{
            if  tripSingleton._id != nil && tripSingleton._id == trip._id {
                mapView.removeAnnotation(driverMarker)
                drivers.removeValue(forKey: trip.driverId);
                tripSingleton.setResponseTrip(trip: trip)
                tripSingleton.saveTrip(managedContext: self.managedContext);
                addMessage(message: NSLocalizedString("The driver reject the trip",comment:""),senderId:trip.driverId,msgId: response.msgId)
                resetVisibility(removeMarker: false)
                UIApplication.shared.setMinimumBackgroundFetchInterval(UIApplication.backgroundFetchIntervalNever)
            }
        }
    }
    
    func onDriverStartTrip(_ response:TCPResponse){
        print( "func onDriverStartTrip");
        
        if let trip=response.trip{
            if  tripSingleton._id != nil && tripSingleton._id == trip._id {
                tripSingleton.setResponseTrip(trip: trip)
                tripSingleton.saveTrip(managedContext: self.managedContext);
                addMessage(message: NSLocalizedString("The driver arrived to start point",comment:""),senderId:trip.driverId,msgId: response.msgId)
                setStarted()
            }
        }
        
    }
    func onDriverFinishTrip(_ response:TCPResponse){
        print( "func onDriverFinishTrip");
        
        if let trip=response.trip,
            let driverMarker=drivers[trip.driverId]{
            if  tripSingleton._id != nil && tripSingleton._id == trip._id {
                mapView.removeAnnotation(driverMarker)
                drivers.removeValue(forKey: trip.driverId);
                tripSingleton.setResponseTrip(trip: trip)
                tripSingleton.saveTrip(managedContext: self.managedContext);
                var costDetails = "";
                //                if (tripSingleton.distance  < tripSingleton.lngKM) {
                
                //costDetails = String(tripSingleton.prBase)+" + (" + String(format:"%.2f",(tripSingleton.distance ))+" "+NSLocalizedString("km",comment:"") + " * "+String(tripSingleton.prKM) + ") + (" + String(format:"%.2f",tripSingleton.duration)+" "+NSLocalizedString("minutes",comment:"") + " * "+String(tripSingleton.prMinute)+")";
                //                } else {
                //
                //                    costDetails=String(tripSingleton.prBase) + " + (" + String(format:"%.2f",(tripSingleton.distance ) )+" "+NSLocalizedString("km",comment:"") + " * " + String(tripSingleton.prLngKM) + ") + (" + String(format:"%.2f",tripSingleton.duration)+" "+NSLocalizedString("minutes",comment:"") + " * " + String(tripSingleton.prLngMinute)+")"
                //                }
                
                
                
                var messsage=NSLocalizedString("The trip is finished",comment:"");
                messsage=messsage+"\n"+NSLocalizedString("cost",comment:"") + ": " + String(format:"%.2f", tripSingleton.cost) + " " + tripSingleton.cur;
                messsage=messsage+"\n"+NSLocalizedString("duration",comment:"") + ": " + String(format:"%.2f", tripSingleton.duration) + " " + NSLocalizedString("minutes",comment:"")+" * "+String(format:"%.2f", tripSingleton.prMin);
                messsage=messsage+"\n"+NSLocalizedString("distance",comment:"") + ": " + String(format:"%.2f", tripSingleton.distance) + " " + NSLocalizedString("km",comment:"") + " * "+String(tripSingleton.prKM);
                
                if (tripSingleton.cost <= tripSingleton.prMin) {
                    messsage=messsage+"\n"+NSLocalizedString("minimum cost",comment:"")+": "+String(format:"%.2f", tripSingleton.prMin)
                }
                addMessage(message: messsage,senderId:trip.driverId,msgId: response.msgId)
                lastTrip()
                resetVisibility(removeMarker: false)
                UIApplication.shared.setMinimumBackgroundFetchInterval(UIApplication.backgroundFetchIntervalNever)
            }
        }
        
    }
    func onNewClientLocation(_ response:TCPResponse){
        print( "func onNewClientLocation");
        if appDelegate.user.isAdmin(){
            if let userId=response._id{
                if driversInfo[userId]==nil{
                    var parameters=getNewParameters();
                    parameters["event"]="userInfo";
                    parameters["user_id"]=response._id;
                    appDelegate.addToQueue(parameters)
                }
                if let clientMarker=clients[userId]{
                    clientMarker.coordinate = CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude)
                }
                else{
                    let clientMarker = PointAnnotation()
                    clientMarker.coordinate = CLLocationCoordinate2D(latitude: response.latitude, longitude: response.longitude)
                    //clientLocationMarker.title = "Sydney"
                    //clientLocationMarker.snippet = "Australia"
                    if tripSingleton.clientId != nil && userId==tripSingleton.clientId{
                        clientMarker.icon=UIImage(named: "client_order_pointer");
                    }
                    else{
                        clientMarker.icon=UIImage(named: "client_pointer");
                    }
                    clientMarker._id=userId
                    mapView.addAnnotation(clientMarker)
                    clients[userId]=clientMarker
                }
            }
        }
        
    }
    func onOffClientLocation(_ response:TCPResponse){
        print( "func onOffClientLocation");
        if let clientId=response._id,
            let clientMarker=clients[clientId]{
            if  tripSingleton._id == nil {
                mapView.removeAnnotation(clientMarker)
                clients.removeValue(forKey: clientId);
            }
        }
        
    }
    
    func onClientCancelTrip(_ response:TCPResponse){
        print( "func onClientCancelTrip");
        
        if let trip=response.trip,
            let clientMarker=clients[trip.clientId],
            let msgId=response.msgId{
            if  tripSingleton._id != nil && tripSingleton._id == trip._id {
                mapView.removeAnnotation(clientMarker)
                clients.removeValue(forKey: trip.clientId);
                tripSingleton.setResponseTrip(trip: trip)
                tripSingleton.saveTrip(managedContext: self.managedContext);
                addMessage(message: NSLocalizedString("The client cancelled the trip",comment:""),senderId:trip.clientId,msgId: response.msgId)
                resetVisibility(removeMarker: false)
                tripSingleton.reset()
                /*if UIApplication.shared.applicationState == .background {
                    locationManager.stopUpdatingLocation()
                }*/
            }
        }
    }
    
    func onDriverNewTrip(_ response:TCPResponse){
        print( "func onDriverNewTrip");
        if let trip = response.trip{
            clientInfoArray=trip.client
            if tripSingleton._id != nil && trip._id != tripSingleton._id{
                var map=getNewParameters()
                map["event"]="driverCancel"
                map["_id"]=trip._id
                map["driverId"]=trip.driverId
                map["clientId"]=trip.clientId
                appDelegate.addToQueue(map);
            }
            else {
                
                tripSingleton.setResponseTrip(trip: trip)
                tripSingleton.saveTrip(managedContext: self.managedContext);
                addMessage(message: NSLocalizedString("You have new Trip",comment:""),senderId: trip.clientId,msgId: response.msgId)
                setPending();
                if let clientMarker=clients[trip.clientId]{
                    clientMarker.coordinate = CLLocationCoordinate2D(latitude: trip.clientLat, longitude: trip.clientLng)
                    clientMarker.icon=UIImage(named: "client_order_pointer");
                    clientMarker._id=trip.clientId
                    mapView.removeAnnotation(clientMarker)
                    mapView.addAnnotation(clientMarker)
                    clients[trip.clientId]=clientMarker
                }
                else{
                    let clientMarker = PointAnnotation()
                    clientMarker.coordinate = CLLocationCoordinate2D(latitude: trip.clientLat, longitude: trip.clientLng)
                    clientMarker.icon=UIImage(named: "client_order_pointer");
                    clientMarker._id=trip.clientId
                    mapView.addAnnotation(clientMarker)
                    clients[trip.clientId]=clientMarker
                    //mapView.selectedMarker=clientMarker
                }
            }
        }
        
        
    }
    
    func tripMessage(_ response:TCPResponse){
        print( "func tripMessage");
        
        if let msg = response.tripMessage{
            if  tripSingleton._id == msg.tripId {
                let tripMessageEntity = NSEntityDescription.entity(forEntityName: "TripMessage", in: managedContext)!
                let tripMessage:NSManagedObject = NSManagedObject(entity: tripMessageEntity, insertInto: managedContext);
                tripMessage.setValue(msg.message, forKey: "message");
                tripMessage.setValue(response.msgId, forKey: "msgId");
                tripMessage.setValue(Constants.dateFormatter.date(from: msg.createTime), forKey: "createTime");
                tripMessage.setValue(msg.senderId, forKey: "senderId");
                tripMessage.setValue(msg.tripId, forKey: "tripId");
                tripMessage.setValue(msg.state, forKey: "state");
                tripMessage.setValue(msg.tripMessageId, forKey: "tripMessageId");
                
                do {
                    try managedContext.save()
                } catch let error as NSError {
                    print("Could not save. \(error), \(error.userInfo)")
                }
                showMessage(message: msg.message, senderId: msg.senderId,msgId:response.msgId)
            }
        }
        
    }
    
    
    func setPending(){
        print("func setPending");
        cancelButton.isHidden=false
        callButton.isHidden=false
        costStackView.isHidden=true
        buttonsStackView.isHidden=false
        hideChat();
        if appDelegate.user.isDriver() {
            okButton.isHidden=false
        }
        
        if appDelegate.user.isClient() || appDelegate.user.isAdmin(){
            mapView.showsUserLocation=false
            //mapView.settings.myLocationButton = false
        }
        activityIndicatorView.stopAnimating()
        loadingView.isHidden=true;
        onlineItem.isEnabled=false
        onlineTextItem.title=""
        switchItem.isHidden=true
    }
    
    func setReserved(){
        print("func setReserved");
        cancelButton.isHidden=false
        callButton.isHidden=false
        costStackView.isHidden=true
        buttonsStackView.isHidden=false
        
        if(appDelegate.user.isDriver()) {
            okButton.isHidden=true
            arrivedButton.isHidden=false
            finishButton.isHidden=true
        }
        if(mapButton.isHidden){
            hideChat();
        }
        else{
            showChat();
        }
        
        if(appDelegate.user.isClient()) {
            //mapView.showsUserLocation=false;
            //mapView.settings.myLocationButton = false
        }
        activityIndicatorView.stopAnimating()
        loadingView.isHidden=true;
        onlineItem.isEnabled=false
        onlineTextItem.title=""
        switchItem.isHidden=true
    }
    
    func setStarted(){
        print("func setStarted");
        cancelButton.isHidden=false
        callButton.isHidden=false
        costStackView.isHidden=false
        buttonsStackView.isHidden=false
        
        if(appDelegate.user.isDriver()) {
            okButton.isHidden=true
            arrivedButton.isHidden=true
            finishButton.isHidden=false
        }
        if(mapButton.isHidden){
            hideChat();
        }
        else{
            showChat();
        }
        if(appDelegate.user.isClient()) {
            //mapView.showsUserLocation=false;
            //mapView.settings.myLocationButton = false
        }
        activityIndicatorView.stopAnimating()
        loadingView.isHidden=true;
        onlineItem.isEnabled=false
        onlineTextItem.title=""
        switchItem.isHidden=true
    }
    
    func setFinish(){
        print("func setFinish");
        cancelButton.isHidden=true
        callButton.isHidden=true
        costStackView.isHidden=true
        buttonsStackView.isHidden=true
        
        if(appDelegate.user.isDriver()) {
            okButton.isHidden=true
            arrivedButton.isHidden=true
            finishButton.isHidden=true
        }
        
        hideChat();
        chatButton.isHidden=true
        if(appDelegate.user.isClient()) {
            //mapView.showsUserLocation=false;
            //mapView.settings.myLocationButton = false
        }
        activityIndicatorView.stopAnimating()
        loadingView.isHidden=true;
        onlineItem.isEnabled=false
        onlineTextItem.title=""
        switchItem.isHidden=true
    }
    
    
    @objc func showChat(){
        print("func showChat");
        chatView.isHidden=false
        //mapView.isHidden=true
        chatButton.isHidden=true
        mapButton.isHidden=false
        
        chatButton.setImage(UIImage(named: "chat_free"), for: .normal)
        backItem.isEnabled=true
        backItem.title=NSLocalizedString("Map",comment:"")
        unreadedMessagesCount=0;
        tableView.reloadData()
        
    }
    
    @objc func hideChat(){
        print("func hideChat");
        chatView.isHidden=true
        //mapView.isHidden=false
        chatButton.isHidden=false
        mapButton.isHidden=true
        backItem.isEnabled=false
        backItem.title=""
        chatView.endEditing(true)
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(chatMsgEditText.getWindowToken(), 0);
    }
    
    @objc func dail(){
        print("func dail");
        var mobile:String?
        
        if appDelegate.user.isClient(){
            if let driver=driversInfo[tripSingleton.driverId],let tel=driver.mobile  as? String{
                mobile=tel
            }
        }else if let tel=clientInfoArray.mobile as? String{
            mobile=tel
        }
        
        if let tel=mobile {
            /*let alert = UIAlertController(title: "", message: "Call "+tel+"?", preferredStyle: UIAlertControllerStyle.alert)
             alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: { action in*/
            if let url = URL(string: "tel://\(tel)") {
                UIApplication.shared.open(url)
            }
            /*}))
             alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertActionStyle.cancel, handler: nil))
             self.present(alert, animated: true, completion: nil)*/
        }
    }
    
    @objc func showMyLocation(){
        print("func showMyLocation");
        appDelegate.locationManager?.startUpdatingLocation();
        //mapView.setUserTrackingMode(.follow, animated: true)
    }
    
    @objc func openMapNav(){
        print("func openMapNav");
        for c in clients.values{
            if c.showClientInfo {
                let placemark = MKPlacemark(coordinate: c.coordinate, addressDictionary: nil)
                let mapItem = MKMapItem(placemark: placemark)
                mapItem.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey:MKLaunchOptionsDirectionsModeDriving])
            }
        }
    }
    
    @objc func mapTapped(){
        print("func mapTapped");
        if appDelegate.user.isDriver(){
            for c in clients.values{
                if c.showClientInfo{
                    c.showClientInfo = false
                    mapView.removeAnnotation(c)
                    mapView.addAnnotation(c)
                    openMapButton.isHidden=true
                    clients[c._id]=c
                }
            }
        }
    }
    
    func showMessage(message:String,senderId:String,msgId:String){
        print("func showMessage");
        if chatView.isHidden && senderId != appDelegate.user._id{
            let toastLabel = UILabel(frame: CGRect(x: self.view.frame.size.width/2 - 175, y: self.view.frame.size.height-100, width: 350, height: 35))
            showToast(message: message);
            chatButton.setImage(UIImage(named: "chat"), for: .normal)
            unreadedMessagesCount=unreadedMessagesCount+1
            
        }
        tableView.reloadData()
        if UIApplication.shared.applicationState == .background {
            showNotification(NSLocalizedString("Ovikl",comment:""), message, msgId:msgId)
        }
    }
    
    
    
    func addMessage(message:String,senderId:String,msgId:String){
        print("func addMessage");
        if  tripSingleton._id != nil {
            let tripMessageEntity = NSEntityDescription.entity(forEntityName: "TripMessage", in: managedContext)!
            let tripMessage:NSManagedObject = NSManagedObject(entity: tripMessageEntity, insertInto: managedContext);
            tripMessage.setValue(message, forKey: "message");
            tripMessage.setValue(tripSingleton.msgId, forKey: "msgId");
            tripMessage.setValue(Date(), forKey: "createTime");
            tripMessage.setValue(senderId, forKey: "senderId");
            tripMessage.setValue(tripSingleton._id , forKey: "tripId");
            tripMessage.setValue(tripSingleton.state , forKey: "state");
            do {
                try managedContext.save()
            } catch let error as NSError {
                print("Could not save. \(error), \(error.userInfo)")
            }
            showMessage(message: message, senderId: senderId,msgId: msgId)
            
            
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        print("func prepare");
        if (segue.identifier == "lastTripSegue") {
            if let tripId=tripSingleton._id {
                let vc = segue.destination as! LastTripViewController
                vc.tripId = tripId
            }
        }
    }
    
    
    @objc func okTrip(){
        print("func okTrip");
        if appDelegate.user.isDriver() {
            tripSingleton.state = Constants.RESERVED;
            tripSingleton.driverLat = appDelegate.mostRecentLocation.coordinate.latitude;
            tripSingleton.driverLng = appDelegate.mostRecentLocation.coordinate.longitude;
            var map=tripSingleton.getMap()
            map["event"]="driverConfirmed"
            appDelegate.addToQueue(map);
            setReserved();
            let marker = clients[tripSingleton.clientId]
            if (clients.count>0) {
                for m in clients {
                    if m.key != tripSingleton.clientId{
                        mapView.removeAnnotation(m.value)
                    }
                }
                clients.removeAll();
            }
            clients[tripSingleton.clientId]=marker
        }
    }
    @objc func cancelTrip() {
        print("func cancelTrip");
        let alert = UIAlertController(title: NSLocalizedString("Cancel trip",comment:""), message: NSLocalizedString("Are you sure you want to cancel this trip?",comment:""), preferredStyle: UIAlertController.Style.alert)
        alert.addAction(UIAlertAction(title: NSLocalizedString("OK",comment:""), style: UIAlertAction.Style.default, handler: { action in
            self.cancelTripConfirmed()
        }))
        alert.addAction(UIAlertAction(title: NSLocalizedString("Cancel",comment:""), style: UIAlertAction.Style.cancel, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    
    func cancelTripConfirmed(){
        print("func cancelTripConfirmed");
        self.tripSingleton.state = Constants.CANCELED;
        if (appDelegate.user.isDriver()) {
            self.tripSingleton.cancelledBy = "driver";
            
            var map = self.tripSingleton.getMap();
            map["event"]="driverCancel";
            appDelegate.addToQueue(map);
            
            if let marker = self.clients[self.tripSingleton.clientId], let clientId=self.tripSingleton.clientId {
                mapView.removeAnnotation(marker)
                self.clients.removeValue(forKey: clientId)
            }
            //mMap.setInfoWindowAdapter(null);
            
            /*let subViews = self.chatScrollView.subviews
             for subview in subViews{
             subview.removeFromSuperview()
             }*/
        } else {
            self.tripSingleton.cancelledBy = "client";
            
            var map = self.tripSingleton.getMap();
            map["event"]="clientCancel";
            appDelegate.addToQueue(map);
            
            if let marker = self.drivers[self.tripSingleton.driverId] {
                mapView.removeAnnotation(marker);
                self.drivers.removeValue(forKey: self.tripSingleton.driverId);
            }
            //send client location to join zone rooms
            if (self.clientLocationMarker != nil) {
                var map=getNewParameters();
                map["event"]="location";
                map["_id"]=appDelegate.user._id;
                map["type"]=appDelegate.user.type;
                map["latitude"]=String(self.clientLocationMarker.coordinate.latitude);
                map["longitude"]=String(self.clientLocationMarker.coordinate.longitude);
                appDelegate.addToQueue(map);
            }
            
        }
        
    }
    
    @objc func arrivedTrip(){
        print("func arrivedTrip");
        if appDelegate.user.isDriver() {
            self.tripSingleton.state = Constants.STARTED;
            if appDelegate.mostRecentLocation != nil {
                tripSingleton.driverLat = appDelegate.mostRecentLocation.coordinate.latitude;
                tripSingleton.driverLng = appDelegate.mostRecentLocation.coordinate.longitude;
            }
            var map=self.tripSingleton.getMap()
            map["event"]="startTrip"
            appDelegate.addToQueue(map);
            setStarted()
        }
    }
    
    @objc func finishTrip(){
        print("func finishTrip");
        let alert = UIAlertController(title: NSLocalizedString("Finish trip",comment:""), message: NSLocalizedString("Are you sure you want to finish this trip?",comment:""), preferredStyle: UIAlertController.Style.alert)
        alert.addAction(UIAlertAction(title: NSLocalizedString("OK",comment:""), style: UIAlertAction.Style.default, handler: { action in
            self.finishtripConfirmed()
        }))
        alert.addAction(UIAlertAction(title: NSLocalizedString("Cancel",comment:""), style: UIAlertAction.Style.cancel, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    
    func finishtripConfirmed(){
        print("func finishtripConfirmed");
        if appDelegate.user.isDriver(){
            self.tripSingleton.state = Constants.FINISHED;
            self.tripSingleton.driverLat = appDelegate.mostRecentLocation.coordinate.latitude;
            self.tripSingleton.driverLng = appDelegate.mostRecentLocation.coordinate.longitude;
            self.tripSingleton.distance=appDelegate.distance/1000
            self.tripSingleton.duration=appDelegate.duration/60
            self.tripSingleton.cost=appDelegate.cost
            var map=self.tripSingleton.getMap()
            map["event"]="finishTrip"
            appDelegate.addToQueue(map);
            setFinish()
        }
    }
    
    func showDriverInfo(userInfo:User){
        print("showDriverInfo " + userInfo._id)
        driverInfoViewUser=userInfo
        driverInfoView=UIView(frame: CGRect(x: (self.view.frame.size.width-300)/2, y: (self.view.frame.size.height-450)/2, width: 300, height: 450))
        driverInfoView.layer.cornerRadius=15
        driverInfoView.alpha=1.0
        driverInfoView.backgroundColor=Constants.white
        self.view.addSubview(driverInfoView)
        
        
        let driverInfoScrollView=UIScrollView(frame: CGRect(x: 20, y: 20, width: 260, height: 380))
        driverInfoScrollView.showsVerticalScrollIndicator=false
        driverInfoScrollView.showsHorizontalScrollIndicator=false
        driverInfoView.addSubview(driverInfoScrollView)
        
        let okButton = UIButton(frame: CGRect(x: 240, y: 410, width: 60, height: 25))
        okButton.setTitle(NSLocalizedString("OK",comment:""), for: .normal)
        okButton.setTitleColor( Constants.green, for: .normal)
        driverInfoView.addSubview(okButton)
        if tripSingleton._id ==  nil{
            let gesture1 = UITapGestureRecognizer(target: self, action:  #selector(okButtonTapped))
            okButton.addGestureRecognizer(gesture1)
        }
        else{
            let gesture1 = UITapGestureRecognizer(target: self, action:  #selector(cancelButtonTapped))
            okButton.addGestureRecognizer(gesture1)
        }
        
        
        if tripSingleton._id ==  nil && appDelegate.user.isClient(){
            let cancelButton = UIButton(frame: CGRect(x: 180, y: 410, width: 60, height: 25))
            cancelButton.setTitle(NSLocalizedString("Cancel",comment:""), for: .normal)
            cancelButton.setTitleColor( Constants.red, for: .normal)
            cancelButton.contentHorizontalAlignment = .right
            let gesture2 = UITapGestureRecognizer(target: self, action:  #selector(cancelButtonTapped))
            cancelButton.addGestureRecognizer(gesture2)
            driverInfoView.addSubview(cancelButton)
        }
        
        
        var km:Double!
        var minutes:Double!
        if expectedKmTextField.text() != "", let k=Double(expectedKmTextField.text()){
            km=k
        }
        if expectedDurationTextField.text() != "", let m=Double(expectedDurationTextField.text()){
            minutes=m
        }
        let viewWidth=260
        var height=0
        let view1=UIView(frame: CGRect(x: 0, y: 0, width: viewWidth, height: 400))
        driverInfoScrollView.addSubview(view1)
        
        
        var title=""
        if km != nil && minutes != nil{
            var cost:Double = userInfo.cost.base + km * userInfo.cost.km + minutes * userInfo.cost.minute
            if cost<userInfo.cost.minimum {
                cost = userInfo.cost.minimum
            }
            title = NSLocalizedString("Expected cost is",comment: "")+" "+String(cost)+" "+userInfo.cost.currency
        }
        else {
            
            if let t=userInfo.carManufacturer{
                title = title + " " + t
            }
            if let t=userInfo.carModel{
                title = title + " " + t
            }
            if let t=userInfo.carMadeYear{
                title = title + " " + t
            }
        }
        
        let car = UILabel(frame: CGRect(x: 0, y: 0, width: viewWidth, height: 25))
        view1.addSubview(car)
        car.text=title
        height=height+35
        
        
        let imagesView=UIView()
        
        var width=0
        if userInfo.isDriver(){
            for v in Constants.vehicles{
                if v.type == userInfo.carType{
                    let b=BtnImage(frame: CGRect(x: width, y: 0, width: 100, height: 120))
                    b.setImage(v.image, managedContext!)
                    imagesView.addSubview(b)
                    width=width+105
                }
            }
        
            if userInfo.images.frontImageSmall != nil {
                let b=BtnImage(frame: CGRect(x: width, y: 0, width: 100, height: 120))
                b.setImage(userInfo.images.frontImageSmall, managedContext!)
                imagesView.addSubview(b)
                width=width+105
            }
            if userInfo.images.sideImageSmall != nil {
                let b=BtnImage(frame: CGRect(x: width, y: 0, width: 100, height: 120))
                b.setImage(userInfo.images.sideImageSmall, managedContext!)
                imagesView.addSubview(b)
                width=width+105
            }
            if userInfo.images.backImageSmall != nil {
                let b=BtnImage(frame: CGRect(x: width, y: 0, width: 100, height: 120))
                b.setImage(userInfo.images.backImageSmall, managedContext!)
                imagesView.addSubview(b)
                width=width+105
            }
            
            let imageScrollView = UIScrollView(frame: CGRect(x: 0, y: 35, width: viewWidth, height: 120))
            imageScrollView.contentSize=CGSize(width: width, height: 120)
            imageScrollView.addSubview(imagesView)
            imageScrollView.showsHorizontalScrollIndicator=false
            imageScrollView.showsVerticalScrollIndicator=false
            view1.addSubview(imageScrollView)
            height=height+120
        
        }
        
        
        
        
        
        if userInfo.isDriver(){
            let s1 = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s1.distribution = .equalSpacing
            s1.axis = .horizontal
            let t1 = UILabel()
            t1.text=NSLocalizedString("Driver rate",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v1 = RatingControl()
            v1.starCount=5
            v1.starSize = CGSize(width: 25, height: 25)
            v1.editable=false
            s1.addArrangedSubview(t1)
            s1.addArrangedSubview(v1)
            NSLayoutConstraint.activate([s1.heightAnchor.constraint(equalToConstant: 25),v1.widthAnchor.constraint(equalToConstant: 125)])
            view1.addSubview(s1)
            height=height+35
            if let driverRate = userInfo.driverRate{
                v1.rating=Int(driverRate)
            }
            
            
            
            let s2 = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s2.distribution = .equalSpacing
            s2.axis = .horizontal
            let t2 = UILabel()
            t2.text=NSLocalizedString("Vehicle rate",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v2 = RatingControl()
            v2.starCount=5
            v2.starSize = CGSize(width: 25, height: 25)
            v2.editable=false
            s2.addArrangedSubview(t2)
            s2.addArrangedSubview(v2)
            NSLayoutConstraint.activate([s2.heightAnchor.constraint(equalToConstant: 25),v2.widthAnchor.constraint(equalToConstant: 125)])
            view1.addSubview(s2)
            height=height+35
            if let carRate=userInfo.carRate{
                v2.rating=Int(carRate)
            }
        }
        else if userInfo.isClient(){
            let s1 = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s1.distribution = .equalSpacing
            s1.axis = .horizontal
            let t1 = UILabel()
            t1.text=NSLocalizedString("Client rate",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v1 = RatingControl()
            v1.starCount=5
            v1.starSize = CGSize(width: 25, height: 25)
            v1.editable=false
            s1.addArrangedSubview(t1)
            s1.addArrangedSubview(v1)
            NSLayoutConstraint.activate([s1.heightAnchor.constraint(equalToConstant: 25),v1.widthAnchor.constraint(equalToConstant: 125)])
            view1.addSubview(s1)
            height=height+35
            if let clientRate = userInfo.clientRate{
                v1.rating=Int(clientRate)
            }
        }
        
        if appDelegate.user.isAdmin(){
           if let text=userInfo.firstName{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("First name",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            if let text=userInfo.lastName{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Last name",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            if let text=userInfo.email{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Email",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            if let text=userInfo.mobile{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Mobile",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            
        }
        
        if userInfo.cost != nil{
            if let text=userInfo.cost.minimum{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Minimum cost",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            
            if let text=userInfo.cost.base{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Base cost",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            
            if let text=userInfo.cost.km{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("km cost",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            
            if let text=userInfo.cost.minute{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Minute cost",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=String(text)
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
            
            if let text=userInfo.cost.currency{
                let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
                s.distribution = .equalSpacing
                s.axis = .horizontal
                let t = UILabel()
                t.text=NSLocalizedString("Currency",comment:"")
                //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
                let v = UILabel()
                v.text=text
                s.addArrangedSubview(t)
                s.addArrangedSubview(v)
                NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
                view1.addSubview(s)
                height=height+35
            }
        }
        
        if let text=userInfo.carNumber{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Plate number",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=text
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        if let createDate=userInfo.createDate{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Register from",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=Constants.dateFormatterShort.string(from: createDate)
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        
        if let h=userInfo.totalHours,let d=userInfo.totalDistance{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("total trips",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=String(h)+"/"+String(d)
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        if let text=userInfo.claimsCount{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Claims",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=String(text)
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        
        if let text=userInfo.tripsCount{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Trips count",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=String(text)
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        
        if let text=userInfo.carManufacturer{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Manufacturer",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=text
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        
        if let text=userInfo.carModel{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Model",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=text
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        if let text=userInfo.carMadeYear{
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .equalSpacing
            s.axis = .horizontal
            let t = UILabel()
            t.text=NSLocalizedString("Made year",comment:"")
            //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
            let v = UILabel()
            v.text=String(text)
            s.addArrangedSubview(t)
            s.addArrangedSubview(v)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        }
        
        driverInfoScrollView.contentSize=CGSize(width: viewWidth, height: height)
        //NSLayoutConstraint.activate([stackView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor)])
        
    }
    
    @objc func okButtonTapped() {
           print("func DriverInfoViewDelegate okButtonTapped ")
           if appDelegate.user.isClient(){
               if let user=driverInfoViewUser,tripSingleton._id == nil,let driverMarker=drivers[user._id] {
                   tripSingleton.clientId = appDelegate.user._id;
                   tripSingleton.driverId = user._id;
                   tripSingleton.state = Constants.PENDING;
                   tripSingleton.clientLat = clientLocationMarker.coordinate.latitude;
                   tripSingleton.clientLng = clientLocationMarker.coordinate.longitude;
                   
                   tripSingleton.driverLat = driverMarker.coordinate.latitude;
                   tripSingleton.driverLng = driverMarker.coordinate.longitude;
                   tripSingleton.maxPromoAmount=maxPromoAmount;
                   tripSingleton.promoPercentage=promoPercentage;
                   
                   //driverMarker.icon=UIImage(named: "car_pointer_selected");
                   
                   if let driver=driversInfo[tripSingleton.driverId],let type=driver.carType{
                       
                       for v in Constants.vehicles{
                           if v.type == type, images[v.selectedPointer] != nil{
                               driverMarker.icon=images[v.selectedPointer]
                               mapView.removeAnnotation(driverMarker)
                               mapView.addAnnotation(driverMarker)
                               var parameters=tripSingleton.getMap();
                               parameters["event"]="selectedDriver";
                               parameters["carType"]=type;
                               appDelegate.addToQueue(parameters)
                               setPending();
                               cameraMoveEnable=false
                           }
                       }
                       
                   }
                   if let view = driverInfoView {
                       view.removeFromSuperview()
                       driverInfoView = nil
                   }
                   driverInfoViewUser = nil
                   for i in mapView.selectedAnnotations{
                       mapView.deselectAnnotation(i, animated: false)
                   }
               }
           }
           else{
               if let view = driverInfoView {
                   view.removeFromSuperview()
                   driverInfoView = nil
               }
               driverInfoViewUser = nil
               for i in mapView.selectedAnnotations{
                   mapView.deselectAnnotation(i, animated: false)
               }
           }
           
       }
       
       @objc func cancelButtonTapped() {
           print("func DriverInfoViewDelegate cancelButtonTapped")
           if let view = driverInfoView {
               view.removeFromSuperview()
               driverInfoView = nil
           }
           driverInfoViewUser = nil
           for i in mapView.selectedAnnotations{
               mapView.deselectAnnotation(i, animated: false)
           }
           
       }
       
    
}




extension ViewController: OptionsViewDelegate {
    func changeStartPoint(){
        print("func OptionsViewDelegate changeStartPoint")
        cameraMoveEnable=true
    }
    func confirmStartPoint(){
        print("func OptionsViewDelegate confirmStartPoint")
        cameraMoveEnable=false
    }
    func accept(){
        print("func OptionsViewDelegate accept")
        okTrip()
    }
    func arrived(){
        print("func OptionsViewDelegate arrived")
        arrivedTrip();
    }
    func finish(){
        print("func OptionsViewDelegate finish")
        finishTrip();
    }
    func cancel(){
        print("func OptionsViewDelegate cancel")
        cancelTrip();
    }
    func chat(){
        print("func OptionsViewDelegate chat")
        showChat();
    }
    func call(){
        print("func OptionsViewDelegate call")
        dail()
    }
    func map(){
        print("func OptionsViewDelegate map")
        hideChat();
    }
    func lastTrip(){
        print("func OptionsViewDelegate lastTrip")
        performSegue(withIdentifier: "lastTripSegue", sender: self)
    }
    func support(){
        print("func OptionsViewDelegate support")
        //performSegue(withIdentifier: "supportSegue", sender: self)
        var email="mailto:support@ovikl.com?subject=Ovikl%20support"
        if appDelegate.user.zone != nil && appDelegate.user.zone != "" && appDelegate.user.zoneContact != nil &&
            appDelegate.user.zoneContact.email != nil && appDelegate.user.zoneContact.email != ""{
            email="mailto:"+appDelegate.user.zoneContact.email+"?cc=support@ovikl.com&subject=Ovikl%20support"
        }
        
        
        if let url = URL(string: email){
            if #available(iOS 10.0, *){
                UIApplication.shared.open(url)
            }
            else{
                UIApplication.shared.openURL(url)
            }
        }
    }
    
    func expected() {
        expectedView.isHidden=false
    }
    
    
}


extension ViewController: MenuViewDelegate {
    
    
    func menuLastTrip(){
        print("func MenuViewDelegate menuLastTrip")
        performSegue(withIdentifier: "lastTripSegue", sender: self)
    }
    func menuSupport(){
        print("func MenuViewDelegate menuSupport")
        support()
    }
    func about(){
        print("func MenuViewDelegate about")
        if let url = URL(string: NSLocalizedString("url", comment: "")){
            if #available(iOS 10.0, *) {
                UIApplication.shared.open(url, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
            } else {
                UIApplication.shared.openURL(url)
            }
        }
    }
    func logout(){
        print("func MenuViewDelegate logout")
        disconnect()
        appDelegate.reset()
        self.performSegue(withIdentifier: "logout", sender: self)
        self.navigationController!.viewControllers.removeAll();
        
        //exit(0)
    }
    
    func myInfo(){
        print("func MenuViewDelegate myInfo")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"");
        performSegue(withIdentifier: "MyInfoSegue2", sender: self)
    }
    
    func changePassword(){
        print("func MenuViewDelegate changePassword")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"");
        performSegue(withIdentifier: "changePasswordSegue", sender: self)
    }
    
    func vehicleInfo(){
        print("func MenuViewDelegate vehicleInfo")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"");
        performSegue(withIdentifier: "RegisterCarSegue2", sender: self)
    }
    func prices(){
        print("func MenuViewDelegate prices")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"");
        performSegue(withIdentifier: "PricesSegue2", sender: self)
    }
    func online(){
        print("func MenuViewDelegate online")
        appDelegate.isOnline=true
        appDelegate.locationManager?.startUpdatingLocation();
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"");
        refreshUser()
        UserDefaults.standard.set(true, forKey: "online")
    }
    func offline(){
        print("func MenuViewDelegate offline")
        appDelegate.isOnline=false
        appDelegate.disconnectTCP()
        appDelegate.stopTimer()
        loadingLabel.text=NSLocalizedString("You are offline now",comment:"");
        activityIndicatorView.stopAnimating()
        UserDefaults.standard.set(false, forKey: "online")
    }
    
    func callSupport() {
        if appDelegate.user.zone != nil && appDelegate.user.zone != "" && appDelegate.user.zoneContact != nil &&
           appDelegate.user.zoneContact.mobile != nil && appDelegate.user.zoneContact.mobile != ""{
    
          if let tel=appDelegate.user.zoneContact.mobile {
              /*let alert = UIAlertController(title: "", message: "Call "+tel+"?", preferredStyle: UIAlertControllerStyle.alert)
               alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.default, handler: { action in*/
              if let url = URL(string: "tel://\(tel)") {
                  UIApplication.shared.open(url)
              }
              /*}))
               alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertActionStyle.cancel, handler: nil))
               self.present(alert, animated: true, completion: nil)*/
          }
        }
    }
    
    func zoneUsers(){
        print("func MenuViewDelegate ZoneUsers")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"")
        performSegue(withIdentifier: "ZoneUsersSegue", sender: self)
    }
    func zoneTrips(){
        print("func MenuViewDelegate 'ZoneTrips")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"")
        performSegue(withIdentifier: "ZoneTripsSegue", sender: self)
    }
    func zoneBlockedUsers(){
        print("func MenuViewDelegate ZoneBlockedUsers")
        disconnect()
        activityIndicatorView.startAnimating()
        loadingLabel.text=NSLocalizedString("connecting",comment:"")
        performSegue(withIdentifier: "ZoneBlockedUsersSegue", sender: self)
    }
}



extension ViewController: UITableViewDelegate, UITableViewDataSource  {
    
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        print("func UITableViewDelegate numberOfRowsInSection")
        if tripSingleton._id == nil{
            return 0;
        }
        else{
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripMessage")
            request.predicate = NSPredicate(format: "tripId = %@", tripSingleton._id)
            request.returnsObjectsAsFaults = false
            do {
                let result = try managedContext.fetch(request)
                return result.count
                
            } catch {
                print("Failed")
                return 0;
            }
        }
        
        
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        print("func UITableViewDelegate cellForRowAt")
        if tripSingleton._id != nil{
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripMessage")
            request.predicate = NSPredicate(format: "tripId = %@", tripSingleton._id)
            request.sortDescriptors = [NSSortDescriptor(key: "createTime", ascending: true)]
            request.fetchLimit=1
            request.fetchOffset=indexPath.row
            request.returnsObjectsAsFaults = false
            do {
                let result = try managedContext.fetch(request)
                for data in result as! [NSManagedObject] {
                    if let senderId=data.value(forKey: "senderId") as? String,
                        let message=data.value(forKey: "message") as? String,
                        let time=data.value(forKey: "createTime") as? Date{
                        if senderId==appDelegate.user._id{
                            let cell:ChatTableViewCellSender = tableView.dequeueReusableCell(withIdentifier: "senderCell") as! ChatTableViewCellSender
                            if appDelegate.user.isDriver(){
                                cell.profile.image=UIImage(named: "caoutch");
                            }
                            else{
                                cell.profile.image=UIImage(named: "client");
                            }
                            cell.message?.text = message
                            cell.message?.sizeToFit()
                            cell.message?.isScrollEnabled=false
                            cell.message?.layer.borderWidth=1
                            cell.message?.layer.borderColor=Constants.colorPrimary.cgColor
                            cell.message?.layer.cornerRadius=8
                            cell.time?.text = Constants.dateFormatterTime.string(from: time)
                            if ltr{
                                cell.message?.textAlignment=NSTextAlignment.left
                                cell.time?.textAlignment=NSTextAlignment.left
                            }
                            else{
                                cell.message?.textAlignment=NSTextAlignment.right
                                cell.time?.textAlignment=NSTextAlignment.right
                            }
                            return cell
                        }
                        else{
                            let cell:ChatTableViewCellReceiver = tableView.dequeueReusableCell(withIdentifier: "receiverCell") as! ChatTableViewCellReceiver
                            if appDelegate.user.isDriver(){
                                cell.profile.image=UIImage(named: "client");
                            }
                            else{
                                cell.profile.image=UIImage(named: "caoutch");
                            }
                            cell.message?.text = message
                            cell.message?.sizeToFit()
                            cell.message?.isScrollEnabled=false
                            cell.message?.layer.borderWidth=1
                            cell.message?.layer.borderColor=Constants.colorPrimary.cgColor
                            cell.message?.layer.cornerRadius=8
                            cell.time?.text = Constants.dateFormatterTime.string(from: time)
                            if ltr{
                                cell.message?.textAlignment=NSTextAlignment.right
                                cell.time?.textAlignment=NSTextAlignment.right
                            }
                            else{
                                cell.message?.textAlignment=NSTextAlignment.left
                                cell.time?.textAlignment=NSTextAlignment.left
                            }
                            return cell
                        }
                    }
                    
                }
                
                
            } catch {
                print("Failed")
                
            }
        }
        let cell:ChatTableViewCellSender = tableView.dequeueReusableCell(withIdentifier: "senderCell") as! ChatTableViewCellSender
        cell.message.text="";
        return cell
        
        
    }
    
    /*func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
     return animals.count
     }
     
     func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: IndexPath) -> UITableViewCell {
     let cell:UITableViewCell = tableView.dequeueReusableCellWithIdentifier(cellReuseIdentifier) as UITableViewCell!
     
     cell.textLabel?.text = animals[indexPath.row]
     
     return cell
     }
     
     func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
     print("You tapped cell number \(indexPath.row).")
     }*/
    
}


extension ViewController:MKMapViewDelegate{
    func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
        print("func GMSMapViewDelegate didChange regionDidChangeAnimated" + String(mapView.centerCoordinate.latitude)+" "+String(mapView.centerCoordinate.longitude))
        if appDelegate.user.isClient()&&appDelegate.isLogin&&clientLocationMarker != nil&&tripSingleton._id == nil&&cameraMoveEnable{
            
            //let coordinate = mapView.projection.coordinate(for: mapView.center)
            
            clientLocationMarker.coordinate = mapView.centerCoordinate
        
            var parameters=getNewParameters();
            parameters["event"]="location";
            parameters["_id"]=appDelegate.user._id;
            parameters["type"]=appDelegate.user.type;
            parameters["latitude"]=String(clientLocationMarker.coordinate.latitude);
            parameters["longitude"]=String(clientLocationMarker.coordinate.longitude);
            appDelegate.addToQueue(parameters)
        }
    }
    
    func mapView(_ mapView: MKMapView, didDeselect view: MKAnnotationView) {
        if let annotation = view.annotation as? PointAnnotation{
            print("func GMSMapViewDelegate mapView didDeselect PointAnnotation " + annotation._id)
            /*if appDelegate.user.isClient(){
                cameraMoveEnable=false;
                if let id=annotation._id,
                let userInfo=driversInfo[id]{
                    showDriverInfo(userInfo: userInfo)
                }
            }
            else if annotation==clientLocationMarker && !clientLocationMarker.showClientInfo{
                clientLocationMarker.showClientInfo = false
                mapView.removeAnnotation(clientLocationMarker)
                mapView.addAnnotation(clientLocationMarker)
            }*/
        }
    }
    
    func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView){
        
        if view.annotation is MKUserLocation {
            print("func GMSMapViewDelegate mapView didSelect MKUserLocation")
            view.canShowCallout=false
            //view.setSelected(false, animated: false)
            
            
            return
        }
        else if let annotation = view.annotation as? PointAnnotation{
            print("func GMSMapViewDelegate mapView didSelect PointAnnotation " + annotation._id)
            if appDelegate.user.isClient() || appDelegate.user.isAdmin() {
                cameraMoveEnable=false;
                if let id=annotation._id,
                let userInfo=driversInfo[id]{
                    showDriverInfo(userInfo: userInfo)
                }
            }
            else if annotation.showClientInfo == false {
                annotation.showClientInfo = true
                mapView.removeAnnotation(annotation)
                mapView.addAnnotation(annotation)
                
                clients[annotation._id]=annotation
            }
        }
    }
    
    
    
    func mapView(_ mapView: MKMapView,viewFor annotation: MKAnnotation) -> MKAnnotationView?{
        
        if annotation is MKUserLocation {
            let av = MKAnnotationView(annotation: annotation, reuseIdentifier: "icon")
            av.image=UIImage(named: "current")
            av.frame=CGRect(x: 0, y: 0, width: 10, height: 10)
            av.canShowCallout = false
            av.isEnabled=false
            return av
        }
        else if  let a:PointAnnotation = annotation as? PointAnnotation {
            let av = MKAnnotationView(annotation: annotation, reuseIdentifier: "icon")
            av.image=a.icon
            av.frame=CGRect(x: 0, y: 0, width: 26, height: 38)
            av.centerOffset=CGPoint(x:0,y:-19)
            if appDelegate.user.isDriver() && tripSingleton.clientId != nil && clientInfoArray != nil {
                if let id=a._id{
                    if tripSingleton.clientId==id && clientInfoArray != nil && a.showClientInfo{
                        print("markerInfoWindow "+id)
                        openMapButton.isHidden = false
                        let view = UIView(frame: CGRect.init(x: -100, y: -137, width: 210, height: 135))
                        view.backgroundColor = UIColor.white
                        view.layer.cornerRadius = 6
                        
                        
                        var label1 = UILabel(frame: CGRect.init(x: 5, y: 5, width: 100, height: 25))
                        label1.text = NSLocalizedString("Rate", comment: "")
                        label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label1)
                        let rate = RatingControl(frame: CGRect.init(x: 105, y: 7.5, width: 100, height: 20))
                        rate.starSize=CGSize(width: 20, height: 20)
                        rate.starCount=5
                        
                        if clientInfoArray.clientRate != nil{
                            rate.rating=Int(clientInfoArray.clientRate)
                        }
                        else{
                            rate.rating=0
                        }
                        view.addSubview(rate)
                        
                        
                        label1 = UILabel(frame: CGRect.init(x: 5, y: 30, width: 100, height: 25))
                        label1.text = NSLocalizedString("Total trips", comment: "")
                        label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label1)
                        var label2 = UILabel(frame: CGRect.init(x: 105, y: 30, width: 100, height: 25))
                        if clientInfoArray.totalHours != nil && clientInfoArray.totalDistance != nil {
                            label2.text=String(clientInfoArray.totalHours)+" "+NSLocalizedString("hours", comment: "")+"/"+String(clientInfoArray.totalDistance)+" "+NSLocalizedString("km", comment: "")
                        }
                        else{
                            label2.text = "0 "+NSLocalizedString("hours", comment: "")+"/"+"0 "+NSLocalizedString("km", comment: "")
                        }
                        label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label2)
                        
                        label1 = UILabel(frame: CGRect.init(x: 5, y: 55, width: 100, height: 25))
                        label1.text = NSLocalizedString("Trips count", comment: "")
                        label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label1)
                        label2 = UILabel(frame: CGRect.init(x: 105, y: 55, width: 100, height: 25))
                        if clientInfoArray.tripsCount != nil {
                            label2.text=String(clientInfoArray.tripsCount)
                        }
                        else{
                            label2.text = "0"
                        }
                        label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label2)
                        
                        label1 = UILabel(frame: CGRect.init(x: 5, y: 80, width: 100, height: 25))
                        label1.text = NSLocalizedString("Claims", comment: "")
                        label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label1)
                        label2 = UILabel(frame: CGRect.init(x: 105, y: 80, width: 100, height: 25))
                        if clientInfoArray.claimsCount != nil {
                            label2.text=String(clientInfoArray.claimsCount)
                        }
                        else{
                            label2.text = "0"
                        }
                        label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label2)
                        
                        label1 = UILabel(frame: CGRect.init(x: 5, y: 105, width: 100, height: 25))
                        label1.text = NSLocalizedString("Register from", comment: "")
                        label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label1)
                        label2 = UILabel(frame: CGRect.init(x: 105, y: 105, width: 100, height: 25))
                        if clientInfoArray.createDate != nil {
                            label2.text=Constants.dateFormatterShort.string(from: clientInfoArray.createDate)
                        }
                        else{
                            label2.text = ""
                        }
                        label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                        view.addSubview(label2)
                        av.addSubview(view)
                    }
                }
                return av
            }
            else if appDelegate.user.isClient() || appDelegate.user.isAdmin(){
                let av = MKAnnotationView(annotation: annotation, reuseIdentifier: "icon")
                av.image=a.icon
                av.frame=CGRect(x: 0, y: 0, width: 26, height: 38)
                av.centerOffset=CGPoint(x:0,y:-19)
                av.canShowCallout = false
                if a._id == appDelegate.user._id{
                    av.isEnabled=false
                }
                return av
            }
            
        }
        return nil
    }
    /*func mapView(_ mapView: GMSMapView, markerInfoContents marker: PointAnnotation) -> UIView? {
        print("func GMSMapViewDelegate markerInfoWindow")
        if appDelegate.user.isDriver() && tripSingleton.clientId != nil && clientInfoArray != nil {
            
            if let userData=marker.userData as? [String:String],
                let id=userData["_id"]{
                if tripSingleton.clientId==id && clientInfoArray != nil{
                    print("markerInfoWindow "+id)
                    let view = UIView(frame: CGRect.init(x: 0, y: 0, width: 210, height: 135))
                    view.backgroundColor = UIColor.white
                    view.layer.cornerRadius = 6
                    
                    
                    var label1 = UILabel(frame: CGRect.init(x: 5, y: 5, width: 100, height: 25))
                    label1.text = NSLocalizedString("Rate", comment: "")
                    label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label1)
                    let rate = RatingControl(frame: CGRect.init(x: 105, y: 7.5, width: 100, height: 20))
                    rate.starSize=CGSize(width: 20, height: 20)
                    rate.starCount=5
                    
                    if clientInfoArray.clientRate != nil{
                        rate.rating=Int(clientInfoArray.clientRate)
                    }
                    else{
                        rate.rating=0
                    }
                    view.addSubview(rate)
                    
                    
                    label1 = UILabel(frame: CGRect.init(x: 5, y: 30, width: 100, height: 25))
                    label1.text = NSLocalizedString("Total trips", comment: "")
                    label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label1)
                    var label2 = UILabel(frame: CGRect.init(x: 105, y: 30, width: 100, height: 25))
                    if clientInfoArray.totalHours != nil && clientInfoArray.totalDistance != nil {
                        label2.text=String(clientInfoArray.totalHours)+" "+NSLocalizedString("hours", comment: "")+"/"+String(clientInfoArray.totalDistance)+" "+NSLocalizedString("km", comment: "")
                    }
                    else{
                        label2.text = "0 "+NSLocalizedString("hours", comment: "")+"/"+"0 "+NSLocalizedString("km", comment: "")
                    }
                    label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label2)
                    
                    label1 = UILabel(frame: CGRect.init(x: 5, y: 55, width: 100, height: 25))
                    label1.text = NSLocalizedString("Trips count", comment: "")
                    label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label1)
                    label2 = UILabel(frame: CGRect.init(x: 105, y: 55, width: 100, height: 25))
                    if clientInfoArray.tripsCount != nil {
                        label2.text=String(clientInfoArray.tripsCount)
                    }
                    else{
                        label2.text = "0"
                    }
                    label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label2)
                    
                    label1 = UILabel(frame: CGRect.init(x: 5, y: 80, width: 100, height: 25))
                    label1.text = NSLocalizedString("Claims", comment: "")
                    label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label1)
                    label2 = UILabel(frame: CGRect.init(x: 105, y: 80, width: 100, height: 25))
                    if clientInfoArray.claimsCount != nil {
                        label2.text=String(clientInfoArray.claimsCount)
                    }
                    else{
                        label2.text = "0"
                    }
                    label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label2)
                    
                    
                    
                    
                    label1 = UILabel(frame: CGRect.init(x: 5, y: 105, width: 100, height: 25))
                    label1.text = NSLocalizedString("Register from", comment: "")
                    label1.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label1)
                    label2 = UILabel(frame: CGRect.init(x: 105, y: 105, width: 100, height: 25))
                    if clientInfoArray.createDate != nil {
                        label2.text=Constants.dateFormatterShort.string(from: clientInfoArray.createDate)
                    }
                    else{
                        label2.text = ""
                    }
                    label2.font = UIFont.systemFont(ofSize: 14, weight: .light)
                    view.addSubview(label2)
                    
                    return view
                }
                
                
            }
        }
        return nil
    }*/
    
    
}



// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToUIApplicationOpenExternalURLOptionsKeyDictionary(_ input: [String: Any]) -> [UIApplication.OpenExternalURLOptionsKey: Any] {
    return Dictionary(uniqueKeysWithValues: input.map { key, value in (UIApplication.OpenExternalURLOptionsKey(rawValue: key), value)})
}
