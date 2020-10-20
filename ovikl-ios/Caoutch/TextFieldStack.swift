//
//  TextFieldStack.swift
//  Caoutch
//
//  Created by Mohamed Dahroug on 28/11/2019.
//  Copyright © 2019 Caoutch. All rights reserved.
//

import Foundation
import UIKit
class TextFieldStack:UIView{
    
    var regex:NSRegularExpression!

    
    @IBInspectable var titleText:String=""{
        didSet{
            title.text = "  "+NSLocalizedString(titleText,comment: "")
        }
    }
    @IBInspectable var placeHolder:String=""{
        didSet{
            textField.placeholder = NSLocalizedString(placeHolder,comment: "")
        }
    }
    @IBInspectable var errorText:String=""{
        didSet{
            error.text=NSLocalizedString(errorText,comment: "")
        }
    }
    
    @IBInspectable var regularExperssion:String=""{
        didSet{
            if regularExperssion != "" {
                regex = try! NSRegularExpression(pattern:"^"+regularExperssion+"$")
            }
        }
    }
    
    @IBInspectable var required:Bool=false{
           didSet{
           }
       }
    
    @IBInspectable var type:String=""{
        didSet{
            //textField.keyboardType=UIKeyboardType.init(rawValue: keyboardType) ?? UIKeyboardType.alphabet
            if type == ""{
                
            }
            else if type == "email"{
                textField.keyboardType=UIKeyboardType.emailAddress
                textField.autocapitalizationType = .none
                
            }
            else if type == "password"{
                textField.keyboardType=UIKeyboardType.alphabet
                textField.isSecureTextEntry=true
                textField.autocapitalizationType = .none
                showButton.isHidden=false
                
            }
            else if type == "phone"{
                textField.keyboardType=UIKeyboardType.phonePad
                
            }
            else if type == "name"{
                textField.keyboardType=UIKeyboardType.alphabet
                
            }
            else if type == "number"{
                textField.keyboardType=UIKeyboardType.numberPad
                
            }
            else if type == "decimal"{
                textField.keyboardType=UIKeyboardType.decimalPad
                
            }
            else if type == "nocap"{
                textField.autocapitalizationType = .none
                
            }
            
        }
    }
    
    lazy var stack: UIStackView = {
          let t = UIStackView()

          t.translatesAutoresizingMaskIntoConstraints = false
          t.axis = .vertical
          t.spacing = 2
          t.distribution = .fill
          t.alignment = .fill

          return t
      }()
    
    lazy var stack2: UIStackView = {
        let t = UIStackView()

        t.translatesAutoresizingMaskIntoConstraints = false
        t.axis = .horizontal
        t.spacing = 5
        t.distribution = .fill
        t.alignment = .fill

        return t
    }()
    
    lazy var title: UILabel = {
        let l = UILabel()
        l.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        //l.text = titleText
        //l.textAlignment = .center
        l.translatesAutoresizingMaskIntoConstraints = false
        l.textColor = Constants.primaryTextColor
        return l
    }()
    
    lazy var showButton: UIButton = {
        let b = UIButton()
        b.setTitle(NSLocalizedString("Show", comment: ""), for: .normal)
        b.setTitleColor( Constants.primaryTextColor, for: .normal)
        b.titleLabel?.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        b.isHidden=true
        b.setContentHuggingPriority(UILayoutPriority.defaultHigh, for: NSLayoutConstraint.Axis.horizontal)
        let gesture1 = UITapGestureRecognizer(target: self, action:  #selector(showButtonTapped))
        b.addGestureRecognizer(gesture1)
        return b
    }()
    
    lazy var hideButton: UIButton = {
        let b = UIButton()
        b.setTitle(NSLocalizedString("Hide", comment: ""), for: .normal)
        b.setTitleColor( Constants.primaryTextColor, for: .normal)
        b.titleLabel?.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        b.isHidden=true
        b.setContentHuggingPriority(UILayoutPriority.defaultHigh, for: NSLayoutConstraint.Axis.horizontal)
        let gesture1 = UITapGestureRecognizer(target: self, action:  #selector(hideButtonTapped))
        b.addGestureRecognizer(gesture1)
        return b
    }()
  
    

    lazy var textField: UITextField = {
        let t = UITextField()
        t.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        //t.placeholder = placeHolder
        //t.textAlignment = .center
        t.translatesAutoresizingMaskIntoConstraints = false
        t.borderStyle = UITextField.BorderStyle.roundedRect
        t.backgroundColor = Constants.white
        
        return t
    }()

    lazy var error: UILabel = {
        let l = UILabel()
        l.font = UIFont.systemFont(ofSize: 14, weight: .regular)
        //l.text = errorText
        l.textAlignment = .center
        l.translatesAutoresizingMaskIntoConstraints = false
        l.textColor=Constants.red
        l.isHidden=true
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
        stack.addArrangedSubview(title)
        stack2.addArrangedSubview(textField)
        stack2.addArrangedSubview(showButton)
        stack2.addArrangedSubview(hideButton)
        stack.addArrangedSubview(stack2)
        stack.addArrangedSubview(error)
        addSubview(stack)
        setupLayout()
        textField.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
    }

    private func setupLayout() {
     
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo: self.topAnchor),
            stack.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: self.trailingAnchor),
            stack.bottomAnchor.constraint(equalTo: self.bottomAnchor)
        ])
        
       
        
   
        
            
    }
    
    func text()->String!{
        return textField.text
    }
    
    func isValid()->Bool{
        
        if regex != nil{
            if let text = textField.text{
                
                let range = NSRange(location: 0, length: text.utf16.count)
                if regex.numberOfMatches(in: text, options: [], range: range) > 0 || (!required && text.count==0){
                    if type == "number" || type == "phone" {
                        var str = text
                        let map = ["٠": "0",
                                   "١": "1",
                                   "٢": "2",
                                   "٣": "3",
                                   "٤": "4",
                                   "٥": "5",
                                   "٦": "6",
                                   "٧": "7",
                                   "٨": "8",
                                   "٩": "9",
                                   ",": "."]
                        map.forEach { str = str.replacingOccurrences(of: $0, with: $1) }
                        textField.text=str
                    }
                    print("valid "+text)
                    error.isHidden=true
                }
                else{
                    print("invalid "+text)
                    error.isHidden=false
                    return false
                }
            }
        }
        return true
    }
    
    func match(string:String)->Bool{
        if error.isHidden && string != textField.text{
            error.isHidden=false
            return false
        }
        return true;
    }
    
    
    @objc func textFieldDidChange(_ textField:UITextField){
        if regex != nil{
            if let text = textField.text{
                if !required && text.count==0{
                    error.isHidden=true
                }
                else{
                    let range = NSRange(location: 0, length: text.utf16.count)
                    if regex.numberOfMatches(in: text, options: [], range: range) > 0{
                        //print("valid "+text)
                        error.isHidden=true
                    }
                    else{
                        //print("invalid "+text)
                        error.isHidden=false
                    }
                }
                
            }
        }
    }
    
    
    @objc func showButtonTapped() {
        print("func TextFieldStack buttonTapped")
        textField.keyboardType=UIKeyboardType.alphabet
        textField.isSecureTextEntry=false
        textField.autocapitalizationType = .none
        showButton.isHidden=true
        hideButton.isHidden=false
    }
    
    @objc func hideButtonTapped() {
        print("func TextFieldStack buttonTapped")
        textField.keyboardType=UIKeyboardType.alphabet
        textField.isSecureTextEntry=true
        textField.autocapitalizationType = .none
        showButton.isHidden=false
        hideButton.isHidden=true
    }

    //custom views should override this to return true if
    //they cannot layout correctly using autoresizing.
    //from apple docs https://developer.apple.com/documentation/uikit/uiview/1622549-requiresconstraintbasedlayout
    override class var requiresConstraintBasedLayout: Bool {
        return true
    }

}
