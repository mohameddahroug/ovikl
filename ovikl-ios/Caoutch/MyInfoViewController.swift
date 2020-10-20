
import UIKit
import Foundation
import Alamofire
import CoreData
class MyInfoViewController: UIViewController2{
   
    
    
    @IBOutlet weak var firstNameTextField: TextFieldStack!
    @IBOutlet weak var lastNameTextField: TextFieldStack!
    @IBOutlet weak var mobileTextField: TextFieldStack!
    @IBOutlet weak var zoneTextField: TextFieldStack!
    @IBOutlet weak var contactLabel: UITextView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        firstNameTextField.textField.text=String(appDelegate.user.firstName)
        lastNameTextField.textField.text=String(appDelegate.user.lastName)
        mobileTextField.textField.text=String(appDelegate.user.mobile)
        zoneTextField.textField.text=String(appDelegate.user.zone)
        if appDelegate.user.isAdmin(){
            zoneTextField.textField.isEnabled=false
        }
       
    }
    
    override func viewDidAppear(_ animated: Bool) {
        refreshUser()
    }
    
    
    @IBAction func save(_ sender: Any) {
        mobileTextField.endEditing(true)
        if validate() {
            var parameters: [String: Any]=[:];
            parameters["_id"]=appDelegate.user._id
            parameters["hashedKey"]=appDelegate.user.hashedKey
            parameters["firstName"]=firstNameTextField.text()
            parameters["lastName"]=lastNameTextField.text()
            parameters["mobile"]=mobileTextField.text()
            parameters["email"]=appDelegate.user.email
            parameters["zone"]=zoneTextField.text()
            
            if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
                parameters["ver"] = "i"+version;
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
                        print(json.code)
                        if(json.code==200){
                            if(json.user != nil){
                                print(json.user.email)
                                self.appDelegate.user=json.user;
                                self.appDelegate.save()
                                self.navigationController?.popViewController(animated: true)
                               
                            }
                        }
                        else if(json.code==201){
                            self.showToast(message:NSLocalizedString("Please retry again",comment: ""))
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
                        self.showToast(message:NSLocalizedString("Please retry again",comment: ""))
                    }
                    self.loaded()
                }
                
                
            }
            
        }
    }
    

    
    
    private func validate()->Bool{
        
       
        contactLabel.isHidden=true
        if firstNameTextField.isValid() && lastNameTextField.isValid() && mobileTextField.isValid() && zoneTextField.isValid() {
            return true
        }
        else{
            return false
        }
    }
     
    
 override func loaded(){
     super.loaded()
    
 }


  
}
