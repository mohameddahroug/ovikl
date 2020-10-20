//
//  UIViewController2.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 19/12/2019.
//  Copyright © 2019 Caoutch. All rights reserved.
//

import Foundation
import UIKit
import Alamofire
import CoreData
import GoogleMobileAds

class UIViewController2: UIViewController,GADBannerViewDelegate{
    
    var appDelegate:AppDelegate!;
    var managedContext:NSManagedObjectContext!;
    
    var height:CGFloat!
    var keyboardHeight:CGFloat!
    var constraint:NSLayoutConstraint!
    
    var fullView:UIView!
    
    var indicator:UIActivityIndicatorView!
    
    var retryBtn:UIButton!
    
    
    var adsView:GADBannerView!
    
    
    override func viewDidLoad() {
       
        appDelegate = UIApplication.shared.delegate as! AppDelegate
        if appDelegate.user._id == nil{
            appDelegate.load()
        }
        managedContext = appDelegate.persistentContainer.viewContext
        setupViewResizerOnKeyboardShown()
        self.view.backgroundColor=Constants.white
        super.viewDidLoad()
        
        fullView=UIView(frame: CGRect(x: 0, y: 0, width: view.frame.width, height: view.frame.height))
        fullView.backgroundColor = Constants.white.withAlphaComponent(0.6)
        self.view.addSubview(fullView)
//        if #available(iOS 13.0, *) {
//            overrideUserInterfaceStyle = .light
//        } 
        indicator=UIActivityIndicatorView(frame: CGRect(x: view.frame.width/2-25, y: view.frame.height/2-25, width: 50, height: 50))
        fullView.addSubview(indicator)
        
        retryBtn=UIButton(frame: CGRect(x: view.frame.width/2-75, y: view.frame.height/2-25, width: 150, height: 50))
        retryBtn.setTitle("Retry", for: UIControl.State.normal)
        retryBtn.isHidden=true
        retryBtn.backgroundColor=Constants.colorPrimary
        retryBtn.layer.cornerRadius = 8.0
        let gesture = UITapGestureRecognizer(target: self, action:  #selector(refreshUser))
        retryBtn.addGestureRecognizer(gesture)
        
        fullView.addSubview(retryBtn)
        
        constraint=self.view.subviews[0].bottomAnchor.constraint( equalTo: self.view.bottomAnchor ,constant: 0)
        NSLayoutConstraint.activate([
            constraint/*,
             self.fullView.leadingAnchor.constraint( equalTo: self.view.leadingAnchor),
             self.fullView.topAnchor.constraint( equalTo: self.view.topAnchor),
             self.fullView.widthAnchor.constraint( equalTo: self.view.widthAnchor),
             self.fullView.heightAnchor.constraint( equalTo: self.view.heightAnchor),
             self.retryBtn.centerXAnchor.constraint( equalTo: self.fullView.centerXAnchor),
             self.retryBtn.centerYAnchor.constraint( equalTo: self.fullView.centerYAnchor),
             self.indicator.centerXAnchor.constraint( equalTo: self.fullView.centerXAnchor),
             self.indicator.centerYAnchor.constraint( equalTo: self.fullView.centerYAnchor),*/
        ])
        
        if appDelegate.user._id == nil{
            appDelegate.load()
        }
        
        let navBarHeight = UIApplication.shared.statusBarFrame.size.height +
               (navigationController?.navigationBar.frame.height ?? 0.0)
        
        adsView = GADBannerView(frame: CGRect(x: view.frame.width/2-150, y: navBarHeight, width: 300, height: 50))
        adsView.adUnitID = "ca-app-pub-6615275988084929/3257153734"
        adsView.rootViewController = self
        adsView.load(GADRequest())
        adsView.delegate = self
        self.view.addSubview(adsView)
       
        
        
    }
    
    
    
    deinit {
        print("func ViewController2 deinit");
        NotificationCenter.default.removeObserver(self)
    }
    
    func setupViewResizerOnKeyboardShown() {
        print("func setupViewResizerOnKeyboardShown");
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(keyboardWillShowForResizing),
                                               name: UIResponder.keyboardWillShowNotification,
                                               object: nil)
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(keyboardWillHideForResizing),
                                               name: UIResponder.keyboardWillHideNotification,
                                               object: nil)
    }
    
    
    @objc func keyboardWillShowForResizing(notification: Notification) {
        print("func keyboardWillShowForResizing");
        /*if let keyboardSize = (notification.userInfo?[UIKeyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue,
         let window = self.view.window?.frame {
         // We're not just minusing the kb height from the view height because
         // the view could already have been resized for the keyboard before
         self.view.frame = CGRect(x: self.view.frame.origin.x,
         y: self.view.frame.origin.y,
         width: self.view.frame.width,
         height: window.origin.y + window.height - keyboardSize.height)
         } else {
         debugPrint("We're showing the keyboard and either the keyboard size or window is nil: panic widely.")
         }*/
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue{
            height=keyboardSize.height;
            if let height = height{
                NSLayoutConstraint.deactivate([
                    constraint!
                ])
                constraint?.constant=height * -1
                NSLayoutConstraint.activate([
                    constraint!
                ])
            }
        }
        
    }
    @objc func keyboardWillHideForResizing(notification: Notification) {
        print("func keyboardWillHideForResizing");
        /*if let keyboardSize = (notification.userInfo?[UIKeyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
         let viewHeight = self.view.frame.height
         self.view.frame = CGRect(x: self.view.frame.origin.x,
         y: self.view.frame.origin.y,
         width: self.view.frame.width,
         height: viewHeight + keyboardSize.height)
         } else {
         debugPrint("We're about to hide the keyboard and the keyboard size is nil. Now is the rapture.")
         }*/
        NSLayoutConstraint.deactivate([
            constraint!
        ])
        constraint?.constant = 0
        NSLayoutConstraint.activate([
            constraint!
        ])
        
    }
    
    
    func showToast(message:String){
        let toastLabel = UILabel(frame: CGRect(x: 10, y: self.view.frame.size.height-100, width: self.view.frame.size.width-20, height: 35))
        toastLabel.adjustsFontSizeToFitWidth=true
        toastLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        toastLabel.textColor = UIColor.white
        toastLabel.textAlignment = .center;
        //toastLabel.font = UIFont(name: "Montserrat-Light", size: 12.0)
        toastLabel.text = message
        toastLabel.alpha = 1.0
        toastLabel.layer.cornerRadius = 10;
        toastLabel.clipsToBounds  =  true
        self.view.addSubview(toastLabel)
        UIView.animate(withDuration: 5.0, delay: 0.1, options: .curveEaseOut, animations: {
            toastLabel.alpha = 0.0
        }, completion: {(isCompleted) in
            toastLabel.removeFromSuperview()
            
        })
    }
    
    func loading(){
        print("func loading");
        fullView.isHidden=false
        indicator.startAnimating()
        retryBtn.isHidden=true
    }
    
    func loaded(){
        print("func loaded");
        fullView.isHidden=true
        indicator.stopAnimating()
        retryBtn.isHidden=true
        view.bringSubviewToFront(adsView)
    }
    
    func failed(){
        print("func failed");
        fullView.isHidden=false
        indicator.stopAnimating()
        retryBtn.isHidden=false
        view.bringSubviewToFront(fullView)
    }
    
    
    func newParameters()->[String: Any]{
        var parameters: [String: Any]=[:];
        if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
            parameters["ver"] = "i"+version;
        }
        if let lang = Locale.current.languageCode {
            parameters["lang"]=lang;
        }
        parameters["time"]=Constants.dateFormatter.string(from: Date());
        return parameters
    }
    
    @objc func refreshUser(){
        print("func refreshUser");
        var parameters: [String: Any]=newParameters();
        parameters["_id"]=appDelegate.user._id
        parameters["hashedKey"]=appDelegate.user.hashedKey
        parameters["type"]=appDelegate.user.type
        
        loading()
        Alamofire.request(Constants.indexUrl+"/user", method: .post, parameters: parameters).responseData { response in
            
            if let jsonData = response.data{
                print("UIViewController2",String(decoding: jsonData, as: UTF8.self))
                let decoder = JSONDecoder()
                //decoder.keyDecodingStrategy = .convertFromSnakeCase
                decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                
                do{
                    let json = try decoder.decode(JsonResponse.self, from: jsonData)
                    
                    if(json.code==200&&json.user != nil){
                        if self.appDelegate.user.zone != nil && self.appDelegate.user.zone != "" && json.user.zone != nil && self.appDelegate.user.zone != json.user.zone{
                            self.showToast(message: NSLocalizedString("You are removed from zone", comment: ""))
                        }
                        self.appDelegate.user=json.user;
                        self.appDelegate.save()
                        self.appDelegate.config=json.config;
                        Constants.vehicles=json.vehicles;
                        
                        self.loaded()
                        
                    }
                    else if json.code == 201{
                        //self.showToast(message: "Email or password is not correct")
                        self.failed()
                    }
                }
                catch let error {
                    print(error)
                    self.failed()
                    self.showToast(message: NSLocalizedString("Please retry again",comment: ""))
                    
                }
            }
            
            
        }
    }
    
    
    
    /// Tells the delegate an ad request loaded an ad.
    func adViewDidReceiveAd(_ bannerView: GADBannerView) {
        print("adViewDidReceiveAd")
    }
    
    /// Tells the delegate an ad request failed.
    func adView(_ bannerView: GADBannerView,
                didFailToReceiveAdWithError error: GADRequestError) {
        print("adView:didFailToReceiveAdWithError: \(error.localizedDescription)")
    }
    
    /// Tells the delegate that a full-screen view will be presented in response
    /// to the user clicking on an ad.
    func adViewWillPresentScreen(_ bannerView: GADBannerView) {
        print("adViewWillPresentScreen")
    }
    
    /// Tells the delegate that the full-screen view will be dismissed.
    func adViewWillDismissScreen(_ bannerView: GADBannerView) {
        print("adViewWillDismissScreen")
    }
    
    /// Tells the delegate that the full-screen view has been dismissed.
    func adViewDidDismissScreen(_ bannerView: GADBannerView) {
        print("adViewDidDismissScreen")
    }
    
    /// Tells the delegate that a user click will open another app (such as
    /// the App Store), backgrounding the current app.
    func adViewWillLeaveApplication(_ bannerView: GADBannerView) {
        print("adViewWillLeaveApplication")
    }
    
}
