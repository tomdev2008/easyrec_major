JMeter Tests for easyrec
========================
In this file the  JMeter tests provided for evaluating easyrec's performance, are shortly described.
It is assumed that the reader is familiar with JMeter, Appache's load test framework, which can be found/downloaded at 
http://jakarta.apache.org/jmeter/

Note, the following scripts are tested for JMeter versions 2.3.2, 2.3.3 and 2.3.4.

How to use
----------
a) Install JMeter and get familiar with it
b) Load one of the provided scripts
c) Adapt the variables to your environment (e.g. host, nrUserThreads, ...)
d) Start the scripts
e) Analyse the results


Test Scripts
------------
Currently the following scripts are provided

1. csvInput.jmx: Reads action data from a csv file and sends this information to easyrec (currently as a buy action)
2. testActionsOnly: Only REST-API action methods (e.g. buy, rate, ...) are tested
3. testAll: Easch REST-API method is called
4. testRankingsOnly: Only the ranking methods of the REST-API are tested
5. testRecsOnly: Only the recommendation methods of the REST-API are tested

Note: Only the methods of the REST-API are covered by these scripts


Inside Scripts
--------------
Basically, the scripts simulate a client application acessing easyrec. Most parameters for controlling the scripts are 
defined in the 'Variables' section. So, in most cases, only these variables must be adapted to your local settings.

The following parameters have to be adapted in most cases: 

apiKeyValue ... the necessary API key of your easyrec installation (e.g. 22b3ffa573652655d5bd27da2a39fbb7)
tenantIdValue ... the tenantId you want to test (e.g. EASYREC_DEMO)
host ... the name/address of the easyrec server (e.g. SAT003)
path ... the web-app path (e.g. /easyrec-web/)

nrUserThreads ... defines the number of threads, performing the API calls in parallel. A thread can be seen as a user of the client app

Test Data
---------
Two CSV files are provided for your tests

1. ml100k.csv ... the 100k ratings of movielens ignoring the rating and time (userId, itemId, itemDescription)
2. ml1mio_rating5_as_buy.csv ... all ratings with value 5 of the 1 million movielens sample (226k ratings!)

Note: The ml1mio_rating5_as_buy.csv data is used to test the ARM
