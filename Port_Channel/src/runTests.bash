#!/bin/bash

################################################
# Runs a set of test cases for the Java socket #
# implementation of the VirtualClass program.  #
# Output of programs is stored in three sub-   #
# directories of the current working directory #
# with each folder containing a file for each  #
# process's output.                            #
# @author Franklin D. Worrell                  #
# @version 4 November 2017                     #
################################################

################################################
# Tests for Java Socket implementation.        #
################################################

# First Socket test 
mkdir socket_test1
java virtualclass.VirtualClass S 8082 2 25 > ./socket_test1/virtualclass.txt & java virtualclass.Student S 0 10 localhost 8082 > ./socket_test1/student0.txt & java virtualclass.Student S 1 20 localhost 8082 > ./socket_test1/student1.txt 

# Second test
mkdir socket_test2
java virtualclass.VirtualClass S 8084 4 20 > ./socket_test2/virtualclass.txt & java virtualclass.Student S 0 12 localhost 8084 > ./socket_test2/student0.txt & java virtualclass.Student S 1 12 localhost 8084 > ./socket_test2/student1.txt & java virtualclass.Student S 2 12 localhost 8084 > ./socket_test2/student2.txt & java virtualclass.Student S 3 12 localhost 8084 > ./socket_test2/student3.txt 

# Third test
mkdir socket_test3 
java virtualclass.VirtualClass S 8086 15 15 > ./socket_test3/virtualclass.txt & java virtualclass.Student S 0 2 localhost 8086 > ./socket_test3/student0.txt & java virtualclass.Student S 1 4 localhost 8086 > ./socket_test3/student1.txt & java virtualclass.Student S 2 4 localhost 8086 > ./socket_test3/student2.txt & java virtualclass.Student S 3 4 localhost 8086 > ./socket_test3/student3.txt & java virtualclass.Student S 4 5 localhost 8086 > ./socket_test3/student4.txt & java virtualclass.Student S 5 6 localhost 8086 > ./socket_test3/student5.txt & java virtualclass.Student S 6 10 localhost 8086 > ./socket_test3/student6.txt & java virtualclass.Student S 7 10 localhost 8086 > ./socket_test3/student7.txt & java virtualclass.Student S 8 10 localhost 8086 > ./socket_test3/student8.txt & java virtualclass.Student S 9 7 localhost 8086 > ./socket_test3/student9.txt & java virtualclass.Student S 10 11 localhost 8086 > ./socket_test3/student10.txt & java virtualclass.Student S 11 5 localhost 8086 > ./socket_test3/student11.txt & java virtualclass.Student S 12 13 localhost 8086 > ./socket_test3/student12.txt & java virtualclass.Student S 13 11 localhost 8086 > ./socket_test3/student13.txt & java virtualclass.Student S 14 13 localhost 8086 > ./socket_test3/student14.txt

################################################
# Tests for Java NIO implementation.           #
################################################

#First NIO test
mkdir nio_test1
java virtualclass.VirtualClass N 8088 2 25 > ./nio_test1/virtualclass.txt & java virtualclass.Student N 0 10 localhost 8088 > ./nio_test1/student0.txt & java virtualclass.Student N 1 20 localhost 8088 > ./nio_test1/student1.txt 

# Second NIO test
mkdir nio_test2
java virtualclass.VirtualClass N 8092 4 20 > ./nio_test2/virtualclass.txt & java virtualclass.Student N 0 12 localhost 8092 > ./nio_test2/student0.txt & java virtualclass.Student N 1 12 localhost 8092 > ./nio_test2/student1.txt & java virtualclass.Student N 2 12 localhost 8092 > ./nio_test2/student2.txt & java virtualclass.Student N 3 12 localhost 8092 > ./nio_test2/student3.txt 

# Third NIO test
#mkdir nio_test3 
#java virtualclass.VirtualClass N 9094 15 15 > ./nio_test3/virtualclass.txt & java virtualclass.Student N 0 2 localhost 9094 > ./nio_test3/student0.txt & java virtualclass.Student N 1 4 localhost 9094 > ./nio_test3/student1.txt & java virtualclass.Student N 2 4 localhost 9094 > ./nio_test3/student2.txt & java virtualclass.Student N 3 4 localhost 9094 > ./nio_test3/student3.txt & java virtualclass.Student N 4 5 localhost 9094 > ./nio_test3/student4.txt & java virtualclass.Student N 5 6 localhost 9094 > ./nio_test3/student5.txt & java virtualclass.Student N 6 10 localhost 9094 > ./nio_test3/student6.txt & java virtualclass.Student N 7 10 localhost 9094 > ./nio_test3/student7.txt & java virtualclass.Student N 8 10 localhost 9094 > ./nio_test3/student8.txt & java virtualclass.Student N 9 7 localhost 9094 > ./nio_test3/student9.txt & java virtualclass.Student N 10 11 localhost 9094 > ./nio_test3/student10.txt & java virtualclass.Student N 11 5 localhost 9094 > ./nio_test3/student11.txt & java virtualclass.Student N 12 13 localhost 9094 > ./nio_test3/student12.txt & java virtualclass.Student N 13 11 localhost 9094 > ./nio_test3/student13.txt & java virtualclass.Student N 14 13 localhost 9094 > ./nio_test3/student14.txt
