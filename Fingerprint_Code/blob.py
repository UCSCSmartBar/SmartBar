import mysql.connector
import sys
from PIL import Image
import base64
import cStringIO
import PIL.Image

#Database connection info
db = mysql.connector.connect(user='treeloy_admin', password='Smartbar2014',
                              host='192.254.232.41',
                              database='treeloy_smartbar')

# Put fingerprints
image = Image.open('C:\images.jpg')
blob_value = open('C:\images.jpg', 'rb').read()
sql = 'UPDATE users SET fingerprint = %s' % (blob_value)    
args = (blob_value, )
cursor=db.cursor()
cursor.execute(sql,args)

# Get fingerprints
pin = '0001110000'
sql1='SELECT fingerprint FROM users WHERE userPin = %s' % (pin)
db.commit()
cursor.execute(sql1)
data=cursor.fetchall()
print type(data[0][0])
file_like=cStringIO.StringIO(data[0][0])
# Might not need if not images
img=PIL.Image.open(file_like)
img.show()

db.close()
