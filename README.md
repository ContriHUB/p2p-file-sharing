# p2p-file-sharing

###How To Use

##Basic Program Compilation
    1. javac -cp . ./Main/Main.java ./discovery/*.java ./discovery/messages/*.java ./p2p/*.java ./testing/*.java
        run the above command after navigating to src folder
    2. java -cp . Main.Main central <!CentralServerIP!> <!CentralServerPort!>
    3. java -cp . Main.Main peer <!PeerIP!> <!PeerPort!> <!CentralServerIP!> <!CentralServerPort!>

##How To Upload a File
    1.run the peer command
    2. upload <!FILEPATHINOS!>
##How To Download a File
    1. run the peer command
    2. download <!FILE HASH!>

##How to Test
    1. in the test directory add a file with paths of all the files
    2. java -cp . Main.Main testing <!test Namwe!>
        Currently it will use three node setup and test it

