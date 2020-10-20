import UIKit
import Foundation
import Alamofire
import CoreData
class ZoneBlockedUsersViewController: UIViewController2, UITableViewDelegate, UITableViewDataSource{
    var objectsArray=[User?]()
    
    @IBOutlet var tableView: UITableView!
  
    @IBOutlet var searchTextField: UITextField!
    @IBOutlet var searchButton: UIButton!
    
    override func viewDidLoad() {
        searchTextField.text=""
        searchTextField.isHidden=true
        searchButton.isHidden=true
        objectsArray.append(nil)
        tableView.delegate = self
        tableView.dataSource = self
        super.viewDidLoad()
        loaded()
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
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        objectsArray.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        print("tableView cellForRowAt " + String(indexPath.row))
        let cell:UITableViewCell = UITableViewCell()
        if indexPath.row < objectsArray.count , let userInfo = objectsArray[indexPath.row]{
            
            
           
//            if tripSingleton._id ==  nil{
//                let gesture1 = UITapGestureRecognizer(target: self, action:  #selector(okButtonTapped))
//                okButton.addGestureRecognizer(gesture1)
//            }
//            else{
//                let gesture1 = UITapGestureRecognizer(target: self, action:  #selector(cancelButtonTapped))
//                okButton.addGestureRecognizer(gesture1)
//            }
            
            
//            if tripSingleton._id ==  nil && appDelegate.user.isClient(){
//                let cancelButton = UIButton(frame: CGRect(x: 180, y: 410, width: 60, height: 25))
//                cancelButton.setTitle(NSLocalizedString("Cancel",comment:""), for: .normal)
//                cancelButton.setTitleColor( Constants.red, for: .normal)
//                cancelButton.contentHorizontalAlignment = .right
//                let gesture2 = UITapGestureRecognizer(target: self, action:  #selector(cancelButtonTapped))
//                cancelButton.addGestureRecognizer(gesture2)
//                driverInfoView.addSubview(cancelButton)
//            }
//
          
            let viewWidth=Int(tableView.frame.width)
            var height=0
            let view1=UIView(frame: CGRect(x: 0, y: 0, width: viewWidth, height: 1000))
            for v in cell.subviews {
                v.removeFromSuperview()
            }
            cell.addSubview(view1)
            height=height+35
            let imagesView=UIView()
            var width=0
            
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
              
            
           
            
            
            let s = UIStackView(frame: CGRect(x: 0, y: height, width: viewWidth, height: 25))
            s.distribution = .fillEqually
            s.axis = .horizontal
            let deleteButton = UIButton()
                deleteButton.setTitle(NSLocalizedString("Remove user block",comment:""), for: .normal)
            deleteButton.setTitleColor( Constants.primaryTextColor, for: .normal)
            if userInfo.zone == appDelegate.user.zone{
                deleteButton.isHidden=false
            }
            else{
                deleteButton.isHidden=true
            }
            
            let confirmButton = UIButton()
            confirmButton.setTitle(NSLocalizedString("Confirm",comment:""), for: .normal)
            confirmButton.setTitleColor( Constants.green, for: .normal)
            confirmButton.isHidden=true
            
            let cancelButton = UIButton()
            cancelButton.setTitle(NSLocalizedString("Cancel",comment:""), for: .normal)
            cancelButton.setTitleColor( Constants.red, for: .normal)
            cancelButton.isHidden=true
            
            let undoButton = UIButton()
            undoButton.setTitle(NSLocalizedString("Undo",comment:""), for: .normal)
            undoButton.setTitleColor( Constants.primaryTextColor, for: .normal)
            if userInfo.getStatus() == "blocked"{
                undoButton.isHidden=false
            }
            else{
                undoButton.isHidden=true
            }

            s.addArrangedSubview(deleteButton)
            s.addArrangedSubview(confirmButton)
            s.addArrangedSubview(cancelButton)
            s.addArrangedSubview(undoButton)
            NSLayoutConstraint.activate([s.heightAnchor.constraint(equalToConstant: 25)])
            view1.addSubview(s)
            height=height+35
        
      
            let gestureDelete = ButtonsTapGesture(target: self, action: #selector(self.onclick))
            gestureDelete.user=userInfo
//            gestureDelete.admin=appDelegate.user
            gestureDelete.deleteButton=deleteButton
            gestureDelete.cancelButton=cancelButton
            gestureDelete.confirmButton=confirmButton
            gestureDelete.undoButton=undoButton
            
            let gestureConfirm = ButtonsTapGesture(target: self, action: #selector(self.onclick))
            gestureConfirm.user=userInfo
//            gestureConfirm.admin=appDelegate.user
            gestureConfirm.deleteButton=deleteButton
            gestureConfirm.cancelButton=cancelButton
            gestureConfirm.confirmButton=confirmButton
            gestureConfirm.undoButton=undoButton
            
            let gestureCancel = ButtonsTapGesture(target: self, action: #selector(self.onclick))
            gestureCancel.user=userInfo
//            gestureCancel.admin=appDelegate.user
            gestureCancel.deleteButton=deleteButton
            gestureCancel.cancelButton=cancelButton
            gestureCancel.confirmButton=confirmButton
            gestureCancel.undoButton=undoButton
            
            let gestureUndo = ButtonsTapGesture(target: self, action: #selector(self.onclick))
            gestureUndo.user=userInfo
//            gestureUndo.admin=appDelegate.user
            gestureUndo.deleteButton=deleteButton
            gestureUndo.cancelButton=cancelButton
            gestureUndo.confirmButton=confirmButton
            gestureUndo.undoButton=undoButton
            
            deleteButton.addGestureRecognizer(gestureDelete)
            cancelButton.addGestureRecognizer(gestureConfirm)
            confirmButton.addGestureRecognizer(gestureCancel)
            undoButton.addGestureRecognizer(gestureUndo)
            
            view1.frame=CGRect(x:0,y:0,width: viewWidth, height: height)
            NSLayoutConstraint.activate([
                view1.topAnchor.constraint(equalTo: cell.topAnchor),
                view1.leftAnchor.constraint(equalTo: cell.leftAnchor),
                view1.bottomAnchor.constraint(equalTo: cell.bottomAnchor),
                view1.rightAnchor.constraint(equalTo: cell.rightAnchor)
                //cell.heightAnchor.constraint(equalToConstant: CGFloat(height))
            ])
            
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
            Alamofire.request(Constants.indexUrl+"/blocked_zone_users", method: .post, parameters: parameters).responseData { response in
                
                if let jsonData = response.data{
                    print("UIViewController2",String(decoding: jsonData, as: UTF8.self))
                    let decoder = JSONDecoder()
                    //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                    
                    do{
                        let json = try decoder.decode(JsonResponse.self, from: jsonData)
                        
                        if(json.code==200){
                            if(json.users != nil&&json.users.count>0){
                               
                                
                                self.objectsArray.append(contentsOf: json.users)
                                self.objectsArray.append(nil)
                            }
                            else{
                                if self.objectsArray.count>0  && self.objectsArray[self.objectsArray.count-1] == nil{
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
        let email = sender.user.email
        if sender.deleteButton == sender.view{
          sender.deleteButton.isHidden=true
          sender.confirmButton.isHidden=false
          sender.cancelButton.isHidden=false
          sender.undoButton.isHidden=true
       }
        else if sender.confirmButton == sender.view{
           var parameters: [String: Any]=newParameters();
            parameters["_id"]=appDelegate.user._id
           parameters["hashedKey"]=appDelegate.user.hashedKey
           parameters["user_email"]=sender.user.email
           parameters["user_id"]=sender.user._id
           parameters["user_type"]=sender.user.type
           Alamofire.request(Constants.indexUrl+"/unblock_zone_user", method: .post, parameters: parameters).responseData { response in
              
              if let jsonData = response.data{
                  print("UIViewController2",String(decoding: jsonData, as: UTF8.self))
                  let decoder = JSONDecoder()
                  //decoder.keyDecodingStrategy = .convertFromSnakeCase
                  decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                  
                  do{
                      let json = try decoder.decode(JsonResponse.self, from: jsonData)
                      
                      if(json.code==200){
                        for i in self.objectsArray {
                            if i?._id == sender.user._id{
                                i?.setStatus(s:"blocked")
                            }
                        }
                      }
                      else if json.code == 201{
                          
                      }
                  }
                  catch let error {
                      print(error)
                      
                  }
              }
           }
           sender.deleteButton.isHidden=true
           sender.confirmButton.isHidden=true
           sender.cancelButton.isHidden=true
           sender.undoButton.isHidden=false
       }
        else if sender.cancelButton == sender.view{
          sender.deleteButton.isHidden=false
          sender.confirmButton.isHidden=true
          sender.cancelButton.isHidden=true
          sender.undoButton.isHidden=true
       }
        else if sender.undoButton == sender.view{
           var parameters: [String: Any]=newParameters();
        parameters["_id"]=appDelegate.user._id
           parameters["hashedKey"]=appDelegate.user.hashedKey
           parameters["user_email"]=sender.user.email
           parameters["user_id"]=sender.user._id
           parameters["user_type"]=sender.user.type
           Alamofire.request(Constants.indexUrl+"/block_zone_user", method: .post, parameters: parameters).responseData { response in
              
              if let jsonData = response.data{
                  print("UIViewController2",String(decoding: jsonData, as: UTF8.self))
                  let decoder = JSONDecoder()
                  //decoder.keyDecodingStrategy = .convertFromSnakeCase
                  decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                  
                  do{
                      let json = try decoder.decode(JsonResponse.self, from: jsonData)
                      
                      if(json.code==200){
                          for i in  self.objectsArray {
                               if i?._id == sender.user._id{
                                   i?.setStatus(s:"active")
                               }
                            }
                      }
                      else if json.code == 201{
                          
                      }
                  }
                  catch let error {
                      print(error)
                      
                  }
              }
           }
          sender.deleteButton.isHidden=false
          sender.confirmButton.isHidden=true
          sender.cancelButton.isHidden=true
          sender.undoButton.isHidden=true
       }
        print("onlick",email)
    }
    
    class ButtonsTapGesture: UITapGestureRecognizer {
//        var admin:User!
        var user:User!
        var deleteButton:UIButton!
        var confirmButton:UIButton!
        var cancelButton:UIButton!
        var undoButton:UIButton!
        
        
        
        
    }
}
