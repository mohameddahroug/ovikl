import Foundation
import UIKit
import CoreData
class BtnImage:UIView{
    
    var regex:NSRegularExpression!
    var selected:Bool=false
    var type:String!
    
    @IBInspectable var titleText:String=""{
        didSet{
            textView.text = "  "+NSLocalizedString(titleText,comment: "");
        }
    }
    @IBInspectable var src:String=""{
        didSet{
            //textField.placeholder = placeHolder
            if(src != ""){
                imageView.image=UIImage(named: src);
                imageView.isHidden=false
                loading.stopAnimating()
            }
        }
    }
    
    
 
    

    
    lazy var imageView: UIImageView = {
        let l = UIImageView(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
        l.isHidden=true
        return l
    }()
    
    
  
    

    lazy var textView: UILabel = {
        let t = UILabel(frame: CGRect(x: 0, y: 100, width: 100, height: 20))
        t.backgroundColor=Constants.white
        t.textAlignment = .center
        return t
    }()

    lazy var loading: UIActivityIndicatorView = {
        let l = UIActivityIndicatorView(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
        l.startAnimating();
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
        self.backgroundColor = Constants.white
      
      
        self.addSubview(imageView)
        self.addSubview(loading)
        self.addSubview(textView)
        
        
        setupLayout()
      
        
    }

    
    private func setupLayout() {
        NSLayoutConstraint.activate([
            self.heightAnchor.constraint(equalToConstant: 120),
            self.widthAnchor.constraint(equalToConstant: 100),
           
        ])
        
       
        
   
        
            
    }
    
    
 
    override class var requiresConstraintBasedLayout: Bool {
        return true
    }
    
    func setSelect(selected:Bool){
        self.selected=selected
        if(selected){
            imageView.layer.borderWidth=1
            imageView.layer.cornerRadius = 5
            imageView.layer.borderColor=Constants.colorPrimary.cgColor
        }
        if(!selected){
            imageView.layer.borderWidth=1
            imageView.layer.cornerRadius = 5
            imageView.layer.borderColor=Constants.white.cgColor
        }
    }
    
    func setImage(_ urlString:String,_ managedContext:NSManagedObjectContext){
        imageView.isHidden=true
        loading.startAnimating()
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
                                self?.imageView.isHidden=false
                                self?.loading.stopAnimating()
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
                        self.imageView.isHidden=false
                        self.loading.stopAnimating()
                    }
                }
                
                
            }
        }
        catch{
            
        }
        
    }
    
    
    
    
  
    

}
