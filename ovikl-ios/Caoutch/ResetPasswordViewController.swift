import UIKit
import Foundation
import Alamofire
class ResetPasswordViewController: UIViewController2{
    
    @IBOutlet weak var emailTextField: TextFieldStack!
    @IBOutlet weak var keyTextField: TextFieldStack!
    @IBOutlet weak var passwordTextField: TextFieldStack!
    //@IBOutlet weak var repasswordTextField: TextFieldStack!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        loaded()
    }
    
    @IBAction func save(_ sender: UIButton) {
        
        if emailTextField.isValid() && keyTextField.isValid() && passwordTextField.isValid() /*&& repasswordTextField.isValid()
            && repasswordTextField.match(string: passwordTextField.text())*/{
            var parameters: [String: Any]=newParameters();
            parameters["email"]=emailTextField.text()
            parameters["password"]=passwordTextField.text()
            parameters["resetKey"]=keyTextField.text()
            loading()
            Alamofire.request(Constants.indexUrl+"/reset_password/", method: .post, parameters: parameters).responseData { response in
                
                if let jsonData = response.data{
                    print(String(decoding: jsonData, as: UTF8.self))
                    let decoder = JSONDecoder()
                    //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                    
                    do{
                        let json = try decoder.decode(JsonResponse.self, from: jsonData)
                        print(json.code)
                        if(json.code==200&&json.user != nil){
                            print(json.user.email)
                            self.appDelegate.user=json.user;
                            self.appDelegate.save()
                            //if appDelegate.user.emailVerified {
                            if self.appDelegate.user.clientStatus=="active"{
                                    self.performSegue(withIdentifier: "showAccount", sender: self)
                                }
                            else if self.appDelegate.user.isDriver() && self.appDelegate.user.clientStatus=="pending"{
                                    self.performSegue(withIdentifier: "registerDriverSegue", sender: self)
                                }
                                else {
                                    self.showToast(message: NSLocalizedString("The user is blocked",comment: ""))
                                }
                            /*}
                            else{
                                //self.showToast(message: "Please check your email to activate the account")
                            }*/
                        }
                        else if json.code == 201{
                            self.showToast(message: NSLocalizedString("Invalid email or reset key",comment: ""))
                        }
                    }
                    catch let error {
                        print(error)
                        
                    }
                }
                self.loaded()
                
            }
        }
        
    }
    
    @IBAction func reset(_ sender: UIButton) {
        if emailTextField.isValid() {
            var parameters: [String: Any]=newParameters();
            parameters["email"]=emailTextField.text()
            loading()
            Alamofire.request(Constants.indexUrl+"/generate_reset_key/", method: .post, parameters: parameters).responseData { response in
                
                if let jsonData = response.data{
                    print(String(decoding: jsonData, as: UTF8.self))
                    let decoder = JSONDecoder()
                    //decoder.keyDecodingStrategy = .convertFromSnakeCase
                    decoder.dateDecodingStrategy = .formatted(Constants.dateFormatter)
                    
                    do{
                        let json = try decoder.decode(JsonResponse.self, from: jsonData)
                        print(json.code)
                        if(json.code==200){
                            self.showToast(message: NSLocalizedString("Please check your email to aget your reset key",comment: ""))
                        }
                        else if json.code == 201{
                            self.showToast(message: NSLocalizedString("Invalid email",comment: ""))
                        }
                    }
                    catch let error {
                        print(error)
                        self.showToast(message: NSLocalizedString("Please retry again",comment: ""))
                    }
                }
                self.loaded()
            }
        }
    }
    
}
