var express   =    require("express");
var app       =    express();

//var connect = require('connect');
//var moment = require('moment');
//var router      =   express.Router();
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/";
var ObjectId = require('mongodb').ObjectID;

var bodyParser = require('body-parser')
var urlencodedParser=bodyParser.urlencoded({ extended: false });

var multipart = require('connect-multiparty');
var uploadPath='/home/ubuntu/admin/images';
var multipartMiddleware = multipart({ uploadDir: uploadPath});

var redis = require("redis");
var client = redis.createClient();
const redisAdapter = require('socket.io-redis');
var io = require('socket.io')(8082);
io.adapter(redisAdapter({ host: 'localhost', port: 6379 }));
var uploadPath='/home/ubuntu/admin/images';
var fs = require('fs');
var cookieParser = require('cookie-parser')
//var session = require('express-session');
// Configuration
app.use(express.static( '/home/ubuntu/admin'));
// app.use(connect.cookieParser());
// app.use(connect.logger('dev'));
 //app.use(connect.bodyParser());
// app.use(connect.json());
// app.use(connect.urlencoded());

var database="transnet";
app.use(cookieParser());
/*app.set('trust proxy', 1);
app.use(session({
  secret: '5c69709f50c5da3697b8a83a',
  resave: false,
  saveUninitialized: true,
  cookie: {
    secure: true
  }
}));*/
app.post("/support_message",urlencodedParser,function(req,res){

	if (req.cookies.user_id) {
        console.log("recieve support message",req.body,req.cookies.user_id);
        if(req.body.userId==null||req.body.message==null){
            res.json({"code":201,"message" : "","data":[]});
        }
        else{
            MongoClient.connect(url, function(err, db) {
                if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					req.body.createTime= new Date();
					req.body.sender=true;
					dbo.collection("support_message").insertOne( req.body, function(err, dbres) {
						if (err)
							console.log(err);
						else{
							//console.log(dbres);
							res.json({"code":200,"message" : req.body._id,"data":[]});
						}
					});
				}
				db.close();
            });
        }
	}
	else{
		res.json({"code":205});
	}
});


app.post("/admin_support_message",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("recieve support message",req.body);
        if(req.body.userId==null||req.body.message==null){
            res.json({"code":201,"message" : "","data":[]});
        }
        else{
            MongoClient.connect(url, function(err, db) {
                if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					req.body.createTime= new Date();
					req.body.sender=false;
					req.body.type="text";
					dbo.collection("support_message").insertOne( req.body, function(err, dbres) {
						if (err)
							console.log(err);
						else{
							//console.log(dbres);
							res.json({"code":200,"message" : req.body._id,"data":[]});
						}
					});
					db.close();
				}
            });
        }
	}
	else{
		res.json({"code":205});
	}
});


app.post("/get_support_message",urlencodedParser,function(req,res){

	if (req.cookies.user_id) {
        console.log("recieve get_support_message",req.body,req.cookies.user_id);
        if(req.body.userId==null)
            res.json({"code":201,"message" : "","data":[]});
        /*else if((req.body.lastMessageId==null||req.body.lastMessageId=="")&&req.body.type!=null)  {
            var msg = {message:"",sender:false,userId:req.body.userId,createTime:new Date()}
            if(req.body.type=="driver"){
                if(req.body.lang==null||req.body.lang=="ar"){
                    msg.message="برجاء اسال رخصه السياره والقياده";
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
            MongoClient.connect(url, function(err, db) {
                if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
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
            MongoClient.connect(url, function(err, db) {
				if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					var myquery = {"userId": req.body.userId,deleted:null};
					if(req.body.lastMessageId!=null&&req.body.lastMessageId!=""){
						myquery._id={$gt:new ObjectId(req.body.lastMessageId)};
					}
					if(req.body.type!=null){
						myquery.sender=false;
					}
					dbo.collection("support_message").find(myquery).sort({$natural:-1}).limit(50).toArray(function(err, result) {
						if (err)
							console.log(err);
						else{
							console.log(result);
							res.json({"code":200,"message" : "","data":[],"messages":result});
						}
					});
					db.close();
				}
            });
        }
	}
	else{
		res.json({"code":205});
	}
});


app.post("/get_opened_support_message",urlencodedParser,function(req,res){

	if (req.cookies.user_id) {
        console.log("recieve get_opened_support_message",req.body,req.cookies.user_id);
        MongoClient.connect(url, function(err, db) {
			if (err)
				console.log(err);
			else{
				var dbo = db.db(database);
				var myquery = {"state": null,deleted:null};
				if(req.body.lastMessageId!=null&&req.body.lastMessageId!=""){
					myquery._id={$gt:new ObjectId(req.body.lastMessageId)};
				}
				dbo.collection("support_message").find(myquery).sort({$natural:1}).limit(10).toArray(function(err, result) {
						if (err)
							console.log(err);
						else{
							console.log(result);
							res.json({"code":200,"message" : "","data":[],"messages":result});
						}
					});
				db.close();
			}
        });
	}
	else{
		res.json({"code":205});
	}
});

app.post("/solve_support_message",urlencodedParser,function(req,res){
    if (req.cookies.user_id) {
		console.log("recieve solve_support_message",req.body);
		if(req.body.userId==null)
			res.json({"code":201,"message" : "","data":[]});
		else{
			MongoClient.connect(url, function(err, db) {
				if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					var myquery = {"userId": req.body.userId};
					if(req.body.lastMessageId!=null&&req.body.lastMessageId!=""){
						myquery._id={$lte:new ObjectId(req.body.lastMessageId)};
					}
					var newvalues = { $set: {state:"closed"} };
					dbo.collection("support_message").update(myquery, newvalues,{upsert:false, multi:true}, function(err, dbres) {
						if (err)
							console.log(err);
						else{
							//console.log(dbres);
							res.json({"code":200,"message" : "","data":[]});
						}
					});
					db.close();
				}
			});
		}
	}
	else{
		res.json({"code":205});
	}
});




app.post("/admin/get_user",urlencodedParser,function(req,res){
	console.log("admin",req.body,req.cookies.user_id);
	if (req.cookies.user_id) {

        if((req.body._id!=null && req.body._id.length==24)||req.body.mobile!=null){
            MongoClient.connect(url, function(err, db) {
				if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					var query = { };
					if(req.body._id!=""){
						query._id=new ObjectId(req.body._id);
					}
					if(req.body.mobile!=""){
						query.id=req.body.mobile;
					}
					if(req.body.type!=""){
						query.type=req.body.type;
					}
					if(req.body.status!=""){
						query.status=req.body.status;
					}
					var skip=0;
					var sort = {_id:1};
					if(req.body.skip!=""){
						skip=Number(req.body.skip);
					}
					if(req.body.sort!=""){
						sort = {_id:Number(req.body.sort)};
					}
					dbo.collection("users").find(query).sort(sort).skip(skip).limit(1).toArray(function(err, result) {
						if (err)
							console.log(err);
						else{
							if(result.length>0){
								console.log(' data :',JSON.stringify(result[0]));
								res.json({"code":200,"message":result[0]});
							}
							else{
								res.json({"code":201,"message":""});
							}
						}
					});
					db.close();
				}
            });
        }else{
            res.json({"code":201,"message":""});
        }
	}
	else{
		res.json({"code":205});
	}
});

app.post("/admin/update_user",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("admin update_user",req.body);
        MongoClient.connect(url, function(err, db) {
			if (err)
				console.log(err);
			else{
				var dbo = db.db(database);
				if(req.body._id==""){
					delete req.body["_id"];
					req.body.createDate= new Date();
					dbo.collection("users").insertOne(req.body,function(err, result) {
						if (err)
							console.log(err);
						else{
							res.json({"code":200,"message" : "Updated successfully"});
						}
					});
				}
				else{
					var query = { "_id":  new ObjectId(req.body._id)};
					delete req.body["_id"];
					req.body.createDate= new Date(req.body.createDate);
					var newvalues = { $set: req.body };
					dbo.collection("users").updateOne(query,newvalues,function(err, result) {
						if (err)
							console.log(err);
						else{
							res.json({"code":200,"message" : "Updated successfully"});
						}
					});
				}
				db.close();
			}
		});
	}
	else{
		res.json({"code":205});
	}
});


/*
socket_location_hash:	socket.id & location
socket_user_hash: 			socket.id & user info
user_socket_hash: 		_id & socket.id
user_trip_hash: 		_id & trip info
*/

app.post("/socket_location_hash",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("admin socket_location_hash",req.body);
		client.hget('socket_location_hash',req.body.socketId, function(err, data) {
					if(err!=null || data == null)
						res.json({"code":201,"message" : ""});
					else
						res.json({"code":200,"message" : data});
		});
	}
	else{
		res.json({"code":205});
	}
});


app.post("/socket_user_hash",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("admin socket_user_hash",req.body);
		client.hget('socket_user_hash',req.body.socketId, function(err, data) {
					if(err!=null || data == null)
						res.json({"code":201,"message" : ""});
					else
						res.json({"code":200,"message" : data});
		});
	}
	else{
		res.json({"code":205});
	}
});

app.post("/user_socket_hash",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("admin user_socket_hash",req.body);
		client.hget('user_socket_hash',req.body.userId, function(err, data) {
					if(err!=null || data == null)
						res.json({"code":201,"message" : ""});
					else
						res.json({"code":200,"message" : data});
		});
	}
	else{
		res.json({"code":205});
	}
});

app.post("/user_trip_hash",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("admin user_trip_hash",req.body);
		client.hget('user_trip_hash',req.body.userId, function(err, data) {
					if(err!=null || data == null)
						res.json({"code":201,"message" : ""});
					else
						res.json({"code":200,"message" : data});
		});
	}
});


app.post("/socket_user_hash_all",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("admin socket_user_hash",req.body);
		client.HGETALL('socket_user_hash', function(err, data) {
					if(err!=null || data == null)
						res.json({"code":201,"message" : ""});
					else
						res.json({"code":200,"message" : data});
		});
	}
	else{
		res.json({"code":205});
	}
});

app.post("/user_trip_hash_all",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("admin user_trip_hash",req.body);
		client.HVALS('user_trip_hash', function(err, data) {
					if(err!=null || data == null)
						res.json({"code":201,"message" : ""});
					else
						res.json({"code":200,"message" : data});
		});
	}
	else{
		res.json({"code":205});
	}
});

app.post("/rooms",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("recieve rooms/",req.body);
		io.of('/').adapter.clientRooms(req.body.socketId, (err, rooms) => {
		  if (err) {
			res.json({"code":201,"message" : ""});
			}
			else{
				console.log(rooms); // an array containing every room a given id has joined.
				res.json({"code":200,"message" : rooms});
			}
		});
	}
	else{
		res.json({"code":205});
	}

});


app.post("/all_rooms",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("recieve rooms/",req.body);
		io.of('/').adapter.allRooms((err, rooms) => {
		  if (err) {
			res.json({"code":201,"message" : ""});
			}
			else{
				console.log(rooms); // an array containing every room a given id has joined.
				res.json({"code":200,"message" : rooms});
			}
		});
	}
	else{
		res.json({"code":205});
	}
});

app.post("/room_sockets",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("recieve rooms/",req.body);
		io.in(req.body.room).clients( (err, clients) => {
		  if (err) {
			res.json({"code":201,"message" : ""});
			}
			else{
				console.log(clients); // an array containing every room a given id has joined.
				res.json({"code":200,"message" : clients});
			}
		});
	}
	else{
		res.json({"code":205});
	}
});


app.post("/trip_list",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("recieve trip_list/",req.body);
		if(req.body.tripId!=null){

		}else{

		}
	}
	else{
		res.json({"code":205});
	}
});


app.post("/trip_message_list",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
        console.log("recieve trip_message_list/",req.body);
		if(req.body.tripId!=null){

		}else{

		}
	}
	else{
		res.json({"code":205});
	}
});


app.post("/delete_msg",urlencodedParser,function(req,res){
	if (req.cookies.user_id) {
		console.log("recieve delete_imsg/",req.body);
		MongoClient.connect(url, function(err, db) {
				if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					var myquery = { _id: new ObjectId(req.body._id),userId: req.body.userId};
					var newvalues = { $set: {deleted:true} };
					dbo.collection("support_message").updateOne(myquery, newvalues,function(err, dbres) {
					if (err){
						console.log(err);
						db.close();

					}
					else{
						//console.log(dbres);

						db.close();
						if(req.body.image!=null){
							fs.unlink(uploadPath+"/"+req.body.image,function (err) {
								if (err)
									console.log('File deleted err '+err);
								else
									console.log('File deleted');
								res.json({"code":200,"_id" : req.body._id,"data":[]});
							});

						}
						else{
							res.json({"code":200,"_id" : req.body._id,"data":[]});
						}
					}
				});
			}
		});
	}
	else{
		res.json({"code":205});
	}

});

app.post('/upload',multipartMiddleware, function(req, res) {
	if (req.cookies.user_id) {
		delete req.body[''];
		console.log("file post",req.body,req.files);

		var file = req.files.image;
		//console.log(file.name);
		//console.log(file.type);
		//console.log(file);
		if(req.body.userId!=null && req.body.userId!=""){
			MongoClient.connect(url, function(err, db) {
				if (err)
					console.log(err);
				else{
					var dbo = db.db(database);
					req.body.createTime= new Date();
					//req.body.sender=true;
					req.body.type="image";
					req.body.state="closed";
					var message ={userId:req.body.userId,image:file.path.replace(uploadPath+"/",""),type:"image",createTime:new Date()};
					console.log( message);
					var newvalues = { $set: req.body };
					dbo.collection("support_message").insertOne( message, function(err, dbres) {
						if (err) throw err;
						//console.log(dbres);
							//res.json({"code":200,"message" : message,"data":[]});
							res.redirect('/user.html?_id='+req.body.userId);
					});
					db.close();
				}
			});
		}
	}
	else{
		res.redirect('/login.html');
	}
});


app.post('/login',urlencodedParser, function (req, res) {
	//console.log("login",req.body);
	var post = req.body;
	if (post.user === 'usadmin' && post.password === 'admin@us') {

		res.cookie('user_id', 'usadmin');
		console.log("login done" );
		res.redirect('/messages.html');
	} else {
		res.send('Bad user/pass');
	}
});

app.get('/',urlencodedParser, function (req, res) {

    res.redirect('/login.html');

});




//require("./files")(router,pool);
//app.use('/',router);
app.listen(8081);
console.log("Listening to PORT 8081");


