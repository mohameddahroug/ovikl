//
//  CheckLocationViewController.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 26/09/2018.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import UIKit
import Foundation
import Alamofire

class LoginViewController: UIViewController2{
    
    @IBOutlet weak var emailTextField: TextFieldStack!
    @IBOutlet weak var passwordTextField: TextFieldStack!
    @IBOutlet weak var tryAgainLabel: UILabel!
    @IBOutlet weak var resendBtn: UIButton!
    @IBOutlet weak var zoneTextField: TextFieldStack!
    @IBOutlet weak var contactLabel: UITextView!
    
    // MARK: - properties
    
  
    //let user = appDelegate.user
    
    // MARK: - view management
    override func viewDidLoad() {
        print("LoginViewController viewDidLoad")
        super.viewDidLoad()
        let notificationCenter = NotificationCenter.default
        notificationCenter.addObserver(self, selector: #selector(appMovedToForeground), name: UIApplication.didBecomeActiveNotification, object: nil)
        
        if appDelegate.user.emailVerified == true && (appDelegate.user.driverStatus == "active" || appDelegate.user.clientStatus=="active" ){
                  let storyBoard:UIStoryboard = UIStoryboard(name:"Main",bundle: nil)
                  let vc = storyBoard.instantiateViewController(withIdentifier: "ٍNavigationController")
                  self.navigationController?.pushViewController(vc, animated: true)
                  
                  return;
        }
        
        if appDelegate.user.driverStatus == "pending"{
            self.performSegue(withIdentifier: "RegisterDriverViewController", sender: self)
            self.navigationController!.viewControllers.removeAll();
            return;
        }
        
        if appDelegate.user.email != nil {
            emailTextField.textField.text=appDelegate.user.email
        
            if appDelegate.user.emailVerified != nil && !appDelegate.user.emailVerified{
                self.showToast(message:NSLocalizedString("Please check your email to activate the account",comment: ""))
            }
        }
        tryAgainLabel.isHidden=true
        tryAgainLabel.adjustsFontSizeToFitWidth=true
        loaded()
        
    }
    
    @objc func appMovedToForeground() {
        print("func LoginViewController appMovedToForeground")

        if appDelegate.user._id != nil {
            self.performSegue(withIdentifier: "ViewController", sender: self)
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        print("LoginViewController viewWillAppear")
        super.viewWillAppear(animated)
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        print("LoginViewController viewDidAppear")

        
    }
    
    // MARK: - actions
    /*@IBAction func loginWithPhone(_ sender: AnyObject) {
        if let viewController = accountKit.viewControllerForPhoneLogin(with: nil, state: nil) as? AKFViewController {
            prepareLoginViewController(viewController)
            if let viewController = viewController as? UIViewController {
                present(viewController, animated: true, completion: nil)
            }
        }
    }*/
    
    @IBAction func signInFunc(_ sender: UIButton) {
        contactLabel.isHidden=true
        tryAgainLabel.isHidden=true
        if emailTextField.isValid() && passwordTextField.isValid() && zoneTextField.isValid() {
            var parameters: [String: Any]=newParameters();
            parameters["email"]=emailTextField.text()
            parameters["password"]=passwordTextField.text()
            parameters["zone"]=zoneTextField.text()
            loading()
            Alamofire.request(Constants.indexUrl+"/login/", method: .post, parameters: parameters).responseData { response in
                
                if let jsonData = response.data{
                    print(String(decoding: jsonData, as: UTF8.self))
                    let decoder = JSONDecoder()
                    //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                    
                    do{
                        let json = try decoder.decode(JsonResponse.self, from: jsonData)
                        //print(json.code)
                        if(json.code==200&&json.user != nil){
                            //print(json.user.email)
                            self.appDelegate.user=json.user;
                            self.appDelegate.save()
                            self.saveToken()
                            //UIApplication.shared.registerForRemoteNotifications()
                            //if appDelegate.user.emailVerified {
                                if self.appDelegate.user.isClient() && self.appDelegate.user.clientStatus=="active"{
                                    self.performSegue(withIdentifier: "ViewController", sender: self)
                                }
                                else if self.appDelegate.user.isDriver() && self.appDelegate.user.driverStatus=="active"{
                                    self.performSegue(withIdentifier: "ViewController", sender: self)
                                }
                                else if self.appDelegate.user.isAdmin() && self.appDelegate.user.adminStatus=="active"{
                                    self.performSegue(withIdentifier: "ViewController", sender: self)
                                }
                                else if self.appDelegate.user.isDriver() && self.appDelegate.user.driverStatus == "pending"{
                                    self.performSegue(withIdentifier: "RegisterCarSegue1", sender: self)
                                }
                                else {
                                    //self.showToast(message: "The user is blocked")
                                    self.tryAgainLabel.isHidden=false
                                    self.tryAgainLabel.text=NSLocalizedString("The user is blocked",comment: "")
                                }
                            /*}
                            else{
                                //self.showToast(message: "Please check your email to activate the account")
                                self.tryAgainLabel.isHidden=false
                                self.tryAgainLabel.text="Please check your email to activate the account"
                                self.resendBtn.isHidden=false
                            }*/
                        }
                        else if json.code == 201 {
                            //self.showToast(message: "Email or password is not correct")
                            self.tryAgainLabel.isHidden=false
                            self.tryAgainLabel.text=NSLocalizedString("Email or password is not correct",comment: "")
                        }
                        else if json.code == 202{
                            self.zoneTextField.error.isHidden=false
                        }
                        else if json.code == 203{
                            self.contactLabel.isHidden=false
                            var s = String(NSLocalizedString("You are blocked in \"%zone%\" by admin.\nKindly contact zone admin\nMobile: %mobile%\nemail: %email%",comment: ""))
                            s=s.replacingOccurrences(of: "%zone%", with: json.zone.zone)
                            s=s.replacingOccurrences(of: "%mobile%", with: json.zone.mobile)
                            s=s.replacingOccurrences(of: "%email%", with: json.zone.email)
                            self.contactLabel.text=s
                        }
                    }
                    catch let error {
                        print(error)
                        //self.showToast(message: "Please retry again")
                        self.tryAgainLabel.isHidden=false
                        self.tryAgainLabel.text=NSLocalizedString("Please retry again",comment: "")
                    }
                }
                self.loaded()
                
            }
        }
    }
   
    @IBAction func registerFunc(_ sender: UIButton) {
        self.performSegue(withIdentifier: "RegisterViewController", sender: self)
    }
    
    @IBAction func forgetFunc(_ sender: UIButton) {
        self.performSegue(withIdentifier: "ResetPasswordViewController", sender: self)
    }
    
    @IBAction func resendFunc(_ sender: UIButton) {
        var parameters: [String: Any]=newParameters();
        parameters["email"]=emailTextField.text()
        loading()
        Alamofire.request(Constants.indexUrl+"/resend/", method: .post, parameters: parameters).responseData { response in
            
            if let jsonData = response.data{
                print(String(decoding: jsonData, as: UTF8.self))
                let decoder = JSONDecoder()
                //decoder.keyDecodingStrategy = .convertFromSnakeCase
                decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                
                do{
                    let json = try decoder.decode(JsonResponse.self, from: jsonData)
                    print(json.code)
                    if(json.code==200){
                        self.tryAgainLabel.isHidden=true
                        self.showToast(message: NSLocalizedString("Please check your email to activate the account",comment: ""))
                        self.resendBtn.isHidden=true
                    }
                    else if json.code == 201{
                        //self.showToast(message: "Email or password is not correct")
                        self.tryAgainLabel.isHidden=true
                        self.showToast(message: NSLocalizedString("Please retry again",comment: ""))
                    }
                }
                catch let error {
                    print(error)
                    //self.showToast(message: "Please retry again")
                    self.tryAgainLabel.isHidden=true
                    self.showToast(message: NSLocalizedString("Please retry again",comment: ""))
                }
            }
            self.loaded()
        }
    }
    
    

  
    
    func saveUser(){
        print("saveUser")
        /*if let name = nameTextField.text,name.count>0 {
            //user.name=name
            let parameters=user.getMap();
            nameTextField.isHidden=true
            clientButton.isHidden=true
            driverButton.isHidden=true
            tryAgainLabel.isHidden=true
            
            Alamofire.request(Constants.indexUrl+"/users/", method: .post, parameters: parameters).responseJSON { response in
                
                if let json = response.result.value as? [String: Any],let code=json["code"] as? Double ,code==200,
                    let userData=json["data"] as? [[String:Any]],
                    let userId=userData[0]["_id"] as? String{
                    self.user.userId=userId;
                    self.user.save();
                    self.accountKit.logOut()
                    self.performSegue(withIdentifier: "showAccount", sender: self)
                }
                else{
                    self.nameTextField.isHidden=false
                    self.clientButton.isHidden=false
                    self.driverButton.isHidden=false
                    self.tryAgainLabel.isHidden=false
                }
                
            }
        }
        else{
            nameTextField.attributedPlaceholder=NSAttributedString(string: NSLocalizedString("Enter user name", comment: ""),
                                                                   attributes: [NSAttributedStringKey.foregroundColor: UIColor.red])
        }*/
    }
    
    
    func saveToken(){
        print("saveToken")
        if appDelegate.user._id != nil && appDelegate.iosToken != "" && (appDelegate.user.iosToken == nil || appDelegate.user.iosToken != appDelegate.iosToken) {
            var parameters: [String: Any]=[:];
            parameters["_id"]=appDelegate.user._id
            parameters["hashedKey"]=appDelegate.user.hashedKey
            parameters["iosToken"]=appDelegate.iosToken
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
                                self.appDelegate.user=json.user;
                                self.appDelegate.save()
                               
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
  
    
    
   
}

