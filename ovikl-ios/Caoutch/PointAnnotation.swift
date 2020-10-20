//
//  PointAnnotation.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 25/02/2020.
//  Copyright © 2020 Caoutch. All rights reserved.
//

import Foundation
import MapKit

class PointAnnotation: NSObject, MKAnnotation{
    dynamic var coordinate: CLLocationCoordinate2D=CLLocationCoordinate2D()
    var driver:User!
    var _id:String!
    var icon:UIImage!
    var showClientInfo=true;
}
