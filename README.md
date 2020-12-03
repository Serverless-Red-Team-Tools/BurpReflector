# Burp Reflector

This tool is composed of an extension of the Burp Suite software, in which we will detect all those requests that are made to an HTTP API Gateway.

The differentiating character of this plugin is that the infrastructure supported behind this plugin is serverless that allows us to deploy it in cloud systems easily and depends on the use that we can give it in some systems such as AWS (the one we have used) is covered by your Free Tier cape.

Next we are going to show how the service is set up in Amazon Web Services, for this we can visualize the following graph:


![Schema](https://raw.githubusercontent.com/Serverless-Red-Team-Tools/BurpReflector/main/Media/Schema.png)



### Installation

Burp Reflector requires that you download the burp suite software (https://portswigger.net/burp) to install this extension.

In this repository you have a .jar library where you have this plugin with the name "BurpReflector.jar" , when you open the burpsuite tool you should see the option "Extender" and then you can pulse in ADD button to add a jar where we have our Burp Reflector:

![ExtenderView](https://raw.githubusercontent.com/Serverless-Red-Team-Tools/BurpReflector/main/Media/ExtenderView.png)

Once the Burp Reflector tab appears in the top bar, you can access to add the configuration to connect to your websocket:

![ConfigView](https://raw.githubusercontent.com/Serverless-Red-Team-Tools/BurpReflector/main/Media/ConfigView.png)

Once we have configured the address of our websocket and we have connected with it, we can make requests to the HTTP API and view it in the Proxy view:

![ProxyView](https://raw.githubusercontent.com/Serverless-Red-Team-Tools/BurpReflector/main/Media/ProxyView.png)


### Deploy in AWS

Coming soon... 


