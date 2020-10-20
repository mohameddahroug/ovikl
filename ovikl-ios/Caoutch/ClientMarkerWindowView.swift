//
//  ClientMarkerWindowView.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 9/1/18.
//  Copyright © 2018 Caoutch. All rights reserved.
//

import Foundation
import UIKit



class ClientMarkerWindowView: UIView {
    
    @IBOutlet weak var totaltripsLabel: UILabel!
    @IBOutlet weak var tripsCountLabel: UILabel!
    @IBOutlet weak var claimsLabel: UILabel!
    @IBOutlet weak var createDateLabel: UILabel!
    var spotData: NSDictionary?
 
    class func instanceFromNib() -> UIView {
        return UINib(nibName: "ClientMarkerWindowView", bundle: nil).instantiate(withOwner: self, options: nil).first as! UIView
    }
}
