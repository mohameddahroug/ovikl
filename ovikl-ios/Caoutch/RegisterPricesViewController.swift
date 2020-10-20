import UIKit
import Foundation
import Alamofire
import CoreData
class RegisterPricesViewController: UIViewController2{
    
    @IBOutlet weak var minimumTextField: TextFieldStack!
    @IBOutlet weak var baseTextField: TextFieldStack!
    @IBOutlet weak var kmTextField: TextFieldStack!
    @IBOutlet weak var minuteTextField: TextFieldStack!
    @IBOutlet weak var currencyTextField: TextFieldStack!
    
    @IBOutlet weak var costLabel: UILabel!
    @IBAction func Save(_ sender: UIButton) {
        currencyTextField.endEditing(true)
        if validate() {
            var parameters: [String: Any]=[:];
            
            parameters["_id"]=appDelegate.user._id
            parameters["hashedKey"]=appDelegate.user.hashedKey
            parameters["cost.minimum"]=minimumTextField.text()
            parameters["cost.base"]=baseTextField.text()
            parameters["cost.km"]=kmTextField.text()
            parameters["cost.minute"]=minuteTextField.text()
            parameters["cost.currency"]=currencyTextField.text()
            if appDelegate.user.iosToken == nil {
                parameters["iosToken"]=appDelegate.iosToken
                parameters["fcmToken"]=""
            }
            if appDelegate.user.driverStatus == nil || appDelegate.user.driverStatus == "pending"{
                parameters["driverStatus"]="active"
            }
            if let lang = Locale.current.languageCode {
                parameters["lang"]=lang;
            }
            parameters["time"]=Constants.dateFormatter.string(from: Date());
            
            loading()
            Alamofire.request(Constants.indexUrl+"/register2/", method: .post, parameters: parameters).responseData { response in
                
                if let jsonData = response.data{
                    print(String(decoding: jsonData, as: UTF8.self))
                    let decoder = JSONDecoder()
                    //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                    
                    do{
                        let json = try decoder.decode(JsonResponse.self, from: jsonData)
                        print(json.code ?? "")
                        if(json.code==200){
                            if(json.user != nil){
                                print(json.user.email ?? "")
                                self.appDelegate.user=json.user;
                                self.appDelegate.save()
                                self.performSegue(withIdentifier: "prices2viewSegue", sender: self)
                            }
                        }
                        else if(json.code==201){
                            self.showToast(message:NSLocalizedString("Please retry again",comment: ""))
                        }
                    }
                    catch let error {
                        print(error)
                        self.showToast(message:NSLocalizedString("Please retry again", comment: ""))
                    }
                    self.loaded()
                }
                
                
            }
        }
        
    }
    
    
    override func viewDidLoad() {
           super.viewDidLoad()
        costLabel.attributedText=NSAttributedString(string: NSLocalizedString("cost_calculation",comment: ""))
       }
    
    override func viewDidAppear(_ animated: Bool) {
        refreshUser()
    }
    
    
    private func validate()->Bool{
        
        if minimumTextField.isValid() && baseTextField.isValid() && minuteTextField.isValid() && kmTextField.isValid() && currencyTextField.isValid() {
            return true
        }
        else{
            return false
        }
    }
    
    
    
    override func loaded(){
        super.loaded()
        if appDelegate.user.cost != nil {
            if appDelegate.user.cost.minimum != nil{
                minimumTextField.textField.text=String(appDelegate.user.cost.minimum)
            }
            if appDelegate.user.cost.base != nil{
                baseTextField.textField.text=String(appDelegate.user.cost.base)
            }
            if appDelegate.user.cost.minute != nil{
                minuteTextField.textField.text=String(appDelegate.user.cost.minute)
            }
            if appDelegate.user.cost.km != nil{
                kmTextField.textField.text=String(appDelegate.user.cost.km)
            }
            if appDelegate.user.cost.currency != nil{
                currencyTextField.textField.text=String(appDelegate.user.cost.currency)
            }
        }
    }
       
}
