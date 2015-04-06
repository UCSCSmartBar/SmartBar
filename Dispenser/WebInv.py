#!/usr/bin/python

import MySQLdb
def GetInventory():
   # Open database connectio
   print('hello')
   db = MySQLdb.connect("192.254.232.41","treeloy_admin","Smartbar2014","treeloy_smartbar" )

   # prepare a cursor object using cursor() method
   cursor = db.cursor()

   # Prepare SQL query to INSERT a record into the database.
   sql = "SELECT inv FROM inventory"

   try:
      print('Yo')
      # Execute the SQL command
      cursor.execute(sql)
      # Commit your changes in the database
      db.commit()
   except:
      # Rollback in case there is any error
      db.rollback()
      print "Error: unable to get inventory"
      
   # Fetch all the rows in a list of lists.
   for row in cursor.fetchall() :
       inv = row[0]
       print "%s" % inv
   # disconnect from server
   db.close()
   return inv

   
def PutInventory(inventory_string):
   # Open database connection
   db = MySQLdb.connect("192.254.232.41","treeloy_admin","Smartbar2014","treeloy_smartbar" )

   # prepare a cursor object using cursor() method
   cursor = db.cursor()

   # Inventory
   invNew = inventory_string

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
