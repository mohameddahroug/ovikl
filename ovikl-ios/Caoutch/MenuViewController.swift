//
//  MenuViewController.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 8/13/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import UIKit
import Foundation


/*protocol MenuViewControllerDelegate{
    func didSelectColor(controller:MenuViewController,text:String)
}*/

class MenuViewController: UIViewController {
    
    
    
    @IBOutlet weak var label: UILabel!
    
   
    @IBOutlet weak var alertView: UIView!
    
    @IBOutlet weak var vehicleInfoButton: UIButton!
    @IBOutlet weak var pricesButton: UIButton!
    @IBOutlet var lastTripsButton: UIButton!
    

    
    @IBOutlet weak var callButton: UIButton!
    
    @IBOutlet var zoneUsersButton: UIButton!
    @IBOutlet var zoneTripsButton: UIButton!
    @IBOutlet var zoneBlockedUsersButton: UIButton!
    
    var delegate: MenuViewDelegate?
    var lang:String?
    
    let alertViewGrayColor = UIColor(red: 224.0/255.0, green: 224.0/255.0, blue: 224.0/255.0, alpha: 1)
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        setupView()
        //animateView()
        //let user=appDelegate.user
        let trip=TripSingelton.mInstance;
        var a = appDelegate.user.firstName + "\n" + appDelegate.user.email + "\n" + NSLocalizedString(appDelegate.user.type,comment: "")
        if appDelegate.user.zone != nil && appDelegate.user.zone != ""{
            a=a+" "+NSLocalizedString("in",comment: "")+" "+appDelegate.user.zone;
        }
        label.text=a
            
        if appDelegate.user.isDriver(){
            
            vehicleInfoButton.isHidden=false
            pricesButton.isHidden=false
            
        }
        else if appDelegate.user.isAdmin(){
            lastTripsButton.isHidden=true
            zoneUsersButton.isHidden=false
            zoneTripsButton.isHidden=false
            zoneBlockedUsersButton.isHidden=false
        }
        
        if appDelegate.user.zone != nil && appDelegate.user.zone != "" && appDelegate.user.zoneContact != nil &&
            appDelegate.user.zoneContact.mobile != nil && appDelegate.user.zoneContact.mobile != ""{
            callButton.isHidden=false
        }
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        view.layoutIfNeeded()
        
        
    }
    
    func setupView() {
        alertView.layer.cornerRadius = 0
        self.view.backgroundColor = UIColor.black.withAlphaComponent(0.2)
        let gesture = UITapGestureRecognizer(target: self, action: #selector(closeMenu))
        gesture.numberOfTapsRequired = 1
        view.addGestureRecognizer(gesture)
        let gesture2 = UITapGestureRecognizer(target: self, action: #selector(closeMenu))
        gesture2.numberOfTapsRequired = 1
        alertView.addGestureRecognizer(gesture2)
    }
    
    func animateView() {
         alertView.alpha = 0;
         alertView.frame.origin.x = -250
         alertView.layer.cornerRadius  = 0.0; 
         UIView.animate(withDuration: 0.4, animations: { () -> Void in
            self.alertView.alpha = 4.0;
            self.alertView.frame.origin.x = 0
         })
     }
    
    @objc func closeMenu(){
        self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func changePassword(_ sender: Any) {
        self.dismiss(animated: true, completion: nil)
        delegate?.changePassword()
    }
    
    
    @IBAction func myInfo(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.myInfo()
    }
    
    @IBAction func lastTrip(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.menuLastTrip()
    }
    
   
    
    @IBAction func vehicleInfo(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.vehicleInfo()
    }
    
    @IBAction func prices(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.prices()
    }
    
    @IBAction func support(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.menuSupport()
    }
    
    @IBAction func about(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.about()
    }
    
    @IBAction func logout(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.logout()
    }
    
    
  
    
    @IBAction func callSupport(_ sender: UIButton) {
        delegate?.callSupport()
    }
    
    @IBAction func zoneUsers(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.zoneUsers()
    }
    
    @IBAction func zoneTrips(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.zoneTrips()
    }
    
    
    @IBAction func zoneBlockedUsers(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.zoneBlockedUsers()
    }
    
}
protocol MenuViewDelegate: class {
    func menuLastTrip()
    func menuSupport()
    func about()
    func logout()
    func myInfo()
    func changePassword()
    func vehicleInfo()
    func prices()
 
    func callSupport()
    func zoneUsers()
    func zoneTrips()
    func zoneBlockedUsers()
}
