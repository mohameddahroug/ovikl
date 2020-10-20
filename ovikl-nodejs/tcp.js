const tls = require('tls');
const fs = require('fs');
var constant = require('./constant');
//var express	 =		require("express");
//var app			 =		express();
//const crypto = require('crypto');
var port = process.argv[2];
var prod=process.argv[3];

var jwt = require('jsonwebtoken');
const http2 = require('http2');
var privateKeyP8;



if(prod==undefined||prod!="dev"){
    console.log('prod');
    prod=true;
    privateKeyP8 = fs.readFileSync('./AuthKey.p8');
}
else{
    console.log('dev');
    prod=false;
}
var redis = require("redis");
//redis.debug_mode = true;
var client = redis.createClient();
client.on("error", function (err) {
		console.log("Error " + err);
});

process.on('warning', e => console.warn(e.stack));

var database="transnet";
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/transnet";
//var urlTaxifare = "mongodb://localhost:27017/taxifare";
var ObjectId = require('mongodb').ObjectID;

//var bodyParser = require('body-parser')
//var urlencodedParser=bodyParser.urlencoded({ extended: false });

//var multipart = require('connect-multiparty');
//var multipartMiddleware = multipart({ uploadDir: constant.uploadPath});
//var nodemailer = require('nodemailer');



/*var transporter = nodemailer.createTransport({
	port: 25,
		host: 'localhost',
		tls: {
			rejectUnauthorized: false
		},
	});*/
//var io = require('socket.io-emitter')({ host: '127.0.0.1', port: 6379 });

/*client.keys('user_*location', function (err, keys) {
  if (err) return console.log(err);

  for(var i = 0, len = keys.length; i < len; i++) {
    client.del(keys[i]);
     console.log('delete '||keys[i])
  }
});

client.keys('user_*zone', function (err, keys) {
  if (err) return console.log(err);

  for(var i = 0, len = keys.length; i < len; i++) {
    client.del(keys[i]);
     console.log('delete '||keys[i])
  }
});*/

client.keys('user_*info', function (err, keys) {
  if (err) return console.log(err);

  for(var i = 0, len = keys.length; i < len; i++) {
    console.log('delete '+keys[i]);
    client.del(keys[i]);
  }
});


var admin = require("firebase-admin");

var serviceAccount = require("./transnet-bebf1-firebase-adminsdk.json");
if(prod){
    admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://transnet-bebf1.firebaseio.com"
    });
}


if(prod){
    options = {
    key: fs.readFileSync('/etc/letsencrypt/live/ovikl.com-0001/privkey.pem'),
    cert: fs.readFileSync('/etc/letsencrypt/live/ovikl.com-0001/fullchain.pem'),
    // This is necessary only if using client certificate authentication.
    //requestCert: true
    };
    const server = tls.createServer(options,(c) => {handle(c);});
    server.on('error', (err) => {
        console.log('ssl server error', err);
        disconnect(publishClient,subscribeClient);
    });
    
    server.listen(port, () => {
        console.log('ssl server bound '+port);
    });    
}
else{
    const net = require('net');
    const server = net.createServer();
    server.on('connection',handle)
    server.on('error', (err) => {
        console.log('server error', err);
        disconnect(publishClient,subscribeClient);
    });
    server.listen(port, () => {
        console.log('server bound '+port);
    }); 
}

//const net = require('net');
function handle(c){

    var publishClient = redis.createClient();
    var subscribeClient = redis.createClient();
	// 'connection' listener.
	console.log('client connected');
    var socket={_id:null,latitude:null,longitude:null,trip:{},zone:null,zones:null,zoneCode:null,toAdminTime:null,type:null};
    var userFields={projection: {type:1,createDate:1,
    carMadeYear:1,carManufacturer:1,carModel:1,carNumber:1,carType:1,
    'images.frontImageSmall':1,'images.sideImageSmall':1,'images.backImageSmall':1,
    cost:1,fcmToken:1,iosToken:1,clientStatus:1,driverStatus:1,mobile:1,
    driverRate:1,carRate:1,clientRate:1,zone:1,adminStatus:1,email:1,firstName:1,lastName:1}};
	//socket._id=Math.random().toString().replace("0.","")+Math.random().toString().replace("0.","");
	//c.setKeepAlive(true,3000);
	c.setTimeout(constant.timeoutServer);
	c.on('timeout', ()=>{
	    console.log(socket._id,'timeout');
	    c.end();
	    disconnect();
	});
	c.on('error', (err) => {
    	console.log('server error', err);
    	disconnect();
    });
	c.on('end', () => {
		console.log(socket._id,'client disconnected');
		disconnect();
	});
	c.on('data', (dataStr) => {
        try{
		    

            data=JSON.parse(dataStr);
            if(socket._id==null)
                console.log(JSON.stringify(data));
            else
                console.log(socket._id,JSON.stringify(data));
            if(data.event=="login"&&socket._id==null){
                login(data,write,subscribeClient);
            }
            else if(data.event=="userInfo"&&data.auth_id==socket._id){
                userInfo(data,write);
            }
            else if(data.event=="location"&&data.auth_id==socket._id){
                location(data,write);
            }
            else if(data.event=="selectedDriver"&&data.auth_id==socket._id){
                selectedDriver(data,write);
            }
            else if(data.event=="driverConfirmed"&&data.auth_id==socket._id){
                driverConfirmed(data,write);
                //c.setTimeout(constant.timeoutServerLong);
            }
            else if(data.event=="driverCancel"&&data.auth_id==socket._id){
                driverCancel(data,write);
            }
            else if(data.event=="clientCancel"&&data.auth_id==socket._id){
                clientCancel(data,write);
            }
            else if(data.event=="tripMessage"&&data.auth_id==socket._id){
                tripMessage(data,write);
            }
            else if(data.event=="startTrip"&&data.auth_id==socket._id){
                startTrip(data,write);

            }
            else if(data.event=="finishTrip"&&data.auth_id==socket._id){
                finishTrip(data,write);
            }
            else if(data.event=="close"&&data.auth_id==socket._id){
                disconnect(publishClient,subscribeClient);
            }
            else{
                write(dataStr);
            }
		}
		catch (ex) {
	        console.log(socket._id,"read tcp "+ex);

		}

	})
	function write(data){
        try {
            console.log(socket._id,"response "+data.toString());
            c.write(data+'\n');
            //c.pipe(c);
        } catch (ex) {
            console.log(socket._id,"write tcp"+ex);
            c.end();
            disconnect(publishClient,subscribeClient);
        }
    }

	function getChannelMessage(channel,message){
        console.log(socket._id,"channels get message "+channel+" "+message);
        try {
            if(channel.startsWith("user_")){
                data=JSON.parse(message);
                if(data.event!=null){
                    if(data.event=="onDriverNewTrip"||
                        data.event=="onDriverConfirmTrip"||
                        data.event=="onDriverStartTrip"){
                        socket.trip={ ...socket.trip, ...data.trip };
                    }
                    else if(
                         data.event=="onDriverCancelTrip"||
                         data.event=="onClientCancelTrip"||
                         data.event=="onDriverFinishTrip"){
                         delete socket.trip; socket.trip={};
                         //c.setTimeout(constant.timeoutServer);
                    }

                    /*if(data.event=="onDriverConfirmTrip"){
                        c.setTimeout(constant.timeoutServerLong);
                    }*/
                    else if(data.event=="disconnect"){
                        c.write(message+'\r\n');
                        disconnect(publishClient,subscribeClient);
                        c.end();
                        return;
                    }
                    //console.log(socket);
                    
                }
            }
            if(socket.type=="client"&&data.event=="onNewDriverLocation"&&socket.zoneCode!=null&&socket.zoneCode!=""){
                var d=Date.now();
                if(socket.toAdminTime==null||(d-socket.toAdminTime>5000)){
                    socket.toAdminTime=d;
                    if(data.tripId==null){
                        publishClient.publish(socket.zoneCode+"_admin",JSON.stringify({
                            event:'onNewClientLocation',msgId:generateMsgId(),_id: socket._id,latitude:socket.latitude,longitude:socket.longitude,auth_id:socket._id
                        }));
                        publishClient.publish("super_admin",JSON.stringify({
                            event:'onNewClientLocation',msgId:generateMsgId(),_id: socket._id,latitude:socket.latitude,longitude:socket.longitude,auth_id:socket._id
                        }));
                    }
                    else{
                        publishClient.publish(socket.zoneCode+"_admin",JSON.stringify({
                            event:'onOffClientLocation',msgId:generateMsgId(),_id: socket._id,latitude:socket.latitude,longitude:socket.longitude,auth_id:socket._id
                        }));
                        publishClient.publish("super_admin",JSON.stringify({
                            event:'onOffClientLocation',msgId:generateMsgId(),_id: socket._id,latitude:socket.latitude,longitude:socket.longitude,auth_id:socket._id
                        }));
                    }
                }
                
            }
            c.write(message+'\r\n');
            //c.pipe(c);
        } catch (ex) {
            console.log(socket._id,"channels get message "+ex);
        }
    }

	subscribeClient.on("message",getChannelMessage);





    function login(data,fn){
                    
        var tag = "login";

            //console.log(tag,JSON.stringify(data));
            msgId=data.msgId;
            delete data[''];
            //client.hset('socket_location_hash',socket._id,JSON.stringify({'_id':data._id,'type':data.type,'latitude':null,'longitude':null}));
            client.get("user_"+data._id+".info", function(err, userExist) {
                if(userExist!=null&&err==null){
                    //console.log('login','userExist');
                    fn(JSON.stringify({event:"login",retry:true}));
                    var r={}
                    r.event="disconnect";
                    r._id=data._id
                    publishClient.publish("user_"+data._id,JSON.stringify(r));
                    c.end();
                    disconnect();
                }
                else{
                    socket._id=data._id;
                    socket.type=data.type
                    MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true }, function(err, db) {
                        if (err)
                            console.log('login',err);
                        else{
                            var dbo = db.db(database);
                            //var query = { "_id":	new ObjectId(data._id),passwordHashed:data.passwordHashed};
                            var aggregation=[
                                                {
                                                    $match:{
                                                    "_id":	new ObjectId(data._id),hashedKey:data.hashedKey
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
                                                        driverRate:1,carRate:1,clientRate:1,zone:1,adminStatus:1,email:1,firstName:1,lastName:1,
                                                        passwordHashed:1,
                                                        "zoneContact": { "$arrayElemAt": [ "$zoneContact", 0 ] }
                                                    } 
                                                }
                                            ];
                            dbo.collection("user").aggregate(aggregation).toArray(function(err, result) {
                                if (err)
                                    console.log('login',err);
                                else if(result[0]!=null){
                                    result=result[0];
                                    result.cacheTime=new Date();
                                    result.port=port;
                                    if(result.zone != null){
                                        socket.zoneCode=result.zone;
                                    }
                                    if(result.type == 'admin'){
                                        subscribeClient.SUBSCRIBE(socket.zoneCode+"_admin");
                                    }
                                    else if(result.type == 'super_admin'){
                                        subscribeClient.SUBSCRIBE("super_admin");
                                    }
                                    else{
                                        subscribeClient.SUBSCRIBE("user_"+data._id);
                                    }
                                    client.set("user_"+data._id+".info",JSON.stringify(result));
                                    client.get("user_"+data._id+".trip", function(errTrip, replyTrip) {
                                        if(errTrip==null && replyTrip != null){
                                            client.lrange('trip_list_'+replyTrip,0,-1,function(err,tripArr){
                                                client.lrange('trip_message_list_'+replyTrip,0,-1,function(err,tripMessageArr){
                                                    if(tripArr!=null){
                                                        delete socket.trip; socket.trip={};
                                                        for(i=0;i<tripArr.length;i++){
                                                            tripArr[i]=JSON.parse(tripArr[i]);
                                                            socket.trip={...tripArr[i], ...socket.trip };
                                                            /*if(tripArr[i].state=='RESERVED'){
                                                                c.setTimeout(constant.timeoutServerLong);
                                                            }*/
                                                        }
                                                    }
                                                    if(tripMessageArr!=null){
                                                        for(i=0;i<tripMessageArr.length;i++){
                                                            tripMessageArr[i]=JSON.parse(tripMessageArr[i]);
                                                        }
                                                    }
                                                    client.get("user_"+tripArr[0].driverId+'.location', function(err, replyLocation) {
                                                        if(err==null&&replyLocation!=null){
                                                            replyLocation=JSON.parse(replyLocation);
                                                            s=JSON.stringify({event:data.event,msgId:data.msgId,user:result,tripArr:tripArr,tripMessageArr:tripMessageArr,driverLocation:replyLocation});
                                                            //console.log('login',s);
                                                            fn(s);
                                                        }
                                                        else{
                                                            s=JSON.stringify({event:data.event,msgId:data.msgId,user:result,tripArr:tripArr,tripMessageArr:tripMessageArr,driverLocation:null});
                                                            //console.log('login',s);
                                                            fn(s);
                                                        }
                                                    });

                                                });
                                            });
                                            db.close();
                                        }
                                        else{
                                            if(data.tripId!=null){

                                                var dbo = db.db(database);
                                                query = { "_id":	new ObjectId(data.tripId)};
                                                dbo.collection("trip").findOne(query,function(err, tripResult) {
                                                    if(tripResult!=null){
                                                        var i=0;
                                                        var tripArr=new Array();
                                                        if(tripResult.FINISHED!=null){tripArr[i]=tripResult.FINISHED; i++;}
                                                        if(tripResult.CANCELED!=null){tripArr[i]=tripResult.CANCELED; i++;}
                                                        if(tripResult.STARTED!=null){tripArr[i]=tripResult.STARTED; i++;}
                                                        if(tripResult.RESERVED!=null){tripArr[i]=tripResult.RESERVED; i++;}
                                                        if(tripResult.PENDING!=null){tripArr[i]=tripResult.PENDING;	i++;}
                                                        query = { "tripId": data.tripId};

                                                        dbo.collection("trip_messages").find(query).toArray(function(err, tripMessageArr) {
                                                                s=JSON.stringify({event:data.event,msgId:data.msgId,user:result,tripArr:tripArr,tripMessageArr:tripMessageArr});
                                                                //console.log('login',s);
                                                                fn(s);
                                                                db.close();
                                                        });

                                                    }
                                                    else{
                                                        s=JSON.stringify({event:data.event,msgId:data.msgId,user:result,tripArr:null,tripMessageArr:null});
                                                        //console.log('login',s);
                                                        fn(s);
                                                        db.close();
                                                    }

                                                });

                                            }
                                            else{
                                                //console.log('login',result);
                                                fn(JSON.stringify({event:data.event,msgId:data.msgId,user:result}));
                                                db.close();
                                            }
                                        }
                                    });
                                }

                            });
                        }

                    });
                }
            });
    }


    function userInfo(data,fn) {
        var tag='userInfo';
        //console.log(tag,' get driver info:',data);
        delete data[''];
        getUser(data.user_id, function( reply) {

        if(reply != null){
            //console.log(tag,reply);
            fn(JSON.stringify({msgId:data.msgId,event:data.event,user:reply}));
        }else{
            fn(JSON.stringify({msgId:data.msgId,event:data.event,user:null}));
        }
        });
    }


    function getUser(_id,fn) {
        var tag='getUser';
        client.get("user_"+_id+".info", function(err, reply) {

        if(err==null && reply != null){
            //console.log(tag,reply);
            var user = JSON.parse(reply);
            //delete user.fcmToken;
            //delete user.iosToken;
            delete user.port;
            delete user.cacheTime;

            fn(user);
        }else{
            MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
                if (err){
                console.log(err);
                fn(null);
                }
                else{
                var dbo = db.db(database);
                var query = { "_id":	new ObjectId(_id)};
                
                dbo.collection("user").findOne(query,userFields,function(err, result) {
                    if (err){
                    console.log(err);
                    fn(null);
                    }
                    else if(result!=null){
                        var user = result;
                        delete user.fcmToken;
                        delete user.iosToken;
                        delete user.port;
                        delete user.cacheTime;
                        fn(user);
                    }
                });
                }
            });
        }
        });
    }

    function getDriverZones(latitude,longitude){
        var distance=3;
        var lat=(Math.floor(latitude*100/distance)*distance);
        var lng=(Math.floor(longitude*100/distance)*distance);

        if(socket.zoneCode != null)
            return [socket.zoneCode];
        else
            return ["zone_"+ (lat-distance) +"_"+(lng-distance) , "zone_"+ (lat-distance) +"_"+(lng) , "zone_"+ (lat-distance) +"_"+(lng+distance),
                "zone_"+ (lat) +"_"+(lng-distance) , "zone_"+ (lat) +"_"+(lng) , "zone_"+ (lat) +"_"+(lng+distance),
                "zone_"+ (lat+distance) +"_"+(lng-distance) , "zone_"+ (lat+distance) +"_"+(lng) , "zone_"+ (lat+distance) +"_"+(lng+distance)];
    }

    function getClientZone(latitude,longitude){
        var distance=3;
        var lat=(Math.floor(latitude*100/distance)*distance);
        var lng=(Math.floor(longitude*100/distance)*distance);
        if(socket.zoneCode != null)
            return socket.zoneCode;
        else
            return "zone_"+ (lat) +"_"+(lng);
    }

    function location(data,fn) {
        
        var tag = 'location';

        //console.log(tag,socket);
        delete data[''];
        data.latitude=(Math.round(data.latitude*10000)/10000);
        data.longitude=(Math.round(data.longitude*10000)/10000);

        fn(JSON.stringify({msgId:data.msgId}));
        client.set("user_"+data._id+'.location',JSON.stringify({_id:data._id,type:data.type,latitude:data.latitude,longitude:data.longitude,cacheTime:new Date(),port:port}));
        socket.latitude=data.latitude;
        socket.longitude=data.longitude;
        //client.hset('user_socket_hash',data._id,socket._id);

        if(socket.trip._id == null){
                //console.log('location reply :',reply);
                //console.log('location err :',err);

            if(data.type == 'driver'){
                socket.zones = getDriverZones(data.latitude,data.longitude);
                //console.log('location zone :',zones);
                data.event='onNewDriverLocation';
                for (i = 0; i < socket.zones.length; i++) {
                    publishClient.publish(socket.zones[i],JSON.stringify(data));
                }
                //client.set("user_"+data._id+'.zones',zones.toString());
                
            }
            else if(data.type == 'client'){
                data.event='onNewClientLocation';
                var zone=getClientZone(data.latitude,data.longitude);
                
                if(socket.zone==null){
                    subscribeClient.SUBSCRIBE(zone);
                }
                else if(socket.zone!=null&&socket.zone!=zone){
                    subscribeClient.unsubscribe(socket.zone);
                    client.set("user_"+data._id+'.zone',zone);
                    subscribeClient.SUBSCRIBE(zone);
                }
                socket.zone=zone;
                
            }
            
        }
        else if(socket.trip._id != null){
            if(data.type == 'driver'){
                data.event='onNewDriverLocation';
                publishClient.publish("user_"+data.clientId,JSON.stringify(data));
            }
            /*else if(data.type == 'client'){
                data.event='onNewClientLocation';
                publishClient.publish("user_"+data.driverId,JSON.stringify(data));
            }*/
        }

        if(socket.zoneCode!=null&&socket.zoneCode!=""){
            publishClient.publish(socket.zoneCode+"_admin",JSON.stringify(data));
        }
        publishClient.publish("super_admin",JSON.stringify(data));

    }

    function selectedDriver(data,fn) {
            
            var tag = 'selectedDriver';
            console.log(tag,'selectedDriver :',data);
            var tripJson=data;
            delete tripJson[''];
            //socket.zone=null;

        getUser(tripJson.driverId,function(driver){
            client.get("user_"+tripJson.driverId+".trip", function(err, reply) {
            if(err==null && reply == null){
                getUser(tripJson.clientId,function(clientUser){
                //tripJson.startTime=Date.now();
                tripJson._id= new ObjectId();
                tripJson.driver=driver;
                tripJson.client=clientUser;
                tripJson.prMin=driver.cost.minimum;
                tripJson.prBase=driver.cost.base;
                tripJson.prKM= driver.cost.km;
                tripJson.prMinute= driver.cost.minute;
                tripJson.cur=driver.cost.currency;
                tripJson.createTime = new Date();
                tripJson.updateTime = tripJson.createTime;
                if(socket.zoneCode!=null&&socket.zoneCode!=""){
                    tripJson.zone=socket.zoneCode;
                }
                var tripJsonStr=JSON.stringify(tripJson);
                fn(JSON.stringify({
                event: data.event,
                msgId:data.msgId,
                trip:tripJson}));
                client.set("user_"+tripJson.clientId+".trip",tripJson._id.toString());
                client.set("user_"+tripJson.driverId+".trip",tripJson._id.toString());
                client.lpush('trip_list_'+tripJson._id.toString(),tripJsonStr);
                publishClient.publish("user_"+tripJson.driverId,JSON.stringify({
                    event: 'onDriverNewTrip',
                    msgId:generateMsgId(),
                    trip:tripJson}));
                //console.log(tag,'send onDriverNewTrip to '+"user_"+tripJson.driverId);
                pushNotification('You have new trip',null,driver.fcmToken,driver.iosToken);
                });
                socket.trip=tripJson;
                
                if(socket.zone!=null){
                    subscribeClient.unsubscribe(socket.zone);
                    client.del("user_"+socket._id+'.zone');
                    socket.zone=null;
                }
                
            }else{
                tripJson.state='driverHasTrip';
                fn(JSON.stringify({
                event: data.event,
                msgId:data.msgId,
                "trip":tripJson}));
                //pushNotification('The driver has another trip',null,clientUser.fcmToken,clientUser.iosToken);
            }
            });
        });
    }

    function driverConfirmed(data,fn) {

        var tag = "driverConfirmed "+ socket._id+" ";
        data.updateTime=new Date();
        delete data[''];
        dataStr=JSON.stringify(data);
        fn(JSON.stringify({event: data.event,msgId:data.msgId,trip:data}));
        var tripRoom ="trip_"+data._id;
        //client.set("user_"+data.clientId+".trip",tripJson._id.toString());
        //client.set("user_"+data.driverId+".trip",data._id);
        

        //client.get("user_"+data.driverId+'.zones', function(err, oldZones) {
            if(socket.zones!=null){
                for(i=0;i<socket.zones.length;i++){
                    publishClient.publish(socket.zones[i],JSON.stringify(
                    {event:'onOffDriverLocation',msgId:generateMsgId(),_id: data.driverId}
                    ));
                }
            socket.zones=null;
            //client.del("user_"+data.driverId+'.zones');
            }
        //});

        publishClient.publish("user_"+data.clientId,JSON.stringify({
            event: 'onDriverConfirmTrip',
            msgId:generateMsgId(),
            trip:data}));
            pushNotification('The driver confirm the trip',null,socket.trip.client.fcmToken,socket.trip.client.iosToken);
        socket.trip={ ...socket.trip, ...data };
        client.lpush('trip_list_'+data._id,JSON.stringify(socket.trip));
    }

    function driverCancel(data,fn) {
        
        var tag = 'driverCancel '+socket._id;
        data.updateTime=new Date();
        delete data[''];
        dataStr=JSON.stringify(data);
        //io.to("trip_"+data._id).emit('onDriverCancelTrip',dataStr);
        //client.lpush('trip_list_'+data._id,dataStr);
        socket.trip={ ...socket.trip, ...data };
        client.lpush('trip_list_'+data._id,JSON.stringify(socket.trip));
        saveTrip(data.driverId,data.clientId,data._id);
        fn(JSON.stringify({event: data.event,msgId:data.msgId,trip:data}));
        publishClient.publish("user_"+data.clientId,JSON.stringify({
            event: 'onDriverCancelTrip',
            msgId:generateMsgId(),
            trip:data}));
        pushNotification('The driver cancel the trip',null,socket.trip.client.fcmToken,socket.trip.client.iosToken);
        delete socket.trip; socket.trip={};
    }


    function clientCancel(data,fn) {
        
        var tag = 'clientCancel '+socket._id;
        data.updateTime=new Date();
        delete data[''];
        dataStr=JSON.stringify(data);
        //io.to("trip_"+data._id).emit('onClientCancelTrip',dataStr);
        //client.lpush('trip_list_'+data._id,dataStr);
        socket.trip={ ...socket.trip, ...data };
        client.lpush('trip_list_'+data._id,JSON.stringify(socket.trip));
        saveTrip(data.driverId,data.clientId,data._id);
        fn(JSON.stringify({event: data.event,msgId:data.msgId,trip:data}));
        publishClient.publish("user_"+data.driverId,JSON.stringify({
            event: 'onClientCancelTrip',
            msgId:generateMsgId(),
            trip:data}));
        if(socket.trip.driver.fcmToken!=null){
            pushNotification('The client cancel the trip',null,socket.trip.driver.fcmToken,socket.trip.driver.iosToken);
        }
        delete socket.trip; socket.trip={};
    }


    function tripMessage(data,fn) {
        
        var tag = 'tripMessage '+socket._id;
        data.createTime=new Date();
        delete data[''];
        data._id= new ObjectId();
        fn(JSON.stringify({event: data.event,msgId:data.msgId,tripMessage:data}));
        dataStr=JSON.stringify(data);
        //io.to("trip_"+data.tripId).emit('tripMessage',dataStr);
        client.lpush('trip_message_list_'+data.tripId,dataStr);
        if(data.type=="client"){
            publishClient.publish("user_"+data.driverId,JSON.stringify({
                event: 'tripMessage',
                msgId:generateMsgId(),
                tripMessage:data}));
            pushNotification(data.message,null,socket.trip.driver.fcmToken,socket.trip.driver.iosToken);
        }
        else{
            publishClient.publish("user_"+data.clientId,JSON.stringify({
                event: 'tripMessage',
                msgId:generateMsgId(),
                tripMessage:data}));
            pushNotification(data.message,null,socket.trip.client.fcmToken,socket.trip.client.iosToken);
        }

    }

    function startTrip(data,fn) {
        
        var tag = "startTrip "+ socket._id+" ";
        data.updateTime=new Date();
        delete data[''];
        //dataStr=JSON.stringify(data);
        fn(JSON.stringify({event: data.event,msgId:data.msgId,trip:data}));
        var tripRoom ="trip_"+data._id;
        //client.hset('user_trip_hash',data.clientId,dataStr);
        //client.hset('user_trip_hash',data.driverId,dataStr);
        //client.lpush('trip_list_'+data._id,dataStr);
        //io.to("trip_"+data._id).emit('onDriverStartTrip',dataStr);
        publishClient.publish("user_"+data.clientId,JSON.stringify({
            event: 'onDriverStartTrip',
            msgId:generateMsgId(),
            trip:data}));
        if(socket.trip.client.fcmToken!=null)
            pushNotification('The driver start the trip',null,socket.trip.client.fcmToken,socket.trip.driver.iosToken);
        socket.trip={ ...socket.trip, ...data };
        client.lpush('trip_list_'+data._id,JSON.stringify(socket.trip));
    }

    function finishTrip(data,fn) {
        
        var tag = "finishTrip "+ socket._id+" ";
        data.updateTime=new Date();
        delete data[''];
        //dataStr=JSON.stringify(data);
        //client.lpush('trip_list_'+data._id,dataStr);
        socket.trip={ ...socket.trip, ...data };
        client.lpush('trip_list_'+data._id,JSON.stringify(socket.trip));
        saveTrip(data.driverId,data.clientId,data._id);
        var tripRoom ="trip_"+data._id;
        //io.to("trip_"+data._id).emit('onDriverFinishTrip',dataStr);
        fn(JSON.stringify({event: data.event,msgId:data.msgId,trip:data}));
        publishClient.publish("user_"+data.clientId,JSON.stringify({
            event: 'onDriverFinishTrip',
            msgId:generateMsgId(),
            trip:data}));
        if(socket.trip.client.fcmToken!=null)
                pushNotification('The driver finish the trip',null,socket.trip.client.fcmToken,socket.trip.driver.iosToken);
        delete socket.trip; socket.trip={};
    }


    function disconnect() {
        console.log(socket._id,"disconnect");
        if(socket._id!=null){
            client.del("user_"+socket._id+'.info'/*,"user_"+socket._id+'.zones'*/,"user_"+socket._id+'.zone',"user_"+socket._id+'.location');

            if(socket.latitude!=null&&socket.longitude!=null){
                if(socket.type == 'driver'){
                    // var zones = getDriverZones(socket.latitude,socket.longitude);
                        //console.log('location zone :',zones);
                    if(socket.zones!=null){
                        for(i=0;i<socket.zones.length;i++){
                                publishClient.publish(socket.zones[i],JSON.stringify(
                                    {event:'onOffDriverLocation',msgId:generateMsgId(),_id: socket._id}
                                ));
                        }
                    }
                    //client.del("user_"+data.driverId+'.zones');
                    if(socket.zoneCode!=null&&socket.zoneCode!=""){
                        publishClient.publish(socket.zoneCode+"_admin",JSON.stringify({event:'onOffDriverLocation',msgId:generateMsgId(),_id: socket._id}));
                    }
                    publishClient.publish("super_admin",JSON.stringify({event:'onOffDriverLocation',msgId:generateMsgId(),_id: socket._id}));
                }
                else if(socket.type == 'client'){
                    //var zone=getClientZone(socket.latitude,socket.longitude);
                    //client.del("user_"+data._id+'.zone');
                    if(socket.zoneCode!=null&&socket.zoneCode!=""){
                        publishClient.publish(socket.zoneCode+"_admin",JSON.stringify({event:'onOffClientLocation',msgId:generateMsgId(),_id: socket._id}));
                    }
                    publishClient.publish("super_admin",JSON.stringify({event:'onOffClientLocation',msgId:generateMsgId(),_id: socket._id}));
                }
                
            }
        }
        subscribeClient.UNSUBSCRIBE();
        //publishClient.quit();
        //subscribeClient.quit();

    }



    function generateMsgId(){
        return Math.random().toString().replace("0.","MSG")+Math.random().toString().replace("0.","");
    }

    function saveTrip(driverId,clientId,tripId){
        client.del("user_"+driverId+".trip");
        client.del("user_"+clientId+".trip");
        MongoClient.connect(url,{ useNewUrlParser: true,useUnifiedTopology: true  }, function(err, db) {
            if (err)
                console.log("saveTrip",err);
            else{
                var dbo = db.db(database);
                client.lrange('trip_list_'+tripId,0,-1,function(err,tripArr){
                    if (err)
                        console.log("saveTrip",err);
                    else if(tripArr!=null && tripArr.length>0){
                        var trip={};

                        for (var i = 0; i <tripArr.length ; i++) {
                            //console.log(tripArr[i]);
                            //var trip=JSON.parse(tripArr[i]);
                            trip2=JSON.parse(tripArr[i]);
                            delete trip2["_id"];
                            if( i == 0){
                                trip=JSON.parse(tripArr[i])
                                trip._id= new ObjectId(tripId);
                            }
                            trip[trip2.state]=trip2;

                        }
                        dbo.collection("trip").insertOne(trip,function(err, result) {if (err) console.log(err);});
                        client.del('trip_list_'+tripId);
                    }
                    client.lrange('trip_message_list_'+tripId,0,-1,function(err,messageArr){
                        if (err)
                            console.log("saveTrip",err);
                        else if(messageArr!=null && messageArr.length>0){
                            for (var i = messageArr.length-1; i >=0 ; i--) {
                                try {
                                    var message=JSON.parse(messageArr[i]);
                                    message._id= new ObjectId(message._id);
                                    //console.log(message);
                                    dbo.collection("trip_messages").insertOne(message,function(err, result) {if (err) console.log(err);});
                                } catch (e) {
                                    console.log("saveTrip",e);
                                }
                            }
                        }
                        db.close();
                        client.del('trip_message_list_'+tripId);
                    });
                });

            }
        });

    }



    function pushNotification(notification,data,fcmToken,iosToken){
        if(prod){
            if(fcmToken!=null&&fcmToken!=""){
                var message = {
                    token: fcmToken,
                    android: {
                        ttl: 3600000
                    }
                };
                if(notification!=null)
                    message.notification={body:notification,title:'Ovikl'};

                if(data!=null)
                    message.data={data:data};

                // Send a message to the device corresponding to the provided
                // registration token.
                admin.messaging().send(message)
                    .then((response) => {
                        // Response is a message ID string.
                        console.log('Successfully sent message:', response);
                    })
                    .catch((error) => {
                        console.log('Error sending message:', error);
                    });
            }
            else if(iosToken!=null&&iosToken!=""){
                jwt.sign({iss:'QJZ53N4HX7'}, privateKeyP8, { algorithm: 'ES256' ,header:{"kid" : "738WHULU76"}}, function(err, token) {
                    console.log(iosToken);
                    const http2Client = http2.connect('https://api.push.apple.com');
                    http2Client.on('error', (err) => console.error(err));
                    const buffer = JSON.stringify({ "aps" : { "alert" : notification} });
                    const req = http2Client.request({ 
                        [http2.constants.HTTP2_HEADER_SCHEME]: "https",
                        [http2.constants.HTTP2_HEADER_METHOD]: http2.constants.HTTP2_METHOD_POST,
                        [http2.constants.HTTP2_HEADER_PATH]: '/3/device/'+iosToken,
                        "apns-topic":"com.caoutch.transnet",
                        "apns-expiration": 1 ,
                        "apns-priority": 10 ,
                        "authorization": "bearer "+ token,
                        "Content-Type": "application/json",
                        "Content-Length": buffer.length,
                
                    });
                
                    req.on('response', (headers, flags) => {
                    for (const name in headers) {
                        console.log(`${name}: ${headers[name]}`);
                    }
                    });
                
                    req.setEncoding('utf8');
                    let data = '';
                    req.on('data', (chunk) => { data += chunk; });
                    req.write(buffer);
                    req.on('end', () => {
                        console.log(`\n${data}`);
                        http2Client.close();
                        
                    });
                    req.end();
                
                
                
                  });
            }
        }
    }
  
}

