# Orders Validator
This is a Java console application that determines which orders are valid or invalid, according to a set of rules.

## Input
A .csv file path containing the list of orders.

## Output
- accepted-broker-seq.csv: List of the broker and sequence id of accepted orders
- rejected-broker-seq.csv: List of the broker and sequence id of rejected orders
- accepted-orders.txt: List of accepted orders
- rejected-orders.txt: List of rejected orders

## Some assumptions and considerations:

### 1) Regarding the input file:
 - The first line will contain the headers
 - Will not contain duplicates records
 - Orders are organized by time stamp
 - Sequences will be in ascended order for every broker
 - expected encoding for the file: UTF-8. (A robust version may handle different encodings).
 - Time zone support is out of scope
 - Malformed records will be ignored and logged as errors
 - Well-formed records will be processed as valid or invalid

#### A well-formed record 
  - Expected fields order: Time stamp,broker,sequence id,type,Symbol,Quantity,Price,Side
  - Every field will be separated by a comma
  - Timestamp will be formatted as follows MM/d/yyyy HH:mm:ss. A 24 hours format is being used
  - String fields may be empty: broker, type, symbol, side
  - Numeric fields like quantity, price, sequence will be valid numbers
  
#### Sample csv input file:
  ```
  Time stamp,broker,sequence id,type,Symbol,Quantity,Price,Side
  10/5/2017 10:00:00,Fidelity,1,2,BARK,100,1.195,Buy
  10/5/2017 10:00:01,Charles Schwab,1,2,CARD,200,6.855,Sell
  10/5/2017 10:00:02,AXA Advisors,1,K,BRIC,5000,30.7,Sell
  ```

### 2) Valid symbols are hard-coded. 
This was for simplicity. In future versions, these symbols may be configured as an additional parameter.

### 3) Parsing the csv file is manually done. 
A future version can used a library for that purpose.

## Running this app
**Prerequisites** : maven and java 8

**Steps**:
1) Clone this repository on your local machine
2) Go to the root of the project and use maven to package the project from the command line: 
  mvn package
3) Execute the jar from the command line passing the .csv path as a parameter. For example:
  
  ```
  java -jar orders-validator-0.0.1-SNAPSHOT-jar-with-dependencies.jar C:\Users\Mirna\Desktop\trades.csv
  ```