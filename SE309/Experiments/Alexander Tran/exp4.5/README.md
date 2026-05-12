SpringBoot version 2.4.0
JAVA JDK version 11

# SpringBoot JPA: One to One relation example
### One to One relations
In object-oriented programming, real-life entities are represented as classes, and in MySQL, they are stored as tables. JPA/Hibernate is a framework that matches a Class to Table(
each variable represents a column) to ease the manipulation of data in Java. Entities almost always interact with one another and establish individual relationships among 
themselves Eg: A Person will have exactly one social security number(One to One), A person can have multiple cars(One to Many), etc. This example is created with the assumption
that one person can have only one laptop(made by poor people). It shows how a one to one relation can be represented between entities using JPA/Hibernate.
### Pre-requisites

0. Go through the springboot_unit1_2_hellopeople
1. Maven has to be installed on command line OR your IDE must be configured with maven
2. Java version 1.8 - 1.10 (Some versions of springboot are really unhappy with java 11)

### To Run the project 
1. Command Line (Make sure that you are in the folder containing pom.xml)</br>
<code> mvn clean package</code></br>
<code>java -jar target/onetoone-1.0.0.jar</code>
2. IDE : Right click on Application.java and run as Java Application

### Available End points from POSTMAN: CRUDL
1. CREATE requests - 

1. POST request: 
    1. /reviews - Create a new Review. The request has to be of type application/JSON input format
    {
    "mediaType": "MOVIE",
    "title": "Whiplash",
    "rating": 5,
    "body": "Insane pacing and editing. Loved it."
    }

    2. /users - Create a new user WITHOUT a review. Request Format : application / JSON
    {
    "name": "Alex Tran",
    "emailId": "trana25@iastate.edu"
    }

2. READ requests -
GET request:
    1. /reviews/type/{mediaType} - Get all reviews of a specific media type
        EXAMPLE:
       /reviews/type/MOVIE
       /reviews/type/SHOW
       /reviews/type/MUSIC
   2. /users/{id} - Get User object for provided id.
   3. /reviews/{id} - Get Review object for provided id

3. UPDATE requests -
PUT request : 
    1. /users/{id} - Update User information for id provided in the URL.
       {
       "id": 1,
       "name": "Alexander Tran",
       "emailId": "alex@iastate.edu",
       "active": true
       }

    2. /reviews/{id} - Update Review information
       {
       "id": 2,
       "mediaType": "MUSIC",
       "title": "Deftones - White Pony",
       "rating": 5,
       "body": "Timeless album. Atmosphere is unreal."
       }

   3. /reviews/{reviewId}/user/{userId} - Assign a Review to a User.
      EXAMPLE:
      /reviews/1/user/1


4. DELETE a record - 
 DELETE request:
    1. /users/{id} - Delete the User with id provided in the URL
    2. /reviews/{id} - Delete the Review with id provided in the URL

5. LIST all -  
GET request
    1. /users - Get/List all the Reviews stored in the database
    2. /reviews - Get/List all the Users stored in the database along with their associated Reviews

6. Other end points:
GET request:  /oops   --- this shows you what happens when your code throws an exception.

#

### Note :
### 1. /laptops APIs are defined in Laptop/LaptopController, 
### 2. /Persons API is defined in the Person/PersonController

# 
## Some helpful links:
[SpringBoot Entity](https://www.baeldung.com/jpa-entities)   
[SpringBoot OneToOne](https://www.baeldung.com/jpa-one-to-one)    
[Email Regex](https://www.baeldung.com/java-email-validation-regex)

### Version Tested

|IntelliJ  | Project SDK | Springboot | Maven  |
|----------|-------------|------------|--------|
|  2025.3  |     17      | 3.4.3      | 3.9.11 |

