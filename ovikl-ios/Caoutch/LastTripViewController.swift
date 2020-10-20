//
//  LastTripViewController.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 8/13/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import UIKit
import Foundation
import CoreData
import Alamofire
import MapKit

class LastTripViewController: UIViewController2 {
    var tripId:String?
    var tripIdFetched:String?
    //let user=appDelegate.user
 
    var tripEnd:NSManagedObject?
    var index:Int=0
    var ltr=true
    let regionRadius: CLLocationDistance = 1000
    @IBOutlet weak var costButton: UIButton!
    
    
    @IBOutlet weak var rateView: UIView!
    @IBOutlet weak var rateLabel: UILabel!
    @IBOutlet weak var rateStackView: RatingControl!
    @IBOutlet weak var carRateLabel: UILabel!
    @IBOutlet weak var carStackView: RatingControl!
    @IBOutlet weak var carView: UIView!
    
    @IBOutlet weak var claimsTextView: UITextView!
    @IBOutlet weak var mapView: MKMapView!
    @IBOutlet weak var previousButton: UIButton!
    @IBOutlet weak var showDetailsButton: UIButton!
    @IBOutlet weak var nextButton: UIButton!
    @IBOutlet weak var tableView: UITableView!
    @IBAction func previousTrip(_ sender: UIButton) {
        save()
        index=index-1
        findTrip(index: index)
        if index==0{
            previousButton.isEnabled=false
        }
    }
    
    @IBAction func showDetails(_ sender: UIButton) {
        if tableView.isHidden{
            tableView.isHidden=false
            mapView.isHidden=true
            showDetailsButton.setTitle(NSLocalizedString("Hide details",comment:""), for: UIControl.State.normal)
            //tableView.reloadData()
        }
        else{
            tableView.isHidden=true
            mapView.isHidden=false
            showDetailsButton.setTitle(NSLocalizedString("Show details",comment:""), for: UIControl.State.normal)
        }
    }
    
    @IBAction func nextTrip(_ sender: UIButton) {
        save()
        index=index+1
        findTrip(index: index)
        previousButton.isEnabled=true
    }
    
    
    override func viewDidLoad() {
        print("LastTripViewController viewDidLoad")
        super.viewDidLoad()
        if appDelegate.user.isClient(){
            rateLabel.text=NSLocalizedString("Driver rate", comment: "")
            carView.isHidden=false
        }
        else{
            carView.isHidden=true
        }
        
        claimsTextView.layer.borderColor = Constants.colorPrimary.cgColor
        claimsTextView.layer.borderWidth = 1
        claimsTextView.layer.cornerRadius = 5
        
        let textAttributes = [NSAttributedString.Key.foregroundColor:Constants.colorPrimary]
        navigationController?.navigationBar.titleTextAttributes = textAttributes
        
        if (UIApplication.shared.userInterfaceLayoutDirection == UIUserInterfaceLayoutDirection.rightToLeft) {
            ltr=false
        }
        
        tableView.delegate = self
        tableView.dataSource = self
        loaded()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        print("LastTripViewController viewDidAppear")
        tableView.isHidden=true
        mapView.isHidden=false
        mapView.delegate=self
        rateStackView.rating=0
        carStackView.rating=0
        costButton.setTitle("", for: .normal)
        tripIdFetched=tripId
        if tripId == nil{
            previousButton.isEnabled=false
        }
        else{
            nextButton.isHidden=true
            previousButton.isHidden=true
            if appDelegate.user.isDriver(){
                do{
                    var tripLocations=[TripLocation2]()
                    let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripLocation")
                    request.predicate = NSPredicate(format: "tripId = %@ and state = %@",argumentArray:[tripId,Constants.RESERVED])
                    request.sortDescriptors = [NSSortDescriptor(key: "time", ascending: true)]
                    //request.fetchLimit=1
                    request.returnsObjectsAsFaults = false
                    let result = try managedContext.fetch(request)
                    if result.count>1{
                        var i:Double=0
                        var step:Double=Double(result.count)/5.0
                        if step<1{
                            step=1
                        }
                        while Int(i)<result.count{
                        //for data in result as! [NSManagedObject] {
                            let data = (result as! [NSManagedObject])[Int(i)]
                            if let latitude=data.value(forKey: "latitude") as? Double,
                                let longitude=data.value(forKey: "longitude") as? Double,
                                let distance=data.value(forKey: "distance") as? Double,
                                let duration=data.value(forKey: "duration") as? Double,
                                let time=data.value(forKey: "time") as? Date,
                                let state=data.value(forKey: "state") as? String {
                                let tripLocation = TripLocation2()
                                tripLocation.i=Int(i)
                                tripLocation.latitude=latitude
                                tripLocation.longitude=longitude
                                tripLocation.state=state
                                tripLocation.distance=distance
                                tripLocation.duration=duration
                                tripLocation.time=Constants.dateFormatterTime.string(from: time)
                                tripLocations.append(tripLocation)
                                i=i+step;
                            }
                            
                        }
                        
                    }
                    
                    let request2 = NSFetchRequest<NSFetchRequestResult>(entityName: "TripLocation")
                    request2.predicate = NSPredicate(format: "tripId = %@ and state = %@",argumentArray:[tripId,Constants.STARTED])
                    request2.sortDescriptors = [NSSortDescriptor(key: "time", ascending: true)]
                    //request.fetchLimit=1
                    request2.returnsObjectsAsFaults = false
                    let result2 = try managedContext.fetch(request2)
                    if result2.count>1{
                        var i:Double=0
                        var step:Double=Double(result2.count)/30.0
                        if step<1{
                            step=1
                        }
                        while Int(i)<result2.count{
                        //for data in result as! [NSManagedObject] {
                            let data = (result2 as! [NSManagedObject])[Int(i)]
                            if let latitude=data.value(forKey: "latitude") as? Double,
                                let longitude=data.value(forKey: "longitude") as? Double,
                                let distance=data.value(forKey: "distance") as? Double,
                                let duration=data.value(forKey: "duration") as? Double,
                                let time=data.value(forKey: "time") as? Date,
                                let state=data.value(forKey: "state") as? String {
                                let tripLocation = TripLocation2()
                                tripLocation.i=Int(i)
                                tripLocation.latitude=latitude
                                tripLocation.longitude=longitude
                                tripLocation.state=state
                                tripLocation.distance=distance
                                tripLocation.duration=duration
                                tripLocation.time=Constants.dateFormatterTime.string(from: time)
                                tripLocations.append(tripLocation)
                                i=i+step;
                            }
                            
                        }
                        
                    }
                  
                    let jsonEncoder = JSONEncoder()
                    let jsonData = try jsonEncoder.encode(tripLocations)
                    let json = String(data: jsonData, encoding: .utf8)
                    var parameters = newParameters()
                    parameters["_id"]=appDelegate.user._id
                    parameters["hashedKey"]=appDelegate.user.hashedKey
                    parameters["trip_id"]=tripId
                    parameters["locations"]=json
                    Alamofire.request(Constants.indexUrl+"/trip_locations", method: .post, parameters: parameters).responseJSON { response in
                        do{
                            if let json = response.result.value as? [String: Any]{
                                NSLog("JSON: \(json)");
                            }
                        }
                    }
                
                    
                }
                catch {
                    print(error.localizedDescription)
                }
            }
            
        }
      
        
        
        let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Trip")
        if let tripId=tripId{
            request.predicate = NSPredicate(format: "tripId = %@ and (state = %@ or state = %@)",argumentArray:[tripId,Constants.FINISHED,Constants.CANCELED])
            request.sortDescriptors = [NSSortDescriptor(key: "updateTime", ascending: false)]
            request.fetchLimit=1
            request.returnsObjectsAsFaults = false
            do {
                let result = try managedContext.fetch(request)
                if result.count != 1{
                    costButton.setTitle("No trips", for: .normal)
                    return
                }
                if let tripEnd = result[0] as? NSManagedObject{
                    setTrip(tripEnd)
                }
                
            }
            catch {
                print(error.localizedDescription)
            }
        }
        else{
            findTrip(index: 0)
        }
        
    }
    
    func findTrip(index:Int){
        let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Trip")
        
        request.predicate = NSPredicate(format: "state = %@ or state = %@",argumentArray:[Constants.FINISHED,Constants.CANCELED])
        request.sortDescriptors = [NSSortDescriptor(key: "updateTime", ascending: false)]
        request.fetchLimit=2
        request.fetchOffset=index
        request.returnsObjectsAsFaults = false
        do {
            let result = try managedContext.fetch(request)
            if result.count==0{
                costButton.setTitle("No trips", for: .normal)
                return
            }
            else if result.count == 1{
                nextButton.isEnabled=false
            }
            else if result.count == 2{
                nextButton.isEnabled=true
            }
            
            if let tripEnd = result[0] as? NSManagedObject{
                setTrip(tripEnd)
            }
            
        }
        catch {
            print(error.localizedDescription)
        }
        
    }
    
    func setTrip(_ tripEnd:NSManagedObject){
    
        self.tripEnd=tripEnd
        claimsTextView.text=""
        carStackView.rating=0
        rateStackView.rating=0
        costButton.setTitle("", for: .normal)
        mapView.removeOverlays(mapView.overlays)
        if let state=tripEnd.value(forKey: "state") as? String,let cancelledBy=tripEnd.value(forKey: "cancelledBy") as? String{
            if state==Constants.CANCELED{
                if cancelledBy=="client"{
                    costButton.setTitle("The client cancel the trip", for: .normal)
                }
                else if cancelledBy=="driver"{
                    costButton.setTitle("The driver cancel the trip", for: .normal)
                }
                rateView.isHidden=true
                carView.isHidden=true
            }
        }
        else{
            rateView.isHidden=false
            if appDelegate.user.isClient(){
                carView.isHidden=false
            }
        }
        
        if let tripId=tripEnd.value(forKey: "tripId") as? String{
            //self.tripId=tripId
            tripIdFetched=tripId
            tableView.reloadData()
        }
        
        
        if let startLatitude=tripEnd.value(forKey: "clientLat") as? Double,
            let startLongitude=tripEnd.value(forKey: "clientLng") as? Double,
            let finishLatitude=tripEnd.value(forKey: "driverLat") as? Double,
            let finishLongitude=tripEnd.value(forKey: "driverLng") as? Double,
            let distance = tripEnd.value(forKey: "distance") as? Double
            {
            let startCoordinare=CLLocationCoordinate2D(latitude: startLatitude < finishLatitude ? startLatitude :finishLatitude, longitude: startLongitude < finishLongitude ? startLongitude : finishLongitude);
            let finishCoordinare=CLLocationCoordinate2D(latitude: startLatitude > finishLatitude ? startLatitude :finishLatitude, longitude: startLongitude > finishLongitude ? startLongitude : finishLongitude);
            /*let bounds=MKCoordinateRegion(coordinate: startCoordinare, coordinate: finishCoordinare);
            mapView.animate(with: GMSCameraUpdate.fit(bounds))*/
            
            let location = CLLocation(latitude: (startLatitude+finishLatitude)/2, longitude: (startLongitude+finishLongitude)/2)
            let coordinateRegion = MKCoordinateRegion(center: location.coordinate,
                                                      latitudinalMeters: distance*1000,
                                                      longitudinalMeters: distance*1000)
            mapView.setRegion(coordinateRegion, animated: true)
            
        }
        if let createTime=tripEnd.value(forKey: "createTime") as? Date{
            if self.tripId==nil{
                self.title="("+String(index+1)+") "+Constants.dateFormatterLocal.string(from: createTime);
            }
            else{
                 self.title=Constants.dateFormatterLocal.string(from: createTime);
            }
        }
        
        if(appDelegate.user.isClient()) {
            if let driverClaim=tripEnd.value(forKey: "driverClaim") as? String,
                let driverRate=tripEnd.value(forKey: "driverRate") as? Int,
                let carRate=tripEnd.value(forKey: "carRate") as? Int{
                claimsTextView.text=driverClaim
                rateStackView.rating=driverRate
                carStackView.rating=carRate
            }
            
        }
        else{
            if let clientClaim=tripEnd.value(forKey: "clientClaim") as? String,
                let clientRate=tripEnd.value(forKey: "clientRate") as? Int{
                claimsTextView.text=clientClaim
                rateStackView.rating=clientRate
            }
        }
        
        
        if let distance=tripEnd.value(forKey: "distance") as? Double,
            let lngKM=tripEnd.value(forKey: "lngKM") as? Double,
            let prMin=tripEnd.value(forKey: "prMin") as? Double,
            let prBase=tripEnd.value(forKey: "prBase") as? Double,
            let prKM=tripEnd.value(forKey: "prKM") as? Double,
            let prLngKM=tripEnd.value(forKey: "prLngKM") as? Double,
            let prLngMinute=tripEnd.value(forKey: "prLngMinute") as? Double,
            let prMinute=tripEnd.value(forKey: "prMinute") as? Double,
            let duration=tripEnd.value(forKey: "duration") as? Double,
            let cur=tripEnd.value(forKey: "cur") as? String,
            let cost=tripEnd.value(forKey: "cost") as? Double,
            let tripId=tripEnd.value(forKey: "tripId") as? String
            
        {
            
            var costDetails:String;
            if (distance  < lngKM) {
                costDetails = String(prBase) + " + (" + String(format:"%.2f",distance)+" "+"km"+" * " + String(prKM) + ") + ("+String(format:"%.2f",duration)+" "+"minutes" + " * " + String(prMinute)+")";
            } else {
                
                costDetails = String(prBase) + " + (" + String(format:"%.2f",distance )+" "+"km" + " * " + String(prLngKM) + ") + (" + String(format:"%.2f",duration)+" "+"minutes" + " * " + String(prLngMinute)+")"
            }
            if (cost <= prMin) {
                
                costDetails = costDetails + "\n" + String(prMin)+" "+"minimun";
            }
            var p="";
            if let paymentMethod=tripEnd.value(forKey: "paymentMethod") as? String {
                if paymentMethod==Constants.cash {
                    p=" " + NSLocalizedString("by_cash",comment:"");
                }
                else if paymentMethod == Constants.visa {
                    p=" " + NSLocalizedString("by_credit_card",comment:"");
                }
            }
            costButton.setTitle(String(format:"%.2f",cost) + " " + cur + p,for: .normal);
            
           
            do{
                let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripLocation")
                request.predicate = NSPredicate(format: "tripId = %@ and state = %@",argumentArray:[tripId,Constants.RESERVED])
                request.sortDescriptors = [NSSortDescriptor(key: "time", ascending: true)]
                //request.fetchLimit=1
                request.returnsObjectsAsFaults = false
                let result = try managedContext.fetch(request)
                if result.count>1{
                    var path=[CLLocationCoordinate2D]()
                    for data in result as! [NSManagedObject] {
                        if let latitude=data.value(forKey: "latitude") as? Double,let longitude=data.value(forKey: "longitude") as? Double{
                            path.append(CLLocationCoordinate2D(latitude: latitude, longitude: longitude))
                        }
                        
                    }
                    let polylineReserved = Polyline(coordinates: path,count: path.count)
                    //polylineReserved.strokeWidth = 1.5
                    polylineReserved.color = .gray
                    mapView.addOverlay(polylineReserved)
                }
                
                let request2 = NSFetchRequest<NSFetchRequestResult>(entityName: "TripLocation")
                request2.predicate = NSPredicate(format: "tripId = %@ and state = %@",argumentArray:[tripId,Constants.STARTED])
                request2.sortDescriptors = [NSSortDescriptor(key: "time", ascending: true)]
                //request.fetchLimit=1
                request2.returnsObjectsAsFaults = false
                let result2 = try managedContext.fetch(request2)
                if result2.count>1{
                    var path=[CLLocationCoordinate2D]()
                    for data in result2 as! [NSManagedObject] {
                        if let latitude=data.value(forKey: "latitude") as? Double,let longitude=data.value(forKey: "longitude") as? Double{
                            path.append(CLLocationCoordinate2D(latitude: latitude, longitude: longitude))
                        }
                    }
                    let polylineStarted = Polyline(coordinates: path,count: path.count)
                    //polylineStarted.strokeWidth = 1.5
                    polylineStarted.color = .orange
                    mapView.addOverlay(polylineStarted)
                }
            }
            catch {
                print(error.localizedDescription)
            }
        }
        
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        save()
        claimsTextView.endEditing(true)
    }
    
    func save(){
        if (claimsTextView.text != "" && claimsTextView.text != nil)||rateStackView.rating>0 || carStackView.rating>0 {
            var parameters = newParameters()
            parameters["_id"]=tripIdFetched
            if let tripEnd=tripEnd{
                if appDelegate.user.isClient(){
                    tripEnd.setValue(claimsTextView.text, forKey: "driverClaim")
                    tripEnd.setValue(rateStackView.rating, forKey: "driverRate")
                    tripEnd.setValue(carStackView.rating, forKey: "carRate")
                    tripEnd.setValue(appDelegate.user._id, forKey: "clientId")
                    parameters["driverClaim"]=claimsTextView.text
                    parameters["driverRate"]=String(rateStackView.rating)
                    parameters["carRate"]=String(carStackView.rating)
                    parameters["clientId"]=appDelegate.user._id
                }
                else{
                    tripEnd.setValue(claimsTextView.text, forKey: "clientClaim")
                    tripEnd.setValue(rateStackView.rating, forKey: "clientRate")
                    tripEnd.setValue(appDelegate.user._id, forKey: "driverId")
                    parameters["clientClaim"]=claimsTextView.text
                    parameters["clientRate"]=String(rateStackView.rating)
                    parameters["driverId"]=appDelegate.user._id
                }
                do {
                    try managedContext.save()
                    print("trip  is saved")
                } catch let error as NSError {
                    print("Could not save. \(error), \(error.userInfo)")
                }
                Alamofire.request(Constants.indexUrl+"/trip", method: .post, parameters: parameters).responseJSON { response in
                    do{
                        if let json = response.result.value as? [String: Any]{
                            NSLog("JSON: \(json)");
                        }
                    }
                }
            }
        }
    }
    
    
    
    
}


extension LastTripViewController: UITableViewDelegate, UITableViewDataSource  {
    
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        print("func UITableViewDelegate numberOfRowsInSection")
        if let tripId = tripIdFetched{
            print("func UITableViewDelegate numberOfRowsInSection trip \(tripId)")
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripMessage")
            request.predicate = NSPredicate(format: "tripId = %@", tripId)
            request.returnsObjectsAsFaults = false
            do {
                let result = try managedContext.fetch(request)
                 print("func UITableViewDelegate numberOfRowsInSection \(result.count)")
                return result.count
                
            } catch {
                print("Failed")
                return 0;
            }
        }
        else{
            return 0;
        }
        
        
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        print("func UITableViewDelegate cellForRowAt")
        if let tripId = tripIdFetched{
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "TripMessage")
            request.predicate = NSPredicate(format: "tripId = %@", tripId)
            request.fetchLimit=1
            request.fetchOffset=indexPath.row
            request.returnsObjectsAsFaults = false
            request.sortDescriptors = [NSSortDescriptor(key: "createTime", ascending: true)]
            do {
                let result = try managedContext.fetch(request)
                for data in result as! [NSManagedObject] {
                    if let senderId=data.value(forKey: "senderId") as? String,
                        let message=data.value(forKey: "message") as? String,
                        let time=data.value(forKey: "createTime") as? Date{
                        return addToChat(message: message, senderId: senderId, time:time)
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
    
    func addToChat(message:String, senderId:String, time:Date)->UITableViewCell{
        if senderId != appDelegate.user._id{
            let cell:ChatTableViewCellReceiver = tableView.dequeueReusableCell(withIdentifier: "receiverCellLast") as! ChatTableViewCellReceiver
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
        else{
            let cell:ChatTableViewCellSender = tableView.dequeueReusableCell(withIdentifier: "senderCellLast") as! ChatTableViewCellSender
            if appDelegate.user.isDriver(){
                cell.profile.image=UIImage(named: "car");
            }
            else{
                cell.profile.image=UIImage(named: "client");
            }
            cell.message?.text = message
            cell.message?.sizeToFit()
            //cell.message?.translatesAutoresizingMaskIntoConstraints = true
            cell.message?.isScrollEnabled=false
            cell.time?.text = Constants.dateFormatterTime.string(from: time)
            
            cell.message?.layer.borderWidth=1
            cell.message?.layer.borderColor=Constants.colorPrimary.cgColor
            cell.message?.layer.cornerRadius=8
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
    }
}

extension LastTripViewController:MKMapViewDelegate{
    func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer{
        let p = overlay as! Polyline
        let r = MKPolylineRenderer(polyline: p)
        r.strokeColor = p.color
        r.lineWidth=1.5
        return r
        
    }
}

class Polyline:MKPolyline{
    var color=Constants.colorPrimary
}


class TripLocation2:Codable{
    
    var i:Int!
    var latitude:Double!
    var longitude:Double!
    var state:String!
    var time:String!
    var distance:Double!
    var duration:Double!
    
}
