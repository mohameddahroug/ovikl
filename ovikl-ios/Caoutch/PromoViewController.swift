
import UIKit
import Foundation

class PromoViewController: UIViewController{
    var delegate: PromoViewDelegate?
    var code:String!
    
    
    @IBOutlet weak var alertView: UIView!
    @IBOutlet weak var promoEditText: UITextField!
    
    @IBAction func ok(_ sender: UIButton) {
        if let text=promoEditText.text , text.count>0 {
            delegate?.okButtonTapped(promo:text)
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    @IBAction func cancel(_ sender: UIButton) {
        
        self.dismiss(animated: true, completion: nil)
    }
    
    override func viewDidLoad() {
        if let code=code{
            promoEditText.text=code;
        }
    }
    
    func setupView() {
        alertView.layer.cornerRadius = 15
        self.view.backgroundColor = UIColor.black.withAlphaComponent(0.4)
    }
    
    func animateView() {
        alertView.alpha = 0;
        self.alertView.frame.origin.y = self.alertView.frame.origin.y + 50
        UIView.animate(withDuration: 0.4, animations: { () -> Void in
            self.alertView.alpha = 1.0;
            self.alertView.frame.origin.y = self.alertView.frame.origin.y - 50
        })
    }
    
    let alertViewGrayColor = UIColor(red: 224.0/255.0, green: 224.0/255.0, blue: 224.0/255.0, alpha: 1)
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        setupView()
        animateView()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        view.layoutIfNeeded()
        
    }
    
}


protocol PromoViewDelegate: class {
    func okButtonTapped(promo:String?)
    
}
