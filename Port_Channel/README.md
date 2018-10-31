# Port-Channel Implementation: Virtual Classroom
# Franklin D. Worrell


## Description 
This program simulates a virtual classroom program. In this iteration,
the Teacher begins lecturing once all Students have entered the class. 
The Teacher continues to lecture until a Student raises her or his 
hand, at which point, the Teacher acknowledges the Student in front of
the class, and the Student is allowed to ask her question in front of
the entire class. This iteration of the assignment uses only Java 
Sockets. 


## Implementation Details
Multiple different subclasses of Message are implemented. The run-time 
type of a Message instance is used by the Teacher in determining how to 
react to the message and by the Students to track the class's progress. 
When a HandRaise message is received from a Student, the Teacher 
broadcasts to the class (i.e., to every Student in the VirtualClass) and 
gives the Student permission to ask a question. The Student then sends a 
Question message to the Teacher, who broadcasts the text of the Question 
to every student before answering the question. At present, the question 
is randomly selected from a small bank of questions and the Teacher's 
answer is not tailored to the question asked. After a lecture of 
user-selected length completes, the Teacher dismisses the class and the 
program terminates gracefully. The Teacher is created and started by the 
VirtualClass since there is only one Teacher per lecture.  

Teacher and the classes that compose it constitute three threads in the
NIO implementation and n + 2 threads in the socket implementation. Effectively,
the Teacher manages what information is being transmitted to Students, the Port
thread manages the queue of Messages coming from the Students, and the 
remaining thread(s) listen for incoming messages from the students. In the NIO 
implementation, this is a single additional thread, since it can monitor the 
keys returned from the Selector. In the socket implementation, there must be 
a listener thread for each Student--one for each open client socket. 

Students are automated in this implementation. When a Student process is run, 
the time at which the Student will ask a question is provided by the user as 
a command-line argument. While not very realistic, this facilitate simulation 
of the real-life case of Students asking questions at roughly the same time. 
Namely, this tested the ability of the Teacher to queue a message and handle 
it at a later time. 


## To Compile and Run

### To Compile
From the src directory enter the following command in a
terminal window: `javac port_channel/*.java virtualclass/*.java portecho_nio/*.java`

### To Run Distributed Tests on Provided VMs 
#### Test 1, Socket implementation
1. Login to the VM with number 202--this is the VM that will run the VirtualClass 
and Teacher node. Compile the code and from the `src` directory: 
`java virtualclass.VirtualClass S 8082 2 25`
2. Login to another VM. Run a Student node from the `src` directory: 
`java virtualclass.Student S 0 10 137.30.123.202 8082`
3. Login to a third VM. Run another Student node from the `src` directory: 
`java virtualclass.Student S 1 20 137.30.123.202 8082` 

#### Test 1, NIO implementation 
1. Login to the VM with number 202--this is the VM that will run the VirtualClass 
and Teacher node. Compile the code and from the `src` directory: 
`java virtualclass.VirtualClass N 8088 2 25`
2. Login to another VM. Run a Student node from the `src` directory: 
`java virtualclass.Student N 0 10 137.30.123.202 8088`
3. Login to a third VM. Run another Student node from the `src` directory: 
`java virtualclass.Student N 1 20 137.30.123.202 8088` 

#### Test 2, Socket implementation  
1. Login to the VM with number 202--this is the VM that will run the VirtualClass 
and Teacher node. Compile the code and from the `src` directory: 
`java virtualclass.VirtualClass S 8084 4 20` 
2. Login to another VM. Run a Bidder (client) node from the `src` directory: 
`java virtualclass.Student S 0 12 137.30.123.202 8084` 
3. Login to a third VM. Run another Bidder node from the `src` directory: 
`java virtualclass.Student S 1 12 137.30.123.202 8084`
4. Login to a fourth VM. Run another Bidder node from the `src` directory: 
`java virtualclass.Student S 2 12 137.30.123.202 8084` 
5. Login to a fifth VM. Run another Bidder node from the `src` directory: 
`java virtualclass.Student S 3 12 137.30.123.202 8084` 

#### Test 2, NIO implementation 
1. Login to the VM with number 202--this is the VM that will run the VirtualClass 
and Teacher node. Compile the code and from the `src` directory: 
`java virtualclass.VirtualClass N 8092 4 20` 
2. Login to another VM. Run a Bidder (client) node from the `src` directory: 
`java virtualclass.Student N 0 12 137.30.123.202 8092` 
3. Login to a third VM. Run another Bidder node from the `src` directory: 
`java virtualclass.Student N 1 12 137.30.123.202 8092`
4. Login to a fourth VM. Run another Bidder node from the `src` directory: 
`java virtualclass.Student N 2 12 137.30.123.202 8092` 
5. Login to a fifth VM. Run another Bidder node from the `src` directory: 
`java virtualclass.Student N 3 12 137.30.123.202 8092` 

#### Test 3 
This test is too unwieldy to run in the distributed VMs without a script. Hence, 
no distributed testing directions are given for EITHER the Socket or NIO 
implementations. 


### To Run Tests Locally 
From the src directory, make executable and then execute the runTest.bash 
script:
`
chmod 755 runTests.bash
./runTests.bash
`
The output can be found in the new tests directory adjacent to src 
(i.e., sharing the same parent directory as src). There will be two
subdirectories in tests and each of these will have three further 
subdirectories. 


## Discussion of Tests
For all tests, certain parts of the output will match. The output for 
each Student in the class and the VirtualClass for each test is written
to a unique file. The output for each Student within a test should be 
roughly identical with the only variations being at the beginning and 
the end of the class when the Student is reporting joining and leaving 
the lecture. The output from VirtualClass roughly reports some of the 
internal operations of the Teacher. It is of little interest except in the
event of a runtime error. The output from the socket tests and the NIO 
tests should basically match with the exception of some beginning and 
ending bookkeeping output. 

In the first test case, the output should reflect the logic of the 
classroom in an orderly fashion. In this case, the number of students is
small--only 2--and they ask questions one after the other. Hence, there is 
no confusion in associating HandRaises and Questions. The output for both 
of the 2 students should reflect this. 

The second test is a set of four students, all of whom ask a question at 
the same time in the lecture. Since the order in which the Teacher receives 
the HandRaise Messages cannot be guaranteed to match the order in which she
receives the Question Messages, the Teacher must ensure that Student whose
HandRaise she acknowledges is actually the Student whose Question gets 
asked. This is verified by printing the ID of the Student when the 
HandRaise is acknowledged and again when the Question is broadcast. Since 
this ID is a property of the Message, this demonstrates correct matching 
of Students with their Questions. The output for all four students should 
reflect these characteristics. 

The third test is a much larger lecture hall and tests to ensure that the
Teacher class (with its Port under the hood) can handle a larger
volume of Students with more rapid-fire and some simultaneous questions. 
This test creates 15 Students and includes simultaneous questions from
a couple of Students several times. The results of the previous two tests
generalize to this case. Each Student asks his or her question in turn. 

Again, output from the NIO cases should be more-or-less the same at the 
output from the socket implementation. 
