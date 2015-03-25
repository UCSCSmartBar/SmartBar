#!/usr/bin/python

import MySQLdb

# Open database connection
db = MySQLdb.connect("192.254.232.41","treeloy_admin","Smartbar2014","treeloy_smartbar" )

# prepare a cursor object using cursor() method
cursor = db.cursor()

# Inventory
invNew = "$IV,2,2@0,WH,1,52.2,59.2@1,QQ,1,49.8,59.2@2,TO,1,118.5,128.0@3,HH,1,123.0,128.0"

# Prepare SQL query to INSERT a record into the database.
sql = "UPDATE inventory SET inv = '%s'" % (invNew)

try:
   # Execute the SQL command
   cursor.execute(sql)
   # Commit your changes in the database
   db.commit()
except:
   # Rollback in case there is any error
   db.rollback()
   print "Error: unable to put inventory"

# disconnect from server
db.close()
