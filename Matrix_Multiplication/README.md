# Pipeline in Networks: Matrix Multiplication by Blocks 
# Franklin D. Worrell 

## Description 
This project implements a mesh of circular pipelines to perform distributed 
matrix multiplication. An `Initiator` process creates the multiplicands and 
breaks them into blocks. It then sends a block of each multiplicand to each 
`Worker` in the network. 


## Implementation Details 
This iteration of the program uses Java sockets to implement the mesh of 
circular pipelines. A future iteration will include an implementation using 
Java's NIO API. For ease of implementing the matrix multiplication by blocks 
algorithm, the program requires that square matrices of equal size be used 
as the multiplicands. By default, the second multiplicand is the identity 
matrix. Additionally, the number of workers must be some `n` such that for 
some integer `m`, `n = (m * 2) ^ 2`. Hopefully, by the time the NIO 
implementation is complete, at least one of these restrictions will be 
lightened. 

The initial shifting of each multiplicand required by this style of matrix 
multiplication is performed by the `Initiator` before the blocks are 
distributed amongst the `Worker` nodes. The algorithm provided by the 
course textbook is followed. 


## To Compile and Run

### To Compile
From the `src` directory, to compile the code, run: 
`
javac matrix/*.java block_multiplier/*.java
`
The `Initiator` and the `Worker` programs must be run independently. See 
the next section for details on how to run three tests and for a discussion 
of their results. 

### To Run a Test Distributed in Provided VMs
1. Login to the VM with number 202--this is the VM that will run the Initiator
node. Compile the code and from the `src` directory: `java block_multiplier.Initiator 16 4 9181` 
2. Login to another VM. Run a Worker node from the `src` directory: 
`java block_multiplier.Worker 0 8190 137.30.123.202 9181` 
3. Login to a third VM. Run another Worker node from the `src` directory: 
`java block_multiplier.Worker 1 8192 137.30.123.202 9181`
4. Login to a fourth VM. Run another Worker node from the `src` directory: 
`java block_multiplier.Worker 2 8194 137.30.123.202 9181` 
5. Login to a fifth VM. Run another Worker node from the `src` directory: 
`java block_multiplier.Worker 3 8196 137.30.123.202 9181` 


### To Run Tests Locally 
To run the first test, a small matrix with only four workers, from a shell 
in the src folder, run: 
`
mkdir TestOne 
java block_multiplier.Initiator 4 4 9181 > ./TestOne/initiator.txt & java block_multiplier.Worker 0 8190 localhost 9181 > ./TestOne/worker0.txt & java block_multiplier.Worker 1 8192 localhost 9181 > ./TestOne/worker1.txt & java block_multiplier.Worker 2 8194 localhost 9181 > ./TestOne/worker2.txt & java block_multiplier.Worker 3 8196 localhost 9181 > ./TestOne/worker3.txt
`
The results will be located in a `TestOne` folder located in the `src` 
directory. These results are discussed below. 

To run a second test that multiplies two bigger matrices but still only uses 
a few `Worker` nodes, from a terminal in `src`, run: 
`
mkdir TestTwo 
java block_multiplier.Initiator 128 4 9181 > ./TestTwo/initiator.txt & java block_multiplier.Worker 0 8190 localhost 9181 > ./TestTwo/worker0.txt & java block_multiplier.Worker 1 8192 localhost 9181 > ./TestTwo/worker1.txt & java block_multiplier.Worker 2 8194 localhost 9181 > ./TestTwo/worker2.txt & java block_multiplier.Worker 3 8196 localhost 9181 > ./TestTwo/worker3.txt 
`
The results will be located in a new `TestTwo` directory located in `src`, 
and the results of this test are discussed below. 

Finally, a third test that uses two larger multiplicands and more `Worker` 
nodes can be executed by running from a terminal in `src`: 
`
mkdir TestThree 
java block_multiplier.Initiator 128 16 9181 > ./TestThree/initiator.txt & java block_multiplier.Worker 0 8190 localhost 9181 > ./TestThree/worker0.txt & java block_multiplier.Worker 1 8192 localhost 9181 > ./TestThree/worker1.txt & java block_multiplier.Worker 2 8194 localhost 9181 > ./TestThree/worker2.txt & java block_multiplier.Worker 3 8196 localhost 9181 > ./TestThree/worker3.txt & java block_multiplier.Worker 4 8198 localhost 9181 > ./TestThree/worker4.txt & java block_multiplier.Worker 5 8200 localhost 9181 > ./TestThree/worker5.txt & java block_multiplier.Worker 6 8202 localhost 9181 > ./TestThree/worker6.txt & java block_multiplier.Worker 7 8204 localhost 9181 > ./TestThree/worker7.txt & java block_multiplier.Worker 8 8206 localhost 9181 > ./TestThree/worker8.txt & java block_multiplier.Worker 9 8208 localhost 9181 > ./TestThree/worker9.txt & java block_multiplier.Worker 10 8210 localhost 9181 > ./TestThree/worker10.txt & java block_multiplier.Worker 11 8212 localhost 9181 > ./TestThree/worker11.txt & java block_multiplier.Worker 12 8214 localhost 9181 > ./TestThree/worker12.txt & java block_multiplier.Worker 13 8216 localhost 9181 > ./TestThree/worker13.txt & java block_multiplier.Worker 14 8218 localhost 9181 > ./TestThree/worker14.txt & java block_multiplier.Worker 15 8220 localhost 9181 > ./TestThree/worker15.txt 
`
Again, this test's results are summarized below and its output will located 
in `TestThree` inside the `src` directory. 


## Discussion of Tests
All three tests have some things in common. First, the output from each node 
in the network will redirect its output from `stdout` to a unique file in 
the directory associated with its test. The output from both `Initiator` and 
`Worker` is ample and includes information about its configuration, the 
intermediate states of the matrix multiplication after each cycle of shift 
and compute partial product is performed, and other information about its 
functionality and performance. Additionally, the `Initiator` reports the 
length of time the total computation takes and the length of time for each 
of the phases of the algoritm. 

Each test should report that the computation was performed successfully in 
the output from the the `Initiator` node--this will be contained in the 
`initiator.txt` file in each test result directory. The multiplicands and 
the product are reported in this same file. Each `Worker` reports the block 
it received and used to compute a partial product at each stage in the shift 
and compute cycle. 

If you desire to change the type of the multiplicands--i.e., "display", 
identity, or random. By default, the first multiplicand is a "display" 
matrix, and the second multiplicand is an identity matrix. This makes 
tracking the origin of some errors easier. 
