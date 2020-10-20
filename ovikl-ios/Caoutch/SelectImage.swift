import Foundation
import UIKit
import CoreData
import Alamofire
class SelectImage:UIView{
    
    var small:String!
    var large:String!
    
    var regex:NSRegularExpression!
    
    var imagePicker = UIImagePickerController()
    var viewCotroller:UIViewController2?
    var constraint100:NSLayoutConstraint!
    var constraint30:NSLayoutConstraint!
    var constraint60:NSLayoutConstraint!
    var appDelegate:AppDelegate!
  
    
    @IBInspectable var selectText:String=""{
        didSet{
            selectButton.setTitle(NSLocalizedString(selectText,comment: ""), for: UIControl.State.normal)
        }
    }
    
    @IBInspectable var deleteText:String=""{
        didSet{
            deleteButton.setTitle(NSLocalizedString(deleteText,comment: ""), for: UIControl.State.normal)
        }
    }
    
    @IBInspectable var errorText:String=""{
        didSet{
            errorView.text = NSLocalizedString(errorText,comment: "")
        }
    }
    
    @IBInspectable var required:Bool=false
    
    
    
    
    
    lazy var imageView: UIImageView = {
        let l = UIImageView()
        l.isHidden=true
        return l
    }()
    
    lazy var selectButton: UIButton = {
        let l = UIButton()
        l.setTitleColor(Constants.colorPrimaryDark, for: UIControl.State.normal)
        let gesture = UITapGestureRecognizer(target: self, action: #selector(self.selectAction))
        l.addGestureRecognizer(gesture)
        
        l.layer.borderWidth=1
        l.layer.cornerRadius = 5
        l.layer.borderColor=Constants.colorPrimary.cgColor
        
        return l
    }()
    
    @objc func selectAction(sender:UITapGestureRecognizer){
        
        if UIImagePickerController.isSourceTypeAvailable(.savedPhotosAlbum){
            print("Button capture")
            
            imagePicker.delegate = viewCotroller as? UIImagePickerControllerDelegate & UINavigationControllerDelegate
            imagePicker.sourceType = .savedPhotosAlbum
            imagePicker.allowsEditing = false
            
            viewCotroller?.present(imagePicker, animated: true, completion: nil)
        }
    }
    
    lazy var deleteButton: UIButton = {
        let l = UIButton()
        l.setTitleColor(Constants.colorPrimaryDark, for: UIControl.State.normal)
        l.isHidden=true
        let gesture = UITapGestureRecognizer(target: self, action: #selector(self.deleteAction))
        l.addGestureRecognizer(gesture)
        
        l.layer.borderWidth=1
        l.layer.cornerRadius = 5
        l.layer.borderColor=Constants.colorPrimary.cgColor
        l.contentEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5)
        return l
    }()
    
    @objc func deleteAction(sender:UITapGestureRecognizer){
        reset()
    }
    
    
    lazy var errorView: UILabel = {
        let t = UILabel()
        t.backgroundColor=Constants.white
        t.textAlignment = .center
        t.textColor=Constants.red
        t.isHidden=true
        return t
    }()
    
    lazy var loading: UIActivityIndicatorView = {
        let l = UIActivityIndicatorView()
        //l.startAnimating();
        return l
    }()
    
    
    
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupView()
    }
    
    private func setupView() {
        appDelegate = UIApplication.shared.delegate as! AppDelegate
        self.backgroundColor = Constants.white
        addSubview(selectButton)
        addSubview(loading)
        addSubview(imageView)
        addSubview(deleteButton)
        addSubview(errorView)
        setupLayout()
        
        constraint100 = self.heightAnchor.constraint(equalToConstant: 100)
        constraint30 = self.heightAnchor.constraint(equalToConstant: 30)
        constraint60 = self.heightAnchor.constraint(equalToConstant: 60)
        
        reset()
    }
    
    
    private func setupLayout() {
        
           
        
        
    }
    
    
    
    override class var requiresConstraintBasedLayout: Bool {
        return true
    }
    
    
    
    func reset(){
       
        imageView.isHidden=true
        selectButton.isHidden=false
        deleteButton.isHidden=true
        loading.stopAnimating()
        errorView.isHidden=true
        small=nil
        large=nil
       
        selectButton.frame=CGRect(x: self.frame.width/2-100, y: 0, width: 200, height: 30)
        NSLayoutConstraint.deactivate([constraint60])
        NSLayoutConstraint.activate([constraint30])
        NSLayoutConstraint.deactivate([constraint100])
    }
    
    
    func showError(){
       
        imageView.isHidden=true
        selectButton.isHidden=false
        deleteButton.isHidden=true
        loading.stopAnimating()
        errorView.isHidden=false
        small=nil
        large=nil
       
        selectButton.frame=CGRect(x: self.frame.width/2-100, y: 0, width: 200, height: 30)
        errorView.frame=CGRect(x: 0, y: 35, width: self.frame.width, height: 30)
        NSLayoutConstraint.activate([constraint60])
        NSLayoutConstraint.deactivate([constraint30])
        NSLayoutConstraint.deactivate([constraint100])
    }
    
    
    func uploading(){
        loading.startAnimating()
        imageView.isHidden=true
        selectButton.isHidden=true
        deleteButton.isHidden=false
        errorView.isHidden=true
        
        loading.frame=CGRect(x: self.frame.width/2-150, y: 0, width: 100, height: 100)
        deleteButton.frame=CGRect(x: self.frame.width/2-150+110, y: 30, width: 200, height: 30)
        NSLayoutConstraint.deactivate([constraint60])
        NSLayoutConstraint.deactivate([constraint30])
        NSLayoutConstraint.activate([constraint100])
        
    }
    
    func uploaded(){
        
        loading.stopAnimating()
        imageView.isHidden=false
        selectButton.isHidden=true
        deleteButton.isHidden=false
        errorView.isHidden=true
        
        imageView.frame=CGRect(x: self.frame.width/2-150, y: 0, width: 100, height: 100)
        deleteButton.frame=CGRect(x: self.frame.width/2-150+110, y: 30, width: 200, height: 30)
        NSLayoutConstraint.deactivate([constraint60])
        NSLayoutConstraint.deactivate([constraint30])
        NSLayoutConstraint.activate([constraint100])
    }
    
    func isValid()->Bool{
        
        if required && small == nil{
            
            showError()
            return false
        }
        else{
            //reset()
            return true
        }
        
    }
    
    
    
    
    func upload(_ image:UIImage,_ managedContext:NSManagedObjectContext){
        
        var image0: UIImage?=image
        var image1: UIImage?=image
        let size = image.size
        var ratio:CGFloat = 1.0
        
        let largeImage=CGFloat(appDelegate.config.largeImage)
        let smallImage=CGFloat(appDelegate.config.smallImage)
        if size.width > largeImage || size.height > largeImage{
            
            if size.width >= size.height{
                ratio = largeImage / size.width
            }
            else{
                ratio = largeImage / size.height
            }
            
            
            let newSize0: CGSize = CGSize(width:size.width * ratio, height:size.height * ratio)
            let rect0 = CGRect(x:0, y:0, width:newSize0.width, height:newSize0.height)
            UIGraphicsBeginImageContextWithOptions(newSize0, false, 1.0)
            image.draw(in: rect0)
            image0 = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            ratio=ratio*smallImage/largeImage
            let newSize1: CGSize = CGSize(width:size.width * ratio, height:size.height * ratio)
            let rect1 = CGRect(x:0, y:0, width:newSize1.width, height:newSize1.height)
            UIGraphicsBeginImageContextWithOptions(newSize1, false, 1.0)
            image.draw(in: rect1)
            image1 = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            
        }
        if let data0 = image0?.jpegData(compressionQuality: 1.0),let data1 = image1?.jpegData(compressionQuality: 1.0){
            uploading()
            //imageView.image = image
            let url = Constants.indexUrl+"/upload_image"
            let parameters: Parameters = [
                "userId": appDelegate.user._id as String
            ]
            let headers: HTTPHeaders = [
                /* "Authorization": "your_access_token",  in case you need authorization header */
                "Content-type": "multipart/form-data"
            ]
            
            Alamofire.upload(multipartFormData: { (multipartFormData) in
                for (key, value) in parameters {
                    multipartFormData.append("\(value)".data(using: String.Encoding.utf8)!, withName: key as String)
                }
                multipartFormData.append(data0, withName: "image", fileName: "profile.jpg", mimeType: "image/jpeg")
                multipartFormData.append(data1, withName: "image", fileName: "profile.jpg", mimeType: "image/jpeg")
            }, usingThreshold: UInt64.init(), to: url, method: .post, headers: headers) { (result) in
                switch result{
                case .success(let upload, _, _):
                    
                    upload.responseJSON { response in
                        if let json = response.result.value as? [String: Any]{
                            print("JSON: \(json)");
                            if let code=json["code"] as? Double ,code==200,  let messageArr=json["message"] as? [String:Any] ,
                                let imageStr0=messageArr["image0"] as? String,
                                let imageStr1=messageArr["image1"] as? String{
                                do{
                                    let imageEntity = NSEntityDescription.entity(forEntityName: "Image", in: managedContext)!
                                    let imageRecord0:NSManagedObject = NSManagedObject(entity: imageEntity, insertInto: managedContext);
                                    imageRecord0.setValue(imageStr0, forKey: "id")
                                    imageRecord0.setValue(data0, forKey: "image")
                                    imageRecord0.setValue(Date(), forKey: "createTime")
                                    self.large=imageStr0
                                    let imageRecord1:NSManagedObject = NSManagedObject(entity: imageEntity, insertInto: managedContext);
                                    imageRecord1.setValue(imageStr1, forKey: "id")
                                    imageRecord1.setValue(data1, forKey: "image")
                                    imageRecord1.setValue(Date(), forKey: "createTime")
                                    self.small=imageStr1
                                    self.imageView.image=image1
                                    try managedContext.save()
                                    self.uploaded()
                                } catch {
                                    print(error.localizedDescription)
                                    self.reset()
                                }
                            }
                            else{
                                self.reset()
                            }
                        }
                        else{
                            self.reset()
                        }
                    }
                case .failure(let error):
                    print("Error in upload: \(error.localizedDescription)")
                    //onError(error)
                    self.reset()
                }
            }
        }
        else{
            self.reset()
        }
    }
    
    
    func setImage(_ urlString:String!,_ managedContext:NSManagedObjectContext){
        if urlString == nil{
            reset()
            return
        }
        uploading()
        do{
            let url = URL(string: Constants.url+urlString)
            //let data = try Data(contentsOf: url!)
            //imageView.image = UIImage(data: data)
            
            let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Image")
            request.predicate = NSPredicate(format: "id = %@",
                                            argumentArray: [urlString])
            request.fetchLimit=1
            request.returnsObjectsAsFaults = false
            
            let result = try managedContext.fetch(request)
            if result.count==0{
                
                DispatchQueue.global().async { [weak self] in
                    if let data = try? Data(contentsOf: url!) {
                        if let image = UIImage(data: data) {
                            DispatchQueue.main.async {
                                self?.imageView.image = image
                                self?.uploaded()
                                self?.small=urlString
                                let imageEntity = NSEntityDescription.entity(forEntityName: "Image", in: managedContext)!
                                let imageRecord:NSManagedObject = NSManagedObject(entity: imageEntity, insertInto: managedContext);
                                imageRecord.setValue(urlString, forKey: "id")
                                imageRecord.setValue(image.pngData(), forKey: "image")
                                imageRecord.setValue(Date(), forKey: "createTime")
                                print(urlString+" set in cache")
                                
                                do {
                                    try managedContext.save()
                                }
                                catch let error as NSError {
                                    print("Could not save. \(error), \(error.userInfo)")
                                }
                            }
                        }
                    }
                }
            }
            else{
                print(urlString+" get from cache")
                for data in result as! [NSManagedObject] {
                    if let i=data.value(forKey: "image") as? Data{
                        self.imageView.image = UIImage(data: i)
                        self.uploaded()
                        self.small=urlString
                    }
                }
                
                
            }
        }
        catch{
            
        }
        
    }
    
}



