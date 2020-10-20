import UIKit
import Foundation
import Alamofire
import CoreData
class RegisterCarViewController: UIViewController2,UINavigationControllerDelegate, UIImagePickerControllerDelegate {
    
 
    
    
    @IBOutlet weak var typeErrLabel: UILabel!
    @IBOutlet weak var manufacturerTextField: TextFieldStack!
    @IBOutlet weak var modelTextField: TextFieldStack!
    @IBOutlet weak var madYearTextField: TextFieldStack!
    @IBOutlet weak var plateNumberTextField: TextFieldStack!
    @IBOutlet weak var frontSelectImage: SelectImage!
    @IBOutlet weak var sideSelectImage: SelectImage!
    @IBOutlet weak var backSelectImage: SelectImage!
    @IBOutlet weak var saveBtn: UIButton!
    var typesStackView: UIStackView!;
    @IBOutlet weak var typeScrollView: UIScrollView!
    @IBAction func save(_ sender: UIButton) {
        
        modelTextField.endEditing(true)
        if validate() {
            var parameters: [String: Any]=[:];
            var type:String!
            for v in typesStackView.subviews{
                if let b = v as? BtnImage,b.selected {
                    type=b.type
                }
            }
            
            parameters["_id"]=appDelegate.user._id
            parameters["hashedKey"]=appDelegate.user.hashedKey
            parameters["carType"]=type
            parameters["carManufacturer"]=manufacturerTextField.text()
            parameters["carModel"]=modelTextField.text()
            parameters["carMadeYear"]=madYearTextField.text()
            parameters["carNumber"]=plateNumberTextField.text()
            if frontSelectImage.large != nil{
                parameters["images."+Constants.frontImage]=frontSelectImage.large
            }
            parameters["images."+Constants.frontImageSmall]=frontSelectImage.small
            if sideSelectImage.large != nil{
                parameters["images."+Constants.sideImage]=sideSelectImage.large
            }
            parameters["images."+Constants.sideImageSmall]=sideSelectImage.small
            if backSelectImage.small != nil{
                parameters["images."+Constants.backImageSmall]=backSelectImage.small
            }
            if backSelectImage.large != nil {
                parameters["images."+Constants.backImage]=backSelectImage.large
            }
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
                        print(json.code ?? "")
                        if(json.code==200){
                            if(json.user != nil){
                                print(json.user.email ?? "")
                                self.appDelegate.user=json.user;
                                self.appDelegate.save()
                                if self.appDelegate.user.driverStatus == "active" || self.appDelegate.user.clientStatus == "active" {
                                    self.navigationController?.popViewController(animated: true)
                                }
                                else{
                                    self.performSegue(withIdentifier: "RegisterPricesSegue", sender: self)
                                }
                                
                            }
                        }
                        else if(json.code==201){
                            self.showToast(message:NSLocalizedString("Please retry again",comment: ""))
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
    
    
    override func viewDidLoad() {
        frontSelectImage.viewCotroller=self
        sideSelectImage.viewCotroller=self
        backSelectImage.viewCotroller=self
        super.viewDidLoad()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        refreshUser()
    }
    
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        if let image = info[.originalImage] as? UIImage {
            showToast(message:NSLocalizedString("Uploading image",comment:""))
            dismiss(animated: false, completion: nil)
            if picker == frontSelectImage.imagePicker {
                frontSelectImage.upload(image,managedContext)
            }
            else if picker == sideSelectImage.imagePicker {
                sideSelectImage.upload(image,managedContext)
            }
            else if picker == backSelectImage.imagePicker {
                backSelectImage.upload(image,managedContext)
            }
        }
    }
    
    override func loaded(){
        super.loaded()
        
        typesStackView=UIStackView()
        typesStackView.translatesAutoresizingMaskIntoConstraints = false
        typesStackView.axis = .horizontal
        typesStackView.spacing = 5
        typesStackView.distribution = .fill
        typesStackView.alignment = .fill
        
        for v in Constants.vehicles{
            let b=BtnImage()
            b.textView.text=v.name
            b.setImage(v.image, managedContext)
            b.type=v.type
            let gesture = UITapGestureRecognizer(target: self, action:  #selector(selectType))
            b.addGestureRecognizer(gesture)
            typesStackView.addArrangedSubview(b)
            if appDelegate.user.carType == b.type {
                b.setSelect(selected: true)
            }
        }
        typeScrollView.contentSize=CGSize(width: Constants.vehicles.count*105, height: 120)
        typeScrollView.addSubview(typesStackView)
        
        
        NSLayoutConstraint.activate([
            typesStackView.topAnchor.constraint(equalTo: typeScrollView.topAnchor),
            typesStackView.leadingAnchor.constraint(equalTo: typeScrollView.leadingAnchor),
            typesStackView.bottomAnchor.constraint(equalTo: typeScrollView.bottomAnchor),
            typesStackView.trailingAnchor.constraint(equalTo: typeScrollView.trailingAnchor)
        ])
        
        manufacturerTextField.textField.text=appDelegate.user.carManufacturer
        modelTextField.textField.text=appDelegate.user.carModel
        madYearTextField.textField.text=appDelegate.user.carMadeYear
        plateNumberTextField.textField.text=appDelegate.user.carNumber
        if appDelegate.user.images != nil{
            frontSelectImage.setImage(appDelegate.user.images.frontImageSmall, managedContext)
            sideSelectImage.setImage(appDelegate.user.images.sideImageSmall, managedContext)
            backSelectImage.setImage(appDelegate.user.images.backImage, managedContext)
        }
    }
    
    
    @objc func selectType(sender : UITapGestureRecognizer) {
        print("func selectType");
        for v in typesStackView.subviews{
            if let b = v as? BtnImage {
                b.setSelect(selected:false)
            }
        }
        if let b = sender.view as? BtnImage {
            b.setSelect(selected:true)
            typeErrLabel.isHidden=true
            print("func selectType "+b.type);
        }
    }
    
    
    
    
    private func validate()->Bool{
        var type:String!
        for v in typesStackView.subviews{
            if let b = v as? BtnImage,b.selected {
                type=b.type
            }
        }
        
        if type == nil {
            typeErrLabel.isHidden=false
            return false
        }
        
        if manufacturerTextField.isValid() && modelTextField.isValid() && madYearTextField.isValid() && plateNumberTextField.isValid() && frontSelectImage.isValid() && sideSelectImage.isValid() && backSelectImage.isValid(){
            return true
        }
        else{
            return false
        }
    }
    
    
    
}

