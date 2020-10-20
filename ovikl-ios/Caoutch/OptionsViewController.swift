//
//  ChatViewController.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 8/19/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import UIKit
import Foundation

class OptionsViewController: UIViewController {
    var delegate: OptionsViewDelegate?
    var lang:String?
    
    @IBOutlet weak var alertView: UIStackView!
    @IBOutlet weak var changeLocationItem: UIButton!
    @IBOutlet weak var confirmLocationItem: UIButton!
    @IBOutlet weak var okItem: UIButton!
    @IBOutlet weak var arrivedItem: UIButton!
    @IBOutlet weak var finishItem: UIButton!
    @IBOutlet weak var cancelItem: UIButton!
    @IBOutlet weak var chatItem: UIButton!
    @IBOutlet weak var callItem: UIButton!
    @IBOutlet weak var mapItem: UIButton!
    @IBOutlet weak var expectedItem: UIButton!
    
    //@IBOutlet weak var promoItem: UIButton!
    
    
    @IBAction func changeStartPoint(_ sender: UIButton) {
        delegate?.changeStartPoint()
        self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func confirmStartPoint(_ sender: UIButton) {
        delegate?.confirmStartPoint()
        self.dismiss(animated: true, completion: nil)
    }
    
    
    @IBAction func accept(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.accept()
    }
    
    @IBAction func arrived(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.arrived()
    }
    
    @IBAction func finish(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.finish()
    }
    
    @IBAction func cancel(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.cancel()
    }
    
    @IBAction func chat(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.chat()
    }
    
    @IBAction func call(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.call()
    }
    
    @IBAction func map(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.map()
    }
    
    @IBAction func lastTrip(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.lastTrip()
    }
    
    @IBAction func support(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.support()
    }
    
    @IBAction func expected(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
        delegate?.expected()
    }
    
  
    
    
    @objc func closeMenu(){
        self.dismiss(animated: true, completion: nil)
    }
    
    let alertViewGrayColor = UIColor(red: 224.0/255.0, green: 224.0/255.0, blue: 224.0/255.0, alpha: 1)
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        setupView()
        //animateView()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        view.layoutIfNeeded()
       
    }
    
    func setupView() {
        alertView.layer.cornerRadius = 15
        self.view.backgroundColor = UIColor.black.withAlphaComponent(0.2)
        let gesture = UITapGestureRecognizer(target: self, action: #selector(closeMenu))
        gesture.numberOfTapsRequired = 1
        view.addGestureRecognizer(gesture)
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        let user=appDelegate.user
//        if user.isClient(){
//            promoItem.isHidden=false;
//        }
    }
    
    /*func animateView() {
        alertView.alpha = 0;
        self.alertView.frame.origin.y = self.alertView.frame.origin.y + 50
        UIView.animate(withDuration: 0.4, animations: { () -> Void in
            self.alertView.alpha = 1.0;
            self.alertView.frame.origin.y = self.alertView.frame.origin.y - 50
        })
    }*/
    
}


protocol OptionsViewDelegate: class {
    func changeStartPoint()
    func confirmStartPoint()
    func accept()
    func arrived()
    func finish()
    func cancel()
    func chat()
    func call()
    func map()
    func lastTrip()
    func support()
    func expected()
}
