## To Compile and Run 
To compile, from the `src` directory: `javac app/*.java connections/*.java model/*.java semaphore/*.java utilities/*.java` 

## Test Using VMs Provided by Department

### Test 1--Two `Bidder` nodes, synchronous bidding
1. Login to the VM with number 202--this is the VM that will run the Auctioneer
node. Compile the code and from the `src` directory: `java app.Auctioneer 8091 8081 2 10`
2. Login to another VM. Run a Bidder (client) node from the `src` directory: 
`java app.Bidder 0 8093 137.30.123.202 8091 3 1.00` 
3. Login to a third VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 1 8095 137.30.123.202 8091 6 2.00` 

### Test 2--Two `Bidder` nodes, simultaneous bidding, same amount 
1. Login to the VM with number 202--this is the VM that will run the Auctioneer
node. Compile the code and from the `src` directory: `java app.Auctioneer 8091 8081 2 5`
2. Login to another VM. Run a Bidder (client) node from the `src` directory: 
`java app.Bidder 0 8093 137.30.123.202 8091 3 1.00` 
3. Login to a third VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 1 8095 137.30.123.202 8091 3 1.00` 

### Test 3--Four `Bidder` nodes, simultaneous bidding, different amounts 
1. Login to the VM with number 202--this is the VM that will run the Auctioneer
node. Compile the code and from the `src` directory: `java app.Auctioneer 8091 8081 4 15` 
2. Login to another VM. Run a Bidder (client) node from the `src` directory: 
`java app.Bidder 0 8093 137.30.123.202 8091 3 1.00` 
3. Login to a third VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 1 8095 137.30.123.202 8091 3 1.01`
4. Login to a fourth VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 2 8097 137.30.123.202 8091 3 0.59` 
5. Login to a fifth VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 3 8099 137.30.123.202 8091 3 1.65` 

### Test 4--Four `Bidder` nodes, continuous bidding, bad semaphore usage. 
1. Login to the VM with number 202--this is the VM that will run the Auctioneer
node. Compile the code and from the `src` directory: `java app.Auctioneer 8091 8081 4 15` 
2. Login to another VM. Run a Bidder (client) node from the `src` directory: 
`java app.Bidder 0 8093 137.30.123.202 8091 3 1.00 vtest` 
3. Login to a third VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 1 8095 137.30.123.202 8091 3 1.01 vtest`
4. Login to a fourth VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 2 8097 137.30.123.202 8091 3 0.59 vtest` 
5. Login to a fifth VM. Run another Bidder node from the `src` directory: 
`java app.Bidder 3 8099 137.30.123.202 8091 3 1.65 vtest` 

