# ovikl
https://ovikl.com is opensource alternative to Uber,Lyft,Careem and Bolt

* Installing NGINX (optional in development)
https://www.nginx.com/resources/wiki/start/topics/tutorials/install/

* Installing MongoDB
https://docs.mongodb.com/manual/installation/

* Installing Redis
https://redis.io/topics/quickstart

* Installing Node.js
https://nodejs.org/en/download/
https://nodejs.org/en/download/package-manager/

* Download code from Github.

* Run Ovikl server in local machine
```
npm update
```
* Copy ovikl-html to NGINX www path or use below Node.js script
```
node 80.js 
```

* Change the path of uploaded images
index.js:
```javascript
if(!prod){
    constant.uploadPath= '../ovikl-html/images';
}
```
```
node tcp.js 3000 dev
node index.js 8080 dev
```

* Run android application in emulator is default in localhost. 
Create google-services.json from https://console.firebase.google.com/u/0/project/_/settings/general and replace app/google-services.json.

* To run in device as Debug, change the dev IPs to server ip
Constants class:
```java
public static final String dev_url="http://10.0.2.2";
public static final String dev_nodejs_index_url="http://10.0.2.2:8080";
```

* Run iOS application in simulator is default in localhost. 

* To run in device as Debug, change the dev IPs to server ip
Constants class:
```swift
    static let indexUrlDev="http://localhost:8080"
    static let urlDev="http://localhost"
```

* Support:
support@ovikl.com
