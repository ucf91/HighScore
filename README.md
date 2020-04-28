# Quick notes regarding my implementation:
- All of the code base has been developed from scratch in Java purely without any third party libraries taking into
consideration aspects of making it extensible and loosely coupled
- tests were more focused on the required logic so I skipped low level unitary tests for the developed components 
like route handling and dependency injections , etc..
I made all my tests as an Integration tests covering all possible scenarios as much as possible since I've more control over my InMemory collections but 
of course in real live project low level unitary tests are essential and of course the testing code needs some refactorings and should be better .

# Technologies:
- Java 11+ 
- JDK 12
- Junit 5 (Testing)
- Apache HTTP Client (Testing)




# Instructions:
To run the application just type this command on the root folder containing the jar:

java -jar GameScoreTest-1.0.jar


# End Points :
- GET /users/{userId}/login (login)
- POST /levels/{levelId}/score?sessionkey={sessionkey} (post user's score to level )
- GET /levels/{levelId}/highscorelist (get high score list for level )

# Curl call samples :
curl --location --request GET 'localhost:8081/users/11/login'

Response :
I4NY8



curl --location --request POST 'localhost:8081/levels/6/score?sessionkey=I4NY8' \
--header 'Content-Type: text/plain' \
--data-raw '7000'



curl --location --request GET 'localhost:8081/levels/6/highscorelist'
Response:
11=7000,4=500,2=100
