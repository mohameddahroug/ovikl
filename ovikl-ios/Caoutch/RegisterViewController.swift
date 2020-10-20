import UIKit
import Foundation
import Alamofire
import CoreData
class RegisterViewController: UIViewController2{
   
    
    @IBOutlet weak var accountTypeLabel: UILabel!
    @IBOutlet weak var driverBtn: BtnImage!
    @IBOutlet weak var clientBtn: BtnImage!
    @IBOutlet weak var firstNameTextField: TextFieldStack!
    @IBOutlet weak var lastNameTextField: TextFieldStack!
    @IBOutlet weak var emailTextField: TextFieldStack!
    @IBOutlet weak var passwordTextField: TextFieldStack!
    //@IBOutlet weak var repasswordTextField: TextFieldStack!
    @IBOutlet weak var mobileTextField: TextFieldStack!
    @IBOutlet weak var zoneTextField: TextFieldStack!
    @IBOutlet weak var contactLabel: UITextView!
    
    var imagePicker = UIImagePickerController()
  
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let gestureDriver = UITapGestureRecognizer(target: self, action:  #selector(selectDriver))
        driverBtn?.addGestureRecognizer(gestureDriver)

        let gestureClient = UITapGestureRecognizer(target: self, action:  #selector(selectClient))
        clientBtn?.addGestureRecognizer(gestureClient)
        
       
        clientBtn.setImage("/client.png",managedContext)
        driverBtn.setImage("/driver.png",managedContext)
        loaded()
        
    }
    
    @IBAction func save(_ sender: Any) {
        mobileTextField.endEditing(true)
        if validate() {
            var parameters: [String: Any]=[:];
            parameters["firstName"]=firstNameTextField.text()
            parameters["lastName"]=lastNameTextField.text()
            parameters["email"]=emailTextField.text()
            parameters["password"]=passwordTextField.text()
            parameters["mobile"]=mobileTextField.text()
            parameters["zone"]=zoneTextField.text()
            if driverBtn.selected {
                parameters["type"]="driver"
            }
            else{
                parameters["type"]="client"
            }
            
            if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
                parameters["ver"] = "i"+version;
            }
            if let lang = Locale.current.languageCode {
                parameters["lang"]=lang;
            }
            parameters["time"]=Constants.dateFormatter.string(from: Date());
            
            loading()
            Alamofire.request(Constants.indexUrl+"/register/", method: .post, parameters: parameters).responseData { response in
                
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
                                UIApplication.shared.registerForRemoteNotifications()
                                self.showToast(message:NSLocalizedString("Please check your email to activate the account",comment: ""))
                                
                                    //DispatchQueue.main.asyncAfter(deadline: .now() + .seconds(5)) {
                                if self.appDelegate.user.type == "driver"{
                                            self.performSegue(withIdentifier: "RegisterCarSegue1", sender: self)
                                        }
                                        else{
                                            self.performSegue(withIdentifier: "ViewControllerSegue2", sender: self)
                                        }
                                    //}
                               
                               
                            }
                        }
                        else if(json.code==201){
                            self.showToast(message:NSLocalizedString("Email already registered",comment: ""))
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
    
    @objc func selectDriver(sender : UITapGestureRecognizer) {
        print("func selectDriver");
        driverBtn.setSelect(selected:true)
        clientBtn.setSelect(selected:false)
        accountTypeLabel.text=NSLocalizedString("Register as driver",comment: "")
        accountTypeLabel.textColor=Constants.colorPrimaryDark
    }
    
    @objc func selectClient(sender : UITapGestureRecognizer) {
        print("func selectClient");
        driverBtn.setSelect(selected:false)
        clientBtn.setSelect(selected:true)
        accountTypeLabel.text=NSLocalizedString("Register as client", comment: "")
        accountTypeLabel.textColor=Constants.colorPrimaryDark
    }
    
    
    
    private func validate()->Bool{
        contactLabel.isHidden=true
        if !driverBtn.selected && !clientBtn.selected {
            accountTypeLabel.textColor=Constants.red
            return false
        }
        if firstNameTextField.isValid() && lastNameTextField.isValid() && emailTextField.isValid() && mobileTextField.isValid() && passwordTextField.isValid() /*&& repasswordTextField.isValid() && repasswordTextField.match(string: passwordTextField.text())*/
            && zoneTextField.isValid() {
            return true
        }
        else{
            return false
        }
    }
     
    
 


  
}
