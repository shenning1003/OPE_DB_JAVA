# Introduction
This is a working prototype that implements a new scheme aims to protect the query completeness. It uses Order-Preserving-Encryption (OPE) algorithm to encrypt data and generate fake tuples. By inserting fake tuples into real data, it can verify query completeness by analyzing if all expected fake tuples are returned.

The working prototype uses an OPE algorithm which is a modified version from the CryptDB OPE implementaion. The CryptDB can be found at: 
> https://github.com/CryptDB/cryptdb.

This new scheme is able to re-generate fake tuples from query result directly. Besides encryption keys, the client doen't require to remember any other data. 

# How to Run this program
1. First, compile the OPE implementation (c++). To compile, go to the the crypto folder and run: 
> g++ -std=c++0x -I./../../ntl/include -L./../../ntl/lib -L./../../openssl/lib -I./../../openssl/include -I./../ ope.cc hgd.cc -lcrypto -lssl -lntl -lgmp -lm

The program works fine on Ubuntu 14.01 LTS. It may have problems to compile on other linux platform.

2. Goes to **OPE.java**, set the variable 'OPE_location' as the location of the compiled output file. (in this case, it will be 'replace_folder_location\a.out' ) 
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
        
 5. Run **run.java**. First time running it, paramater '-i' is needed to initialize the OPE database (Create a new database, encrypt plaintext using OPE, and insert fake tuples). Otherwise, just run **run.java** without any paramaters.
 6. Once you type a valid MySQL query, the program will return query result, and verify query completeness by analyzing if all the expected fake tuples are returned. 
