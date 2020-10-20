import UIKit
import Foundation
import Alamofire
import CoreData
import MapKit
class ZoneTripsViewController: UIViewController2, UITableViewDelegate, UITableViewDataSource,MKMapViewDelegate{
    var objectsArray=[TCPResponse.Trip?]()
    
    @IBOutlet var tableView: UITableView!
  
    @IBOutlet var searchTextField: UITextField!
    @IBOutlet var searchButton: UIButton!
    
    @IBOutlet var map: MKMapView!
    @IBOutlet var closeMapButton: UIButton!
    
    let region = MKCoordinateRegion(.world)
    override func viewDidLoad() {
        searchTextField.text=""
        searchTextField.isHidden=true
        searchButton.isHidden=true
        objectsArray.append(nil)
        tableView.delegate = self
        tableView.dataSource = self
        super.viewDidLoad()
        loaded()
        map.delegate=self
    }
    
    @IBAction func search(_ sender: UIButton) {
        searchTextField.endEditing(true)
        objectsArray.removeAll()
        objectsArray.append(nil)
        tableView.reloadData()
    }
    
    @IBAction func searchItem(_ sender: UIBarButtonItem) {
        if searchTextField.isHidden{
            searchTextField.text=""
            searchTextField.isHidden=false
            searchButton.isHidden=false
            
        }
        else{
            searchTextField.isHidden=true
            searchButton.isHidden=true
            searchTextField.endEditing(true)
             searchTextField.text=""
            objectsArray.removeAll()
            objectsArray.append(nil)
            tableView.reloadData()
        }
    }
    
    @IBAction func refreshItem(_ sender: UIBarButtonItem) {
    
        searchTextField.endEditing(true)
        objectsArray.removeAll()
           objectsArray.append(nil)
           tableView.reloadData()
    }
    
    
    @IBAction func closeMap(_ sender: UIButton) {
        map.isHidden=true
        closeMapButton.isHidden=true
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        objectsArray.count
    }
    
    func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer{
          let p = overlay as! Polyline
          let r = MKPolylineRenderer(polyline: p)
          r.strokeColor = p.color
          r.lineWidth=1.5
          return r
          
      }
    
    
    func addLine(_ view1:UIStackView,label:String,value:Double){
        addLine(view1, label: label, value: String(format:"%.2f",value))
    }
    
    func addLine(_ view1:UIStackView,label:String,value:String){
        let s = UIStackView()
        s.distribution = .equalSpacing
        s.axis = .horizontal
        let t = UILabel()
        t.text=label
        //NSLayoutConstraint.activate([t.widthAnchor.constraint(equalToConstant: 100)])
        let v = UILabel()
        v.text=value
        s.addArrangedSubview(t)
        s.addArrangedSubview(v)
        NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
        view1.addArrangedSubview(s)
    }
    
    func addLine(_ view1:UIStackView,label:String){
        
        let t = UILabel()
        t.text=label
        t.textAlignment = .center
        NSLayoutConstraint.activate([t.heightAnchor.constraint(equalToConstant: 25)])
        view1.addArrangedSubview(t)
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        print("tableView cellForRowAt " + String(indexPath.row))
        let cell:UITableViewCell = UITableViewCell()
        if indexPath.row < objectsArray.count , let tripInfo = objectsArray[indexPath.row]{
//
          
            let viewWidth=Int(tableView.frame.width)
            var height=0
            let view1=UIStackView(frame: CGRect(x: 0, y: 0, width: viewWidth, height: 1000))
            view1.axis = .vertical
            view1.distribution = .equalSpacing
            
            for v in cell.subviews {
                v.removeFromSuperview()
            }
            cell.addSubview(view1)

        //            view1.layer.borderWidth = 1
        //            view1.layer.borderColor = Constants.colorPrimary.cgColor
                    NSLayoutConstraint.activate([
                        view1.topAnchor.constraint(equalTo: cell.topAnchor),
                        view1.leftAnchor.constraint(equalTo: cell.leftAnchor),
                        view1.bottomAnchor.constraint(equalTo: cell.bottomAnchor),
                        view1.rightAnchor.constraint(equalTo: cell.rightAnchor)
                        //cell.heightAnchor.constraint(equalToConstant: CGFloat(height))
                    ])
            height=height+35
            let imagesView=UIView()
            var width=0
            
            
            addLine(view1, label: NSLocalizedString("Trip",comment:""))
            
            if let createDate=tripInfo.createTime{
                addLine(view1, label: NSLocalizedString("Time",comment:""), value:Constants.dateFormatterShort.string(from: createDate))
            }
            if let text=tripInfo.state{
                addLine(view1, label: NSLocalizedString("State",comment:""), value:text)
            }
            
            
            if let text=tripInfo.cancelledBy{
                addLine(view1, label: NSLocalizedString("Cancelled By",comment:""), value:text)
            }
            
            if let text=tripInfo.cost{
                addLine(view1, label: NSLocalizedString("Cost",comment:""), value:text)
            }
            
            if let text=tripInfo.cur{
                addLine(view1, label: NSLocalizedString("Currency",comment:""), value:text)
            }
            
            if let text=tripInfo.distance{
                addLine(view1, label: NSLocalizedString("Distance",comment:""), value:text)
            }
            if let text=tripInfo.duration{
                addLine(view1, label: NSLocalizedString("Duration",comment:""), value:text)
            }
            
            if let text=tripInfo.zone{
                addLine(view1, label: NSLocalizedString("Zone code",comment:""), value:text)
            }
            let mapButton = UIButton()
                mapButton.setTitle(NSLocalizedString("Show map",comment:""), for: .normal)
            mapButton.setTitleColor( Constants.primaryTextColor, for: .normal)
            view1.addArrangedSubview(mapButton)
            let gestureMap = ButtonsTapGesture(target: self, action: #selector(self.onclick))
            gestureMap.trip=tripInfo
            mapButton.addGestureRecognizer(gestureMap)
          
            if let userInfo=tripInfo.driver{
              
                addLine(view1, label: NSLocalizedString("Driver",comment:""))
                
                for v in Constants.vehicles{
                    if v.type == userInfo.carType{
                        let b=BtnImage(frame: CGRect(x: width, y: 0, width: 100, height: 120))
                        b.setImage(v.image, managedContext!)
                        imagesView.addSubview(b)
                        width=width+105
                    }
                }
                if userInfo.images != nil {
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
                }
                let imageScrollView = UIScrollView()
                imageScrollView.contentSize=CGSize(width: width, height: 120)
                imageScrollView.addSubview(imagesView)
                imageScrollView.showsHorizontalScrollIndicator=false
                imageScrollView.showsVerticalScrollIndicator=false
                NSLayoutConstraint.activate([imageScrollView.heightAnchor.constraint(equalToConstant: 120)])
                view1.addArrangedSubview(imageScrollView)
            
                if  let text = userInfo.type{
                    addLine(view1, label: NSLocalizedString("Driver type",comment:""), value:text)
                    
                }
                
                if let text=userInfo.driverStatus{
                    addLine(view1, label: NSLocalizedString("Status",comment:""), value:text)
                }
                
               
               
                
                if userInfo.isDriver(){
                    if let driverRate = userInfo.driverRate{
                        let s1 = UIStackView()
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
                        view1.addArrangedSubview(s1)
                       
                        v1.rating=Int(driverRate)
                    }
                    
                    
                    if let carRate=userInfo.carRate{
                        let s2 = UIStackView()
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
                        view1.addArrangedSubview(s2)
                        height=height+35
                        
                        v2.rating=Int(carRate)
                    }
                }
                
                   if let text=userInfo.firstName{
                        addLine(view1, label: NSLocalizedString("First name",comment:""), value:text)
                    }
                    if let text=userInfo.lastName{
                        addLine(view1, label: NSLocalizedString("Last name",comment:""), value:text)
                    }
                    if let text=userInfo.email{
                        addLine(view1, label: NSLocalizedString("Email",comment:""), value:text)
                    }
                    if let text=userInfo.mobile{
                        addLine(view1, label: NSLocalizedString("Mobile",comment:""), value:text)
                    }
                    
                
                
                if userInfo.cost != nil{
                    if let text=userInfo.cost.minimum{
                        addLine(view1, label: NSLocalizedString("Minimum cost",comment:""), value:text)
                    }
                    
                    if let text=userInfo.cost.base{
                        addLine(view1, label: NSLocalizedString("Base cost",comment:""), value:text)
                    }
                    
                    if let text=userInfo.cost.km{
                        addLine(view1, label: NSLocalizedString("km cost",comment:""), value:text)
                    }
                    
                    if let text=userInfo.cost.minute{
                        addLine(view1, label: NSLocalizedString("Minute cost",comment:""), value:text)
                    }
                    
                    if let text=userInfo.cost.currency{
                        addLine(view1, label: NSLocalizedString("Currency",comment:""), value:text)
                    }
                }
                
                if let text=userInfo.carNumber{
                    addLine(view1, label: NSLocalizedString("Plate number",comment:""), value:text)
                }
                
                if let createDate=userInfo.createDate{
                    addLine(view1, label: NSLocalizedString("Register from",comment:""), value:Constants.dateFormatterShort.string(from: createDate))
                }
                
                
                if let h=userInfo.totalHours,let d=userInfo.totalDistance{
                    addLine(view1, label: NSLocalizedString("total trips",comment:""), value:String(h)+"/"+String(d))
                }
                
                if let text=userInfo.claimsCount{
                    addLine(view1, label: NSLocalizedString("Claims",comment:""), value:String(text))
                }
                
                
                if let text=userInfo.tripsCount{
                    addLine(view1, label: NSLocalizedString("Trips count",comment:""), value:String(text))
                }
                
                
                if let text=userInfo.carManufacturer{
                    addLine(view1, label: NSLocalizedString("Manufacturer",comment:""), value:text)
                }
                
                
                if let text=userInfo.carModel{
                    addLine(view1, label: NSLocalizedString("Model",comment:""), value:text)
                }
                
                if let text=userInfo.carMadeYear{
                    addLine(view1, label: NSLocalizedString("Made year",comment:""), value:String(text))
                }
                
                if let text=userInfo.zone,text != ""{
                    addLine(view1, label: NSLocalizedString("Zone code",comment:""), value:text)
                }
            
            }
            
            if let userInfo=tripInfo.client{
                    addLine(view1, label: NSLocalizedString("Client",comment:""))
                
                   if  let text=userInfo.type{
                        addLine(view1, label: NSLocalizedString("Type",comment:""), value:text)
                   }
                 
                   
                   if let text=userInfo.clientStatus{
                        addLine(view1, label: NSLocalizedString("Status",comment:""), value:text)
                   }
                
                  
                   
                   if userInfo.isClient(){
                       if let clientRate = userInfo.clientRate{
                           let s1 = UIStackView()
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
                           view1.addArrangedSubview(s1)
                           height=height+35
                           
                           v1.rating=Int(clientRate)
                       }
                   }
                   
                   
                      if let text=userInfo.firstName{
                        addLine(view1, label: NSLocalizedString("First name",comment:""), value:text)
                      }
                       if let text=userInfo.lastName{
                        addLine(view1, label: NSLocalizedString("Last name",comment:""), value:text)
                       }
                       if let text=userInfo.email{
                        addLine(view1, label: NSLocalizedString("Email",comment:""), value:text)
                       }
                       if let text=userInfo.mobile{
                        addLine(view1, label: NSLocalizedString("Mobile",comment:""), value:text)
                       }
            }
                       
                   
                   
                   
            
           
            
            
        }
        else{
            var parameters: [String: Any]=newParameters();
            parameters["_id"]=appDelegate.user._id
            parameters["hashedKey"]=appDelegate.user.hashedKey
            parameters["type"]=appDelegate.user.type
            parameters["zone"]=appDelegate.user.zone
            parameters["search"]=searchTextField.text
            if objectsArray.count>1 {
                parameters["last_user_id"]=objectsArray[objectsArray.count-2]!._id
            }
            let indicator = UIActivityIndicatorView(frame: CGRect(x: 0, y: 0, width: cell.frame.width, height: 25))
            cell.addSubview(indicator)
            NSLayoutConstraint.activate([cell.heightAnchor.constraint(equalToConstant: CGFloat(25))])
            indicator.startAnimating()
            if self.objectsArray[self.objectsArray.count-1] == nil{
                self.objectsArray.remove(at:self.objectsArray.count-1)
            }
            Alamofire.request(Constants.indexUrl+"/trips", method: .post, parameters: parameters).responseData { response in
                
                if let jsonData = response.data{
                    print("UIViewController2",String(decoding: jsonData, as: UTF8.self))
                    let decoder = JSONDecoder()
                    //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                    
                    do{
                        let json = try decoder.decode(JsonResponse.self, from: jsonData)
                        
                        if(json.code==200){
                            if(json.trips != nil&&json.trips.count>0){
                               
                                
                                self.objectsArray.append(contentsOf: json.trips)
                                self.objectsArray.append(nil)
                            }
                            else{
                                if self.objectsArray.count>0 && self.objectsArray[self.objectsArray.count-1] == nil{
                                    self.objectsArray.remove(at:self.objectsArray.count-1)
                                }
                            }
                            self.tableView.reloadData()
                        }
                        else if json.code == 201{
                            
                        }
                    }
                    catch let error {
                        print(error)
                        
                    }
                }
            }
        }
        return cell
    }
    
    
        @objc func onclick(sender : ButtonsTapGesture) {
        map.isHidden=false
        closeMapButton.isHidden=false
       
        map.setRegion(region, animated: true)
        map.removeOverlays(map.overlays)
        map.removeAnnotations(map.annotations)
        
        if let startLatitude=sender.trip.clientLat,
             let startLongitude=sender.trip.clientLng,
             let finishLatitude=sender.trip.driverLat,
             let finishLongitude=sender.trip.driverLng{
            
            var distance:Double=10;
            if let d = sender.trip.distance{
              distance=d;
            }

            let startMarker = PointAnnotation()
            startMarker.coordinate = CLLocationCoordinate2D(latitude: startLatitude, longitude: startLongitude)
            map.addAnnotation(startMarker)

            let finishMarker = PointAnnotation()
            startMarker.coordinate = CLLocationCoordinate2D(latitude: finishLatitude, longitude: finishLongitude)
            map.addAnnotation(finishMarker)



            let location = CLLocation(latitude: (startLatitude+finishLatitude)/2, longitude: (startLongitude+finishLongitude)/2)
            let coordinateRegion = MKCoordinateRegion(center: location.coordinate,
                                                  latitudinalMeters: distance*1000,
                                                  longitudinalMeters: distance*1000)

            if sender.trip.locations != nil && sender.trip.locations.count>1{
              var path=[CLLocationCoordinate2D]()
              for l in sender.trip.locations {
                  
                  path.append(CLLocationCoordinate2D(latitude: l.latitude, longitude: l.longitude))
                  
                  
              }
              let polylineReserved = Polyline(coordinates: path,count: path.count)
              //polylineReserved.strokeWidth = 1.5
              polylineReserved.color = .gray
              map.addOverlay(polylineReserved)
            }
            map.setRegion(coordinateRegion, animated: true)
         }
    }
    class ButtonsTapGesture: UITapGestureRecognizer {
        //        var admin:User!
        var trip:TCPResponse.Trip!
        
        
        
    }
    
    

}
