# Introduction
This program is a working prototype that implements a new scheme aims to protect the query completeness. It uses Order-Preserving_Encryption algorithm(OPE) to encrypt data and generate fake tuples. By inserting fake tuples into real data, it can verify query completeness by analyzing if all expected fake tuples are returned.

We didn't implement a new OPE implementation here. We extract the OPE implement from CryptDB and make some small changes on that. The CryptDB can be found at: 
> https://github.com/CryptDB/cryptdb.

This new scheme is able to re-generate fake tuples from query result directly. Besides encryption keys, the client isn't required to remember any other data. 

# How to Run this program
1. First, compile the OPE implementation (c++) from **CryptDB**. To compile, go to the the crypto folder and run: 
> g++ -std=c++0x -I./../../ntl/include -L./../../ntl/lib -L./../../openssl/lib -I./../../openssl/include -I./../ ope.cc hgd.cc -lcrypto -lssl -lntl -lgmp -lm

It works find on Ubuntu 14.01 LTS. It may have problem on other linux platform.

2. Goes to **OPE.java**, set the variable 'OPE_location' as the location of the compiled output file. (in this case, will be 'replace_folder_location\a.out' ) 
3. Download the employee sample database from 
> https://github.com/datacharmer/test_db. 
Import this database into your MySQL server and configure your own jdbc connections in **DB_connection.java** and **OPE_DB.java**
4. To encrypt data and insert fake tuples, users must specify keys in file **keys.txt**. The structure of the keys are defined as follows:

        TABLE  table_name   num_of_fake_tuple
        COLUMN column_name  domainBit,   rangeBit,  data_key,  fake_tuple_key,  fake_tuple_start_index,  fake_tuple_domainBit
        COLUMN column_name  domainBit,   rangeBit,  data_key,  fake_tuple_key,  fake_tuple_start_index,  fake_tuple_domainBit
        ................
        ................
        
 For example, for the table "employee", the keys looks like:
 
        TABLE OPE_EMPLOYEE 100
        COLUMN birth_date 27 60 123456 654321 2260 14
        COLUMN emp_no 20 60 123456 654321 1 8
        COLUMN first_name 50 60 123456 654321 1 7
        COLUMN last_name 50 60 123456 654321 1 7
        COLUMN gender 2 50 123456 654321 1 7
        COLUMN hire_date 27 60 123456 654321 2360 14
        
 5. Run **run.java**. First time running it, paramater '-i' is need to initialize the OPE database (Create a new database, encrypt plaintext data using OPE, and insert fake tuples). Otherwise, just run **run.java** without any paramaters.
 6. Once you type a valid MySQL query, the program will return query result, and predict query completeness by analyzing if all the fake tuples are returned. 
