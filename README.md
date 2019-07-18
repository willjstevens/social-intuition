![Social Intuition](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/cover-image.png)

Social Intuition
=
Track your predictions about school events, sports, entertainment. 
Capture and track your intuitions. Browse community or private intuitions. Set outcomes and vote on others intuitions. Track your accuracy.

DISCLAIMER #1 - DEPRECATED and INACTIVE
-
SocialIntuition.co has been shutdown and all resources have been fully decommissioned as of July, 2019.

This repository is no longer active. 

This repository is not supported.

DISCLAIMER #2 - LEGAL and LICENSE
-

#### If you choose to fork it, you are subject to the attached license. 

#### If you choose to fork and use it, you are subject and responsible for any security and faulty issues. 
 
#### The repository owner is in **_NO WAY_** responsible or liable for any loss.  

#### The repository owner is in **_NO WAY_** responsible or liable for any assumed damage. 

## The long narrative of Social Intuition
**See the screenshots below for visuals.**

Social Intuition lets you capture and track your intuitions. How accurate are you?

Intuitions are like predictions, paired with outcomes. They may be serious or silly. In Social Intuition you capture and track your intuitions and set the outcome once it occurs. But be honest. This will adjust your accuracy percentage positively or negatively. 

The community also contributes public intuitions for you to browse, vote and watch for the outcome. Add cohorts (or friends) to follow one another. Cohorts will set theirs and you can like/comment, and make a predicted outcome against others. 

You may even capture your own personal or private intuitions without disclosing it to the community. 

## What is in this repository
A running web application, written on a Java platform. This is an _app_-level repository.

It does not include any of the _non_-app resources such as database scripts or images in this README. You will find those in the supporting [Social Intuition Resources](https://github.com/willjstevens/social-intuition-resources) repository. 

## The supporting, resources repository 
The supporting [Social Intuition Resources](https://github.com/willjstevens/social-intuition-resources) repository contains the following _non_-app resources.

 - Artwork (logos, branding) 
 - AWS deployment and build scripts, configuration
 - Database scripts
 - iOS assets
 - Images in this README

## Architecture
The diagram below illustrates the architecture of the Social Intuition server and web client application, running in AWS.

(Deployed 2015)

![Architecture](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/aws-architeture.png)

## Technologies
 - Java
 - Angular
 - Bootstrap, with heavily customized styles, fonts, images
 - SASS
 - MongoDB
 - Spring
 - AWS Java Client
 
See the POM file for a full list of server-side technologies. 

## Requirements

### Containerized environment
Social Intuition (SI) server was built to run inside AWS ECS (Elastic Constainer Service.)  ECS came before Kubernetes; but Kubernetes would work as well. 

See the diagram below describing the original architecture. 

### Cloud provider
This was intended to be run taking advantage of a leading cloud provider such as AWS, which offers a myriad of cloud services. Though, this could be fairly easily ported over to other providers.  You will need to follow its instructions for deploying, configuring, hosting, etc.. 

### Frontend clients
There were two frontend clients for Social Intuition
 1. Web client: The Angular/Bootstrapped, SPA-based client, embedded within this repository and served up from this server-side client. 
    - This is a mobile-friendly, responsive web design client.
 2. iOS mobile client: The Swift-based iOS client, which may be found in the [Social Intuition iOS](https://github.com/willjstevens/social-intuition-ios) repository.

### Domain name
A domain name will be required for running this web application. The app would need to be re-branded. There would need to be several renaming in user-facing messages and copy text, as well as code-level variable namings. 

### SSL
This web application requires a CA-granted SSL certificate, for a given domain. It needs to be installed into your cloud platform per its instructions. 

### Database
This application uses a document-oriented NoSQL for storing data and configuration. MongoDB is recommended. 

### API Service endpoints
This server-side application also hosts endpoints for mobile apps to hit. They hit essentially the same endpoint but with minor device or web session request headers to accommodate different types of clients. Though the internal service calls and logic is the same. 

See the [Social Intuition iOS](https://github.com/willjstevens/social-intuition-ios) repository for the iOS mobile client. 


## Screenshots and showcase 

### Home Page Carousel - 1 - Desktop
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-1.png)

### Home Page Carousel - 1 - Mobile
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-1-mobile.png)

### Home Page Carousel - 2
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-2.png)

### Home Page Carousel - 3
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-3.png)

### Home Page Carousel - 4
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-4.png)

### Home Page Carousel - 5
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-5.png)

### Home Page Carousel - 6
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-6.png)

### Home Page Carousel - 7
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-banner-7.png)

### Home Page - How it Works
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-how-it-works.png)

### Home Page - Features
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/home-features.png)

### Add an Intuition
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/add-intuition.png)

### Login
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/login.png)

### Registration
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/registration.png)

### Intuition 1
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/intuition-1.png)

### Intuition 2
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/intuition-2.png)

### Intuition Score
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/score.png)

### User Profile
![](https://raw.githubusercontent.com/willjstevens/social-intuition-resources/master/screenshots/website/profile.png)

