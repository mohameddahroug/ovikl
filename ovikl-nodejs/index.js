var constant = require('./constant');
var express   =    require("express");
var app       =    express();
const crypto = require('crypto');
//var connect = require('connect');
//var moment = require('moment');
//var router      =   express.Router();
/*var fs = require('fs');
var https = require('https');
var privateKey  = fs.readFileSync('/home/ubuntu/caoutch/ssl/key.pem', 'utf8');
var certificate = fs.readFileSync('/home/ubuntu/caoutch/ssl/certificate.pem', 'utf8');
var credentials = {key: privateKey, cert: certificate};*/
var port = process.argv[2];
var prod=process.argv[3];
if(prod==undefined||prod!="dev"){
    console.log('prod');
    prod=true;
}
else{
    console.log('dev');
    prod=false;
}
var redis = require("redis");
var client = redis.createClient();
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/transnet";
var urlTaxifare = "mongodb://localhost:27017/taxifare";
var ObjectId = require('mongodb').ObjectID;
//var database="transnet";

var bodyParser = require('body-parser')
var urlencodedParser=bodyParser.urlencoded({ extended: false });

var multipart = require('connect-multiparty');
//var uploadPath='/home/ubuntu/admin/images';
//var uploadPath='/Users/mohameddahroug/Desktop/projects/images';
if(!prod){
	constant.uploadPath= '../ovikl-html/images';
}
var multipartMiddleware = multipart({ uploadDir: constant.uploadPath});

//console.log(constant.uploadPath);
var nodemailer = require('nodemailer');
var nodemailer2 = require('nodemailer');
var transporter = nodemailer.createTransport({
	port: 25,
    host: 'localhost',
    tls: {
      rejectUnauthorized: false
    },
  });

var gmailTransport = nodemailer.createTransport({
        service: "gmail",
        host: 'smtp.gmail.com',
        auth: {
            user: "",
            pass: ""
        }
    });
//var io = require('socket.io-emitter')({ host: '127.0.0.1', port: 6379 });

// Configuration
// if(!prod){
//     app.use(express.static('../ovikl-html'));
// }
// app.use(connect.cookieParser());
// app.use(connect.logger('dev'));
 //app.use(connect.bodyParser());
// app.use(connect.json());
// app.use(connect.urlencoded());

//require("./location")(router,pool);



var config={
    minAndroidVersion:constant.minAndroidVersion,
    minIOSVersion:constant.minIOSVersion,
    timeout:constant.timeout,
    //timeoutLong:constant.timeoutLong,
    interval:constant.interval,
    fastInterval:constant.fastInterval,
    //intervalLong:constant.intervalLong,
    //fastIntervalLong:constant.fastIntervalLong,
    smallImage:constant.smallImage,
    largeImage:constant.largeImage
};

/*var vehicles=[
	{"type":"scooter","name":"Scooter","image":"/vehicles/scooter.png"},
	{"type":"limousine","name":"Limousine car","image":"/vehicles/limousine.png"},
	{"type":"taxi","name":"Taxi","image":"/vehicles/taxi.png"},
	{"type":"bus","name":"Bus","image":"/vehicles/bus.png"},
	{"type":"truck","name":"Truck","image":"/vehicles/truck.png"},
	{"type":"tuktuk","name":"Tuktuk","image":"/vehicles/tuktuk.png"},
	{"type":"pickup","name":"Pickup","image":"/vehicles/pickup.png"},
	{"type":"bike","name":"Bike","image":"/vehicles/bike.png"}
];*/
var vehicles=[
	{type:"scooter",name:"Scooter",image:"/vehicles/scooter.png",pointer:"/vehicles/scooter_pointer.png",selectedPointer:"/vehicles/scooter_pointer_selected.png"},
	{type:"limousine",name:"Limousine car",image:"/vehicles/limousine.png",pointer:"/vehicles/limousine_pointer.png",selectedPointer:"/vehicles/limousine_pointer_selected.png"},
	{type:"taxi",name:"Taxi",image:"/vehicles/taxi.png",pointer:"/vehicles/taxi_pointer.png",selectedPointer:"/vehicles/taxi_pointer_selected.png"},
	{type:"bus",name:"Bus",image:"/vehicles/bus.png",pointer:"/vehicles/bus_pointer.png",selectedPointer:"/vehicles/bus_pointer_selected.png"},
	{type:"truck",name:"Truck",image:"/vehicles/truck.png",pointer:"/vehicles/truck_pointer.png",selectedPointer:"/vehicles/truck_pointer_selected.png"},
	{type:"tuktuk",name:"Tuktuk",image:"/vehicles/tuktuk.png",pointer:"/vehicles/tuktuk_pointer.png",selectedPointer:"/vehicles/tuktuk_pointer_selected.png"},
	{type:"pickup",name:"Pickup",image:"/vehicles/pickup.png",pointer:"/vehicles/pickup_pointer.png",selectedPointer:"/vehicles/pickup_pointer_selected.png"},
	{type:"bike",name:"Bike",image:"/vehicles/bike.png",pointer:"/vehicles/bike_pointer.png",selectedPointer:"/vehicles/bike_pointer_selected.png"}
	];


app.post("/login",urlencodedParser,function(req,res){
	delete req.body[''];
	req.body.hashedKey = crypto.createHmac('md5', 'mongodb://localhost:27017/transnet')
                               .update(req.body.password)
							   .digest('hex');
	delete req.body.password;
    if(req.body.email==null||req.body.email==""){
		res.json({code:201,message : "",data:[]});
		console.log("/login",JSON.stringify(req.body),JSON.stringify({code:201,message : "",data:[]}));
	}
    else

        MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
            if (err)
                console.log("/login",JSON.stringify(req.body),err);
            else{
				var dbo = db.db();
				if(req.body.zone==null)
					req.body.zone="";

				if(req.body.zone==""){
					savelogin(req,res,dbo,db,null);
				}
				else{
					myquery = { "zone": req.body.zone};
					dbo.collection("zones").findOne(myquery,function(err, zone) {
						if (err){ 
							console.log("/login",err);
							db.close();
						}
						else{
							//console.log(result);
							if(zone==null)
							{
								res.json({"code":202});
								console.log("/login",JSON.stringify(req.body),JSON.stringify({"code":202}));
								db.close();
							}
							else{
								
								myquery = { "zone": req.body.zone,"email":req.body.email};
								dbo.collection("zone_user_block").findOne(myquery,function(err, result) {
									if (err){ 
										console.log("/login",err);
										db.close();
									}
									else{
										//console.log(result);
										if(result==null)
										{
											savelogin(req,res,dbo,db,zone);
										}
										else{
											
											res.json({"code":203,"zone":zone});
											console.log("/login",JSON.stringify(req.body),JSON.stringify({"code":203,"zone":zone}));
											db.close();
										}
									}
								});
							

							}
						}
					});
				}


                

            }

		});
		
		function savelogin(req,res,dbo,db,zone){
			var myquery = { email: req.body.email,hashedKey:req.body.hashedKey};

                req.body.updateTime= new Date();
                delete req.body["_id"];

				var newvalues = { $set: req.body };
				
				dbo.collection("user").findOne(myquery, function(err, result) {
                    if (err){
                        db.close();
						res.json({code:201,message : ""});
						console.log("/login",JSON.stringify(req.body),JSON.stringify({code:201,message : ""}),err);
                    }
                    else{
						//console.log(dbres);
						if(result != null){
							if(zone!=null)
								result.zoneContact=zone;
							
							if(result.zone!=req.body.zone&&(result.type=="driver"||result.type=="client")){
								dbo.collection("user").updateOne(myquery, newvalues,function(err, dbres) {db.close();});
								result.zone=req.body.zone;
								res.json({code:200,message : "",user:result});
								console.log("/login",JSON.stringify(req.body),JSON.stringify({code:200,message : "",user:result}));
							}
							else if(result.zone!=req.body.zone&&result.type=="admin"){
								res.json({"code":202});
								console.log("/login",JSON.stringify(req.body),JSON.stringify({"code":202}));
							}
							else{
								db.close();
								res.json({code:200,message : "",user:result});
								console.log("/login",JSON.stringify(req.body),JSON.stringify({code:200,message : "",user:result}));
							}
						}
						else{
							res.json({code:201});
							console.log("/login",JSON.stringify(req.body),JSON.stringify({code:201}));
							db.close();
						}
                        
                    }
				});
				

                
		}

});



app.post("/user",urlencodedParser,function(req,res){
	delete req.body['']; 
    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
        if (err)
            console.log("/user",err);
        else{
            var dbo = db.db();
			//var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
			var aggregation=[
				{
					$match:{
					"_id":	new ObjectId(req.body._id),hashedKey:req.body.hashedKey
					}
				},
				{
					$lookup:{
						from: 'zones',
						localField: 'zone',
						foreignField: 'zone',
						as: 'zoneContact'
					},
				},
				{
				$project:{
						type:1,createDate:1,
						carMadeYear:1,carManufacturer:1,carModel:1,carNumber:1,carType:1,
						'images.frontImageSmall':1,'images.sideImageSmall':1,'images.backImageSmall':1,
						cost:1,fcmToken:1,iosToken:1,clientStatus:1,driverStatus:1,mobile:1,
						driverRate:1,carRate:1,clientRate:1,zone:1,adminStatus:1,email:1,firstName:1,lastName:1,hashedKey:1,emailVerified:1,
						"zoneContact": { "$arrayElemAt": [ "$zoneContact", 0 ] }
					} 
				}
			];

            dbo.collection("user").aggregate(aggregation).toArray(function(err, result) {
                if (err){
                    console.log("/user",err);
					res.json({"code":201,"message" : ""});
					console.log("/user",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                    db.close();
                    db.close();
                }
                else{
                    //console.log(result);
                    if(result[0]!=null)
                    {	
						result=result[0];
						res.json({code:200,user:result,config:config,vehicles:vehicles});
						console.log("/user",JSON.stringify(req.body),JSON.stringify({code:200,user:result,config:config,vehicles:vehicles}));
                        db.close();
                    }
                    else{
						res.json({"code":201,"message" : ""});
						console.log("/user",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                        db.close();
                    }
                }
            });
        }
    });

});


app.post("/trip",urlencodedParser,function(req,res){
	delete req.body['']; 
	
	if(req.body.clientId!=null&&req.body.driverId!=null){
		res.json({"code":201,"message" : "","data":[]});
		console.log("/trip",JSON.stringify(req.body),JSON.stringify(({"code":201,"message" : "","data":[]})));
	}
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
		if (err) 
			console.log("/trip",err);
		else{
			var dbo = db.db();
			var myquery = { _id: new ObjectId(req.body._id),state: "FINISHED"};
			if(req.body.clientId!=null)
				myquery.clientId=req.body.clientId;

			if(req.body.driverId!=null)
				myquery.driverId=req.body.driverId;
			req.body.updateTime= new Date();
			delete req.body["_id"]; 
			
			var newvalues = { $set: req.body };
			dbo.collection("trip").updateOne(myquery, newvalues,function(err, dbres) {
				if (err){ 
					console.log("/trip",err);
					db.close();
				}
				else{
					//console.log(dbres);
					res.json({"code":200,"message" : "","data":[]});
					console.log("/trip",JSON.stringify(req.body),JSON.stringify({"code":200,"message" : "","data":[]}));
					db.close();
				}
			});
			
		}
				
	});	
});

     



app.post("/tcp_server",urlencodedParser,function(req,res){
	delete req.body[''];
	var forwardedIpsStr = req.header('x-forwarded-for');
	var IP = '';
	if (forwardedIpsStr) {
		IP = forwardedIpsStr.split(',')[0];
	}
	
	var lastNumber = IP.substr(IP.length - 1);
	//console.log("recieve sever from ip "+req.ip );
	//if(lastNumber=="0"||lastNumber=="2"||lastNumber=="4"||lastNumber=="6"||lastNumber=="8"){
	if(prod){
		res.json({"code":200,"server" : {"ip":"ovikl.com","port":3000}});
		console.log("/tcp_server",IP,JSON.stringify(req.body),JSON.stringify({"code":200,"server" : {"ip":"ovikl.com","port":3000}}));
	}
	else{
		if(req.body.ver!=null && req.body.ver.substr(0,1)=="A"){
			res.json({"code":200,"server" : {"ip":"10.0.2.2","port":3000}});
			console.log("/tcp_server",IP,JSON.stringify(req.body),JSON.stringify({"code":200,"server" : {"ip":"10.0.2.2","port":3000}}));
		}
		else{
			res.json({"code":200,"server" : {"ip":"localhost","port":3000}});	
			console.log("/tcp_server",IP,JSON.stringify(req.body),JSON.stringify({"code":200,"server" : {"ip":"localhost","port":3000}}));	
		}
	}
	/*	console.log("recieve sever load balancer 100 from "+IP);
	}
	else{ //if(lastNumber=="1"||lastNumber=="3"||lastNumber=="5"||lastNumber=="7"||lastNumber=="9"){
		res.json({"code":200,"message" : "https://server101.caoutch.com","data":[],maxWidth:700,scrollRefresh:2000,messagesRefresh:3});
		console.log("recieve sever load balancer 101 from "+IP );
	}*/

});


app.post("/support_message",urlencodedParser,function(req,res){
	delete req.body['']; 
	
	if(req.body.userId==null||req.body.message==null){
		res.json({"code":201,"message" : "","data":[]});
		console.log("/support_message",JSON.stringify(req.body),({"code":201,"message" : "","data":[]}));
	}
	else{
		MongoClient.connect(url,{ useNewUrlParser: true ,useUnifiedTopology: true }, function(err, db) {
			if (err) 
				console.log("/support_message",JSON.stringify(req.body),err);
			else{
				var dbo = db.db();
				req.body.createTime= new Date();
				req.body.sender=true;
				dbo.collection("support_message").insertOne( req.body, function(err, dbres) {
					if (err)
						console.log("/support_message",JSON.stringify(req.body),err);
					else{
						//console.log(dbres);
						res.json({"code":200,"newMessage" : req.body,"data":[]});
						console.log("/support_message",JSON.stringify(req.body),JSON.stringify({"code":200,"newMessage" : req.body,"data":[]}));
					}
					db.close();
				});
				
			}   
		});	
	}
});   



app.post("/get_support_message",urlencodedParser,function(req,res){
	
	if(req.body.userId==null){
		res.json({"code":201,"message" : "","data":[]});
		console.log("/get_support_message",JSON.stringify(req.body));
	}
	/*else if((req.body.lastMessageId==null||req.body.lastMessageId=="")&&req.body.type!=null)  {                                
		var msg = {message:"",sender:false,userId:req.body.userId,createTime:new Date()}  
		if(req.body.type=="driver" && req.body.status!="active"){
			if(req.body.lang==null||req.body.lang=="ar"){
				msg.message="برجاء ارسال رخصه السياره والقياده";
			}else{
				msg.message="Driver and car licenses and ID"; 
			}
		}
		else{
			if(req.body.lang==null||req.body.lang=="ar"){
				msg.message="كيف يمكننا مساعدتك";
			}else{
				msg.message="We are here for help"; 
			}
		}
		MongoClient.connect(url,{ useNewUrlParser: true }, function(err, db) {
			if (err) 
				console.log(err);
			else{
				var dbo = db.db();
				dbo.collection("support_message").insertOne( msg, function(err, dbres) {
					if (err) 
						console.log(err);
					else{
						//console.log(dbres);
						res.json({"code":200,"message" : "","data":[],"messages":[msg]});
					}
				});
				db.close();
			}
		});
	}*/
	else    
	{
		MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
			if (err) 
				console.log("/get_support_message",JSON.stringify(req.body),err);
			else{
				var dbo = db.db();
				var myquery = {"userId": req.body.userId};
				if(req.body.lastMessageId!=null&&req.body.lastMessageId!=""){
					myquery._id={$gt:new ObjectId(req.body.lastMessageId)};
				}    
				if(req.body.type!=null){
					myquery.sender=false;
				}
				dbo.collection("support_message").find(myquery).sort({$natural:-1}).limit(50).toArray(function(err, result) {
					if (err)
						console.log("/get_support_message",JSON.stringify(req.body),err);
					else{
						//console.log(result);
						res.json({"code":200,"message" : "","data":[],"messages":result});
						console.log("/get_support_message",JSON.stringify(req.body),JSON.stringify({"code":200,"message" : "","data":[],"messages":result}));
					}
				});
				db.close();
			}	
		});	
	}
});



app.post('/upload',multipartMiddleware, function(req, res) {
    delete req.body['']; 
    //console.log("file post",req.body,req.files);
    
    var file = req.files.image;
    //console.log(file.name);
    //console.log(file.type);
    //console.log(file);

    MongoClient.connect(url,{ useNewUrlParser: true ,useUnifiedTopology: true }, function(err, db) {
		if (err)
			console.log("/upload",err);
		else{
			var dbo = db.db();
			req.body.createTime= new Date();
			req.body.sender=true;
			var message ={userId:req.body.userId,image:"/images/"+file.path.replace(uploadPath+"/",""),type:"image",createTime:new Date()};
			console.log("/upload", file.path.replace(uploadPath+"/",""));
			var newvalues = { $set: req.body };
			dbo.collection("support_message").insertOne( message, function(err, dbres) {
				if (err) 
					console.log("/upload",err);
				else{
					res.json({"code":200,"message" : message,"data":[]});
					console.log("/upload",JSON.stringify({"code":200,"message" : message,"data":[]}));
				}
			});
			db.close();
		}	
	});	
});

app.post("/register",urlencodedParser,function(req,res){
	delete req.body['']; 
	
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true }, function(err, db) {
		if (err) 
			console.log("/register",err);
		else{	
			var dbo = db.db();
			var myquery = { "email": req.body.email};
			dbo.collection("user").findOne(myquery,function(err, result) {
				if (err){ 
					console.log("/register",err);
					db.close();
				}
				else{
					//console.log(result);
					if(result!=null)
					{
						res.json({"code":201});
						console.log("/register",JSON.stringify(req.body),JSON.stringify({"code":201}));
						db.close();
					}
					else{
						if(req.body.zone == null || req.body.zone == ""){
							saveRegister(req,res,dbo,db,null);
						}
						else{
							myquery = { "zone": req.body.zone};
							dbo.collection("zones").findOne(myquery,function(err, zone) {
								if (err){ 
									console.log("/register",err);
									db.close();
								}
								else{
									//console.log(result);
									if(zone==null)
									{
										res.json({"code":202});
										console.log("/register",JSON.stringify(req.body),JSON.stringify({"code":202}));
										db.close();
									}
									else{
										myquery = { "zone": req.body.zone,"email":req.body.email};
										dbo.collection("zone_user_block").findOne(myquery,function(err, result) {
											if (err){ 
												console.log("/register",err);
												db.close();
											}
											else{
												//console.log(result);
												if(result==null)
												{
													saveRegister(req,res,dbo,db,zone);
												}
												else{
													res.json({"code":203,"zone":zone});
													console.log("/register",JSON.stringify(req.body),JSON.stringify({"code":203,"zone":zone}));
													db.close();
												}
											}
										});
									}
								}
							});
						}

					}
				}
			});
		}
	});
	function saveRegister(req,res,dbo,db,zone){
		req.body.emailVerified=false;
		if(req.body.type=="client")
			req.body.clientStatus="active";
		else
			req.body.driverStatus="pending";
		req.body.createDate= new Date();
		req.body.verifyKey= crypto.createHmac('md5', constant.secret)
									   .update(req.body.time)
									   .digest('hex');
		req.body.hashedKey = crypto.createHmac('md5', constant.secret)
									   .update(req.body.password)
									   .digest('hex');
		delete req.body.password;
		dbo.collection("user").insertOne( req.body, function(err, resDB) {
			if (err) {
				console.log("/register",err);
				db.close();
			}
			else{
				//console.log(resDB);
				req.body["_id"]=resDB.insertedId;
				if(zone!=null)
					req.body.zoneContact=zone;
				res.json({code:200,user:req.body});
				
				db.close();
				var text = "<html><body<p>Welcome to Ovikl</p><p>Open the below link to verify your email</p><p><a href=\"https://index.ovikl.com/verify_email?email="+req.body.email+"&verifyKey="+req.body.verifyKey+"\">https://index.ovikl.com/verify_email?email="+req.body.email+"&verifyKey="+req.body.verifyKey+"</a></p></body></html>";
				console.log("/register",text);
				var mailOptions = {
					from: 'support@ovikl.com',
					to: req.body.email,
					bcc: 'support@ovikl.com',
					subject: 'Verify email in Ovikl',
					html: text
				};
				console.log("/register",JSON.stringify(req.body),JSON.stringify({code:200}),text);
				transporter.sendMail(mailOptions, function(error, info){
					if (error) {
						console.log("/register",JSON.stringify(req.body),error);
					} else {
						//console.log("/register",'Email sent: ' + info.response);
					}
				});
			}
		});
	}
});


app.post("/register2",urlencodedParser,function(req,res){
	delete req.body[''];
	if(req.body.oldPassword!=null&&req.body.oldPassword!=""){
		var hashedKey = crypto.createHmac('md5', constant.secret)
										.update(req.body.oldPassword)
										.digest('hex');
		if(hashedKey!=req.body.hashedKey){
			res.json({code:204,message : "",data:[]});
			console.log("/register2",JSON.stringify(req.body),JSON.stringify({code:201,message : "",data:[]}));
			return;
		}
		
	}

    if(req.body._id==null||req.body._id==""){
		res.json({code:201,message : "",data:[]});
		console.log("/register2",JSON.stringify(req.body),JSON.stringify({code:201,message : "",data:[]}));
	}
	
    else{
	

        MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
            if (err)
                console.log("/register2",JSON.stringify(req.body),err);
            else{
				var dbo = db.db();
				if(req.body.zone==""||req.body.zone==null){
					saveRegister2(req,res,dbo,db,null);
				}
				else{
					myquery = { "zone": req.body.zone};
					dbo.collection("zones").findOne(myquery,function(err, zone) {
						if (err){ 
							console.log("/register2",err);
							db.close();
						}
						else{
							//console.log(result);
							if(zone==null)
							{
								res.json({"code":202});
								console.log("/register2",JSON.stringify(req.body),JSON.stringify({"code":202}));
								db.close();
							}
							else{
								
								myquery = { "zone": req.body.zone,"email":req.body.email};
								dbo.collection("zone_user_block").findOne(myquery,function(err, result) {
									if (err){ 
										console.log("/register2",err);
										db.close();
									}
									else{
										//console.log(result);
										if(result==null)
										{
											saveRegister2(req,res,dbo,db,zone);
										}
										else{
											
											res.json({"code":203,"zone":zone});
											console.log("/register2",JSON.stringify(req.body),JSON.stringify({"code":203,"zone":zone}));
											db.close();
										}
									}
								});
							

							}
						}
					});

				}
				
			}

		});
	}
		
	function saveRegister2(req,res,dbo,db,zone){
		var myquery = { _id: new ObjectId(req.body._id),hashedKey:req.body.hashedKey};
		if(req.body.newPassword!=null&&req.body.newPassword!=""){
			req.body.hashedKey=crypto.createHmac('md5', constant.secret)
			.update(req.body.newPassword)
			.digest('hex');
			delete req.body.oldPassword;
			delete req.body.newPassword;
		}

		req.body.updateTime= new Date();
		delete req.body["_id"];

		var newvalues = { $set: req.body };
		dbo.collection("user").findOneAndUpdate(myquery, newvalues,{returnOriginal: false},function(err, dbres) {
			if (err){
				db.close();
				res.json({code:201,message : ""});
				console.log("/register2",JSON.stringify(req.body),JSON.stringify({code:201,message : ""}),err);
			}
			else{
				//console.log(dbres);
				if(zone!=null)
					dbres.value.zoneContact=zone;
				res.json({code:200,message : "",user:dbres.value});
				console.log("/register2",JSON.stringify(req.body),JSON.stringify({code:200,message : "",user:dbres.value}));
				db.close();
			}
		});
	}

});

app.get("/verify_email",urlencodedParser,function(req,res){
	delete req.body[''];
	
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
		if (err)
			console.log("/verify_email",JSON.stringify(req.query),err);
		else{
			var dbo = db.db();
			var myquery = { "email": req.query.email,"verifyKey":req.query.verifyKey,"emailVerified":false};


            var newvalues = { $set: {"emailVerified":true}};
            dbo.collection("user").findOneAndUpdate(myquery, newvalues,{returnOriginal: false},function(err, dbres) {
                if (err){
                    console.log("/verify_email",JSON.stringify(req.query),err);
                    db.close();
                    res.send("Invalid data");
                }
                else if(dbres.value==null){
                    //console.log(dbres);
					res.send("Invalid data");
					console.log("/verify_email",JSON.stringify(req.query),"Invalid data");
                    db.close();
                }
                else{
                    //console.log(dbres);
                    res.send("Your email is verified");
					db.close();
					var text='<html><body><p>Your email is verified in Ovikl</p>';
					if(dbres.value.type=="driver"){
						text=text+"<p>Please complete your info from the application</p>";
					}
					text=text+"</body></html>";
					var mailOptions = {
						from: 'support@ovikl.com',
						to: dbres.value.email,
						bcc: 'support@ovikl.com',
						subject: 'Ovikl',
						html: text
					};
					console.log("/verify_email",JSON.stringify(req.query),"Your email is verified",text);
					transporter.sendMail(mailOptions, function(error, info){
						if (error) {
							console.log("/verify_email",JSON.stringify(req.query),error);
						} else {
							//console.log('Email sent: ' + info.response);
						}
					});
                }
            });
		}
	});
});


app.post("/resend",urlencodedParser,function(req,res){
	delete req.body[''];
	
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true }, function(err, db) {
		if (err){
			console.log("/resend",JSON.stringify(req.body),err);
		}
		else{
			var dbo = db.db();
			var myquery = { "email": req.body.email};
			dbo.collection("user").findOne(myquery,function(err, result) {
				if (err){
					console.log("/resend",JSON.stringify(req.body),err);
					db.close();
				}
				else{
					//console.log(result);
					if(result==null)
					{
						res.json({"code":201});
						console.log("/resend",JSON.stringify(req.body),JSON.stringify({"code":201}));
						db.close();
					}
					else{

                        res.json({code:200,user:req.body});
                        db.close();
var text = "<html><body<p>Welcome to Ovikl</p><p>Open the below link to verify your email</p><p><a href=\"https://index.ovikl.com/verify_email?email="+result.email+"&verifyKey="+result.verifyKey+"\">https://index.ovikl.com/verify_email?email="+result.email+"&verifyKey="+result.verifyKey+"</a></p></body></html>";

                        console.log("/resend",JSON.stringify(req.body),text);
                        //if(result.email.toLowerCase().indexOf('yahoo.com')>-1){
                            var mailOptions = {
                                from: 'support@ovikl.com',
								to: result.email,
								bcc: 'support@ovikl.com',
                                subject: 'Verify email in Ovikl',
                                html: text
                            };

                            gmailTransport.sendMail(mailOptions, function(error, info){
                                if (error) {
                                	console.log("/resend",JSON.stringify(req.body),error);
                                } else {
                                	//console.log('Email sent: ' + info.response);
                                }
                            });
                        /*}
                        else{
                            var mailOptions = {
                                from: 'support@ovikl.com',
                                to: req.body.email,
                                subject: 'Verify email in Ovikl',
                                text: text
                            };

                            transporter.sendMail(mailOptions, function(error, info){
                                if (error) {
                                console.log(error);
                                } else {
                                console.log('Email sent: ' + info.response);
                                }
                            });
                        }*/

					}
				}
			});
		}
	});
});


app.post('/upload_image',multipartMiddleware, function(req, res) {

    delete req.body[''];
    
	console.log('/upload_image');
    if (req.files.image.length!=2){
        res.json({code:201,message : ""});
    }
    else{
        res.json({"code":200,"message" : {image0:"/images/"+req.files.image[0].path.replace(constant.uploadPath+"/","")
			,image1:"/images/"+req.files.image[1].path.replace(constant.uploadPath+"/","")}});
		console.log('/upload_image',JSON.stringify(req.body),JSON.stringify({"code":200,"message" : {image0:"/images/"+req.files.image[0].path.replace(constant.uploadPath+"/","")
		,image1:"/images/"+req.files.image[1].path.replace(constant.uploadPath+"/","")}}));	
    }
});


/*app.post('/types',multipartMiddleware, function(req, res) {
	console.log('/types');
        res.json({"code":200,"vehicles":vehicles});

});*/


app.post("/generate_reset_key",urlencodedParser,function(req,res){
	delete req.body[''];
	//console.log("/generate_reset_key",JSON.stringify(req.body));
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
		if (err)
			console.log("/generate_reset_key",JSON.stringify(req.body),err);
		else{
			var dbo = db.db();
			var myquery = { "email": req.body.email};
            var x = Math.floor((Math.random()*1000000));
            if(x<100000)
                x=x+100000;
            var resetKey = x.toString();
            var newvalues = { $set: {"resetKey":resetKey}};
            dbo.collection("user").findOneAndUpdate(myquery, newvalues,{returnOriginal: false},function(err, dbres) {
                if (err){
                    console.log("/generate_reset_key",JSON.stringify(req.body),err);
                    db.close();
                    res.json({"code":201});
                }
                else if(dbres.value==null){
                    //console.log(dbres);
					res.json({"code":201});
					console.log("/generate_reset_key",JSON.stringify(req.body),JSON.stringify({"code":201}));
                    db.close();
                }
                else{
                    //console.log(dbres);
                    res.json({"code":200});
                    db.close();

                    var text = "<html><body><p>Welcome to Ovikl\r\nUse the below 6 digits to reset your password</p>"+resetKey+"</body><html>";
                    console.log("/generate_reset_key",JSON.stringify(req.body),JSON.stringify({"code":200}),text);
                    var mailOptions = {
                        from: 'support@ovikl.com',
						to: req.body.email,
						bcc: 'support@ovikl.com',
                        subject: 'Ovikl - Reset password verification key',
                        html: text
                    };

                    transporter.sendMail(mailOptions, function(error, info){
                        if (error) {
                        	console.log("/generate_reset_key",JSON.stringify(req.body),error);
                        } else {
                        	//console.log('Email sent: ' + info.response);
                        }
                    });
                }
            });
		}
	});
});


app.post("/reset_password",urlencodedParser,function(req,res){
	delete req.body[''];
	//console.log("/reset_password",JSON.stringify(req.body));
	if(req.body.resetKey!=null&&req.body.resetKey!=""){
        MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true }, function(err, db) {
            if (err)
                console.log("/reset_password",JSON.stringify(req.body),JSON.stringify({"code":201}),err);
            else{
                var dbo = db.db();
                req.body.hashedKey = crypto.createHmac('md5', constant.secret )
                                               .update(req.body.password)
                                               .digest('hex');
                var myquery = { "email": req.body.email,"resetKey":req.body.resetKey};

                var newvalues = { $set: {"hashedKey": req.body.hashedKey,"resetKey":""}};
                dbo.collection("user").findOneAndUpdate(myquery, newvalues,{returnOriginal: false},function(err, dbres) {
                    if (err){
                        //console.log("/reset_password",err);
                        db.close();
						res.json({"code":201});
						console.log("/reset_password",JSON.stringify(req.body),JSON.stringify({"code":201}),err);
                    }
                    else if(dbres.value==null){
                        //console.log(dbres);
						res.json({"code":201});
						console.log("/reset_password",JSON.stringify(req.body),JSON.stringify({"code":201}));
                        db.close();
                    }
                    else{
                        //console.log(dbres);
						res.json({"code":200,"user":dbres.value});
						console.log("/reset_password",JSON.stringify(req.body),JSON.stringify({"code":200,"user":dbres.value}));
                        db.close();


                    }
                });
            }
        });
	}
});

/*app.post("/config",urlencodedParser,function(req,res){
	console.log("/config",req.body.type);
    if(req.body.type=="client"){
        res.json({config:config,
            vehiclesImages:vehiclesImages});
    }
    else{
        res.json({config:config});
    }
});*/


  

app.post('/error',multipartMiddleware, function(req, res) {
	console.log('/error',req.body);
        
	res.json({"code":200});
});


app.post("/users",urlencodedParser,function(req,res){
	delete req.body['']; 
    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
        if (err)
            console.log("/users",err);
        else{
            var dbo = db.db();
            var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
            dbo.collection("user").findOne(myquery,function(err, result) {
                if (err){
                    console.log("/users",err);
					res.json({"code":201,"message" : ""});
					console.log("/users",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                    db.close();
                }
                else{
                    //console.log(result);
                    if(result!=null&&(result.type=="admin"||result.type=="super_admin"))
                    {	
						var myquery = {};
						if(result.zone!=null&&result.zone!=""){
							myquery.zone=result.zone;
						}
						if(req.body.last_user_id!=null&&req.body.last_user_id!=""){
							myquery._id={$lt:ObjectId(req.body.last_user_id)}
						}
						
						if(req.body.search!=null&&req.body.search!=""){
							myquery["$or"]=[{'firstName': {"$regex" : req.body.search, $options: 'i'}},
											{'lastName': {"$regex" : req.body.search, $options: 'i'}},
											{'mobile': {"$regex" : req.body.search, $options: 'i'}},
											{'firstName': {"$regex" : req.body.search, $options: 'i'}},
											{'lastName': {"$regex" : req.body.search, $options: 'i'}},
											{'mobile': {"$regex" : req.body.search, $options: 'i'}},
											{'type': {"$regex" : "^"+req.body.search+"$", $options: 'i'}},
											{'email': {"$regex" : req.body.search, $options: 'i'}}];
						}
						
						dbo.collection("user").find(myquery).sort({_id:-1}).limit(5).toArray(function(err, result) {
							if (err){
								console.log("/users",err);
								res.json({"code":201,"message" : ""});
								console.log("/users",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
								db.close();
							}
							else{
								res.json({code:200,users:result});
								console.log("/users",JSON.stringify(req.body),JSON.stringify({code:200,users:result}));
								db.close();
							}
						});
						
					}

                    else{
						res.json({"code":201,"message" : ""});
						console.log("/users",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                        db.close();
                    }
                }
            });
        }
    });

});

app.post("/trips",urlencodedParser,function(req,res){
	delete req.body['']; 
    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
        if (err)
            console.log("/trips",err);
        else{
            var dbo = db.db();
            var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
            dbo.collection("user").findOne(myquery,function(err, result) {
                if (err){
                    console.log("/trips",err);
					res.json({"code":201,"message" : ""});
					console.log("/trips",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                    db.close();
                }
                else{
                    //console.log(result);
                    if(result!=null&&(result.type=="admin"||result.type=="super_admin"))
                    {	
						var myquery = {};
						if(result.zone!=null&&result.zone!=""){
							myquery.zone=result.zone;
						}
						if(req.body.last_user_id!=null&&req.body.last_user_id!=""){
							myquery._id={$lt:ObjectId(req.body.last_user_id)}
						}
						
						if(req.body.search!=null&&req.body.search!=""){
							myquery["$or"]=[{'driver.firstName': {"$regex" : req.body.search, $options: 'i'}},
											{'driver.lastName': {"$regex" : req.body.search, $options: 'i'}},
											{'driver.mobile': {"$regex" : req.body.search, $options: 'i'}},
											{'driver.email': {"$regex" : req.body.search, $options: 'i'}},
											{'client.firstName': {"$regex" : req.body.search, $options: 'i'}},
											{'client.lastName': {"$regex" : req.body.search, $options: 'i'}},
											{'client.mobile': {"$regex" : req.body.search, $options: 'i'}},
											{'client.email': {"$regex" : req.body.search, $options: 'i'}}];
						}
						
						dbo.collection("trip").find(myquery).sort({_id:-1}).limit(2).toArray(function(err, result) {
							if (err){
								console.log("/trips",err);
								res.json({"code":201,"message" : ""});
								console.log("/trips",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
								db.close();
							}
							else{
								res.json({code:200,trips:result});
								console.log("/trips",JSON.stringify(req.body),JSON.stringify({code:200,trips:result}));
								db.close();
							}
						});
						
					}
                    else{
						res.json({"code":201,"message" : ""});
						console.log("/trips",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                        db.close();
                    }
                }
            });
        }
	});
	

});


app.post("/trip_locations",urlencodedParser,function(req,res){
	delete req.body['']; 
    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
        if (err)
            console.log("/trip_locations",err);
        else{
            var dbo = db.db();
            var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
            dbo.collection("user").findOne(myquery,function(err, result) {
                if (err){
                    console.log("/trip_locations",err);
					res.json({"code":201,"message" : ""});
					console.log("/trip_locations",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                    db.close();
                }
                else{
                    //console.log(result);
                    if(result!=null&&result.type=="driver"){
						myquery = { "_id": new ObjectId(req.body["trip_id"]),driverId:req.body["_id"]};	
						var newvalues = { $set: {locations:JSON.parse(req.body.locations)} };
						dbo.collection("trip").updateOne(myquery, newvalues,function(err, dbres) {
							if (err){ 
								console.log("/trip_locations",err);
								db.close();
							}
							else{
								//console.log(dbres);
								res.json({"code":200,"message" : "","data":[]});
								console.log("/trip_locations",JSON.stringify(req.body),JSON.stringify({"code":200,"message" : "","data":[]}));
								db.close();
							}
						});
						
                    }
                    else{
						res.json({"code":201,"message" : ""});
						console.log("/trip_locations",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                        db.close();
                    }
                }
            });
        }
    });

});

app.post("/block_zone_user",urlencodedParser,function(req,res){
	delete req.body['']; 
    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
        if (err)
            console.log("/block_zone_user",err);
        else{
            var dbo = db.db();
            var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
            dbo.collection("user").findOne(myquery,function(err, result) {
                if (err){
                    console.log("/block_zone_user",err);
					res.json({"code":201,"message" : ""});
					console.log("/block_zone_user",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                    db.close();
                }
                else{
                    //console.log(result);
                    if(result!=null&&result.type=="admin"){	
						var myquery = { email: req.body.user_email,zone: result.zone};
						
						var newvalues = { $set: {zone:""} };
						dbo.collection("user").updateOne(myquery, newvalues,function(err, dbres) {
							if (err){ 
								console.log("/block_zone_user",err);
								db.close();
							}
							else{
								var block = {zone:result.zone,email:req.body.user_email};

								dbo.collection("zone_user_block").insertOne( block, function(err, dbres) {
									if (err){
										console.log("/block_zone_user",err);
										db.close();
									}
									else{
										//console.log(dbres);
										res.json({"code":200,"message" : "","data":[]});
										console.log("/block_zone_user",JSON.stringify(req.body),JSON.stringify({"code":200,"message" : "","data":[]}));
										db.close();
									}
									
								});


							}
						});

					}
					else if(result!=null&&result.type=="super_admin"){	
						var myquery = { _id: new ObjectId(req.body.user_id)};
						var newvalues = { };
						if(req.body.user_type=="driver"){
							 newvalues = { $set: {driverStatus:"blocked"} };
						}
						else{
							newvalues = { $set: {clientStatus:"blocked"} };
						}
						dbo.collection("user").updateOne(myquery, newvalues,function(err, dbres) {
							if (err){ 
								console.log("/block_zone_user",err);
								db.close();
							}
							else{
								//console.log(dbres);
								res.json({"code":200,"message" : "","data":[]});
								console.log("/block_zone_user",JSON.stringify(req.body),JSON.stringify({"code":200,"message" : "","data":[]}));
								db.close();
							}
						});

					}
				}
			});
		}
	});
});

app.post("/unblock_zone_user",urlencodedParser,function(req,res){
	delete req.body['']; 
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
		if (err)
			console.log("/unblock_zone_user",err);
		else{
			var dbo = db.db();
			var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
			dbo.collection("user").findOne(myquery,function(err, result) {
				if (err){
					console.log("/unblock_zone_user",err);
					res.json({"code":201,"message" : result.zone});
					console.log("/unblock_zone_user",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
					db.close();
				}
				else{
					//console.log(result);
					if(result!=null&&result.type=="admin"){	
						var myquery = { email:req.body.user_email,zone: ""};
						
						var newvalues = { $set: {zone:result.zone} };
						dbo.collection("user").updateOne(myquery, newvalues,function(err, dbres) {
							if (err){ 
								console.log("/unblock_zone_user",err);
								db.close();
							}
							else{
								var block = {zone:result.zone,email:req.body.user_email};

								dbo.collection("zone_user_block").deleteMany( block, function(err, dbres) {
									if (err){
										console.log("/unblock_zone_user",err);
										db.close();
									}
									else{
										//console.log(dbres);
										res.json({"code":210,"message" : "","data":[]});
										console.log("/unblock_zone_user",JSON.stringify(req.body),JSON.stringify({"code":210,"message" : "","data":[]}));
										db.close();
									}
									db.close();
								});


							}
						});

					}
					else if(result!=null&&result.type=="super_admin"){	
						var myquery = { _id: new ObjectId(req.body.user_id)};
						var newvalues = { };
						if(req.body.user_type=="driver"){
							 newvalues = { $set: {driverStatus:"active"} };
						}
						else{
							newvalues = { $set: {clientStatus:"active"} };
						}
						dbo.collection("user").updateOne(myquery, newvalues,function(err, dbres) {
							if (err){ 
								console.log("/unblock_zone_user",err);
								db.close();
							}
							else{
								//console.log(dbres);
								res.json({"code":210,"message" : "","data":[]});
								console.log("/unblock_zone_user",JSON.stringify(req.body),JSON.stringify({"code":210,"message" : "","data":[]}));
								db.close();
							}
						});

					}
				}
			});
		}
	});
});

app.post("/blocked_zone_users",urlencodedParser,function(req,res){
	delete req.body['']; 
    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
        if (err)
            console.log("/blocked_zone_users",err);
        else{
            var dbo = db.db();
            var myquery = { "_id": new ObjectId(req.body["_id"]),hashedKey:req.body.hashedKey};
            dbo.collection("user").findOne(myquery,function(err, result) {
                if (err){
                    console.log("/blocked_zone_users",err);
					res.json({"code":201,"message" : ""});
					console.log("/blocked_zone_users",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                    db.close();
                }
                else{
                    //console.log(result);
                    if(result!=null&&result.type=="admin")
                    {	
						var myquery = {zone:result.zone};
						
						if(req.body.last_user_id!=null&&req.body.last_user_id!=""){
							myquery._id={$lt:ObjectId(req.body.last_user_id)}
						}
						
						if(req.body.search!=null&&req.body.search!=""){
							myquery["email"]={"$regex" : req.body.search, $options: 'i'};
						}
						
						dbo.collection("zone_user_block").find(myquery).sort({_id:-1}).limit(5).toArray(function(err, result) {
							if (err){
								console.log("/blocked_zone_users",err);
								res.json({"code":201,"message" : ""});
								console.log("/blocked_zone_users",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
								db.close();
							}
							else{
								res.json({code:200,users:result});
								console.log("/blocked_zone_users",JSON.stringify(req.body),JSON.stringify({code:200,users:result}));
								db.close();
							}
						});
						
					}

                    else{
						res.json({"code":201,"message" : ""});
						console.log("/users",JSON.stringify(req.body),JSON.stringify({"code":201,"message" : ""}));
                        db.close();
                    }
                }
            });
        }
    });

});

app.get("/add_zone",urlencodedParser,function(req,res){
	console.log(req.path,req.body);
	res.json({"code":201});
});

app.post("/add_zone",urlencodedParser,function(req,res){
	delete req.body['']; 
	console.log(req.path,req.body);
	var user={
		email:req.body.email,
		password:req.body.password,
		firstName:req.body.firstName,
		lastName:req.body.lastName,
		lang:req.body.lang,
		mobile:req.body.mobile,
		zone:req.body.zone,
		country:req.body.country,
		city:req.body.city
	};
	
	var zone={zone:req.body.zone,email:req.body.email,mobile:req.body.mobile};
	MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true }, function(err, db) {
		if (err) 
			console.log(req.path,err);
		else{	
			var dbo = db.db();
			var myquery = { "email": user.email};
			dbo.collection("user").findOne(myquery,function(err, result) {
				if (err){ 
					console.log(req.path,err);
					db.close();
				}
				else{
					//console.log(result);
					if(result!=null)
					{
						res.json({"code":201});
						console.log(req.path,JSON.stringify(req.body),JSON.stringify({"code":201}));
						db.close();
					}
					else{
						myquery = { "zone": zone.zone};
						dbo.collection("zones").findOne(myquery,function(err, result) {
							if (err){ 
								res.json({"code":202});
								console.log(req.path,err);
								db.close();
							}
							else{
								//console.log(result);
								if(result==null){
									saveAddZone(user,zone,res,dbo,db);
								}
								else {
									res.json({"code":202});
									console.log(req.path,JSON.stringify(req.body),JSON.stringify({"code":202}));
									db.close();
								}
							}
						});
					}
				}
			});
		}
	});
	function saveAddZone(user,zone,res,dbo,db){
		user.emailVerified=false;
		user.type="admin";
		user.adminStatus="active";
		user.createDate= new Date();
		user.verifyKey= crypto.createHmac('md5', constant.secret)
									   .update(user.createDate+"")
									   .digest('hex');
		user.hashedKey = crypto.createHmac('md5', constant.secret)
									   .update(user.password)
									   .digest('hex');
		delete user.password;
		dbo.collection("user").insertOne( user, function(err, resDB) {
			if (err) {
				console.log(req.path,err);
				db.close();
			}
			else{
				//console.log(resDB);
				dbo.collection("zones").insertOne( zone, function(err, resDB) {
					if (err) {
						console.log(req.path,err);
						db.close();
					}
					else{
						
						res.json({code:200});
						
						db.close();
						var text = "<html><body<p>Welcome to Ovikl</p><p>Open the below link to verify your email</p><p><a href=\"https://index.ovikl.com/verify_email?email="+
							user.email+"&verifyKey="+user.verifyKey+"\">https://index.ovikl.com/verify_email?email="+
							user.email+"&verifyKey="+user.verifyKey+"</a></p></body></html>";
						console.log(req.path,text);
						var mailOptions = {
							from: 'support@ovikl.com',
							to: user.email,
							bcc: 'support@ovikl.com',
							subject: 'Verify email in Ovikl',
							html: text
						};
						console.log(req.path,JSON.stringify(req.body),JSON.stringify({code:200}),text);
						transporter.sendMail(mailOptions, function(error, info){
							if (error) {
								console.log(req.path,JSON.stringify(req.body),error);
							} else {
								//console.log("/register",'Email sent: ' + info.response);
							}
						});
					}
				});
			}
		});
	}
});


//require("./files")(router,pool);
//app.use('/',router);
app.listen(port);
/*var httpsServer = https.createServer(credentials, app);
httpsServer.listen(8080);*/
console.log("Listening to PORT "+port);
