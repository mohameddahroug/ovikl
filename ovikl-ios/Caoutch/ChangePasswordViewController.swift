
import UIKit
import Foundation
import Alamofire
import CoreData
class ChangePasswordViewController: UIViewController2{
   
    
    @IBOutlet weak var oldPasswordTextField: TextFieldStack!
    @IBOutlet weak var newPasswordTextField: TextFieldStack!
    override func viewDidLoad() {
        super.viewDidLoad()
        
    
       
    }
    
    override func viewDidAppear(_ animated: Bool) {
        refreshUser()
    }
    
    
    @IBAction func save(_ sender: UIButton) {
    
        newPasswordTextField.textField.endEditing(true)
        if validate() {
            var parameters: [String: Any]=[:];
            parameters["_id"]=appDelegate.user._id
            parameters["hashedKey"]=appDelegate.user.hashedKey
            parameters["oldPassword"]=oldPasswordTextField.text()
            parameters["newPassword"]=newPasswordTextField.text()
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
                        else if json.code == 204{
                            self.oldPasswordTextField.error.isHidden=false
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
        
       
       
        if oldPasswordTextField.isValid() && newPasswordTextField.isValid() {
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
