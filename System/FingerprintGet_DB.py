#import mysql.connector
import time
import os
import MySQLdb
import shutil
import phalange
import binascii
import sys
#sys.path.append('/SmartBar/Fingerprint_Code/pyGT511C3-master/pyGT511C3')
import FPS
import usbcomm as USB
import Control_IO as CIO


queue = phalange.phalangeList()
fps = None
def DB_connect():
    db = MySQLdb.connect("192.254.232.41","treeloy_admin","Smartbar2014","treeloy_smartbar" )
    return db

def fps_init():
    print 'initializing fingerpirnt scanner'
    fps =  FPS.FPS_GT511C3(device_name='/dev/ttyAMA0',baud=9600,timeout=2,is_com=False)
    fps.UseSerialDebug = True
    fps.SetLED(False)
    FPS.delay(1)
    fps.DeleteAll()
    print 'done.'
    return fps
#fps = fps_init()

def fpget_init():
    print 'initializing Fingers file'
    if not os.path.exists('/tmp/Fingers'):
        os.mkdir('/tmp/Fingers')
        print 'Fingers created'
    else:
        print 'Fingers exists'
    print 'done.'
#fpget_init()
def fpput_init():
    if not os.path.exists('/tmp/ToServer'):
        os.mkdir('/tmp/ToServer')
        print 'ToServer created'
    else:
        print 'ToServer exists'

def update_queue(string):
    fps.SetLED(True)
    sList = []; toRemove = []
    ctime = time.time()
    clear_files()
    sList = get_files(string)
    if sList != 0:
        del sList[0]
        for i in range(len(sList)):
            if not sList[i]:
                del sList[i]
        sList = map(int, sList)
        toRemove = queue.refill(sList)
        queue.printall()
    populate_scanner()
    for i in range(len(toRemove)):
        fps.DeleteID(toRemove[i])
    ctime = time.time() - ctime
    print 'finished in ' + str(ctime) + 's'
    fps.SetLED(False)
    return 1

def populate_scanner():
    for i in range(len(queue.phalangelist)):
        if queue.phalangelist[i].scannerID == None:
            print 'setting' + str(queue.phalangelist[i].fileID)
            set_template(queue.phalangelist[i].fileID)
    

def get_files(string):
    sList = string.split(',')
    print sList
    db = DB_connect()
    cursor = db.cursor()
    print 'getting files'
    for i in range(1,len(sList)):
        #fizzle = 'SmartBar/' + sList[i]
        fizzle = sList[i]
        sqlStr = "SELECT fingerprint FROM users WHERE userPin = '%s'" % (fizzle)
        try:
           # Execute the SQL command
           cursor.execute(sqlStr)
           # Commit your changes in the database
           db.commit()
        except Exception as e:
           # Rollback in case there is any error
           db.rollback()
           print "Error: unable to put fingerprint: " + fizzle
           print e

        # Fetch all the rows in a list of lists.
        for row in cursor.fetchall() :
            fp = row[0]
            if fp:
                outputFile = '/tmp/Fingers/' + fizzle
                with open(outputFile, 'wb') as output:
                    output.write(fp)
    db.close()
    return sList

def clear_files():
    shutil.rmtree('/tmp/Fingers')
    fpget_init()

def clearput_files():
    shutil.rmtree('/tmp/ToServer')

def save_template(fileID, scannerID):
    fpput_init()
    db = DB_connect()
    cursor = db.cursor()
    ctime = time.time()
    rp = fps.GetTemplate(scannerID)
    outputFile = '/tmp/ToServer/' + str(fileID)
    with open(outputFile, 'wb') as output:
        output.write(rp.RawBytes[12:])
    varbin_value = open(outputFile, 'rb').read()
    sqlStr = "UPDATE users SET fingerprint = '%s' WHERE userPin = '%s'" % (varbin_value, str(fileID))
    try:
       # Execute the SQL command
       cursor.execute(sqlStr)
       # Commit your changes in the database
       db.commit()
    except Exception as e:
       # Rollback in case there is any error
       db.rollback()
       print "Error: unable to put fingerprint"
       print e
    db.close
    ctime = time.time() - ctime
    print 'finished in ' + str(ctime) + 's'
    clearput_files()
    #print binascii.hexlify(rp.RawBytes[12:])

def set_template(fileID):
    ID=0;count=0
    unavail=True
    prevcount=fps.GetEnrollCount()
    #increment through IDs in chronological order until one is available
    while unavail==True:
       unavail=fps.CheckEnrolled(ID)
       if(unavail==True):
           ID+=1
    inputFile = '/tmp/Fingers/' + str(fileID)
    try:
        byte = open(inputFile, 'rb').read()
    except:
        queue.assign_scannerID(fileID,-1)
        print 'finger not available'
        return
    fps.SetTemplate(byte,ID,0)
    queue.assign_scannerID(fileID,ID)
    #print binascii.hexlify(byte)
    return 1
#function which handles ID/verify process
#Identify returns the matched ID#
#Verify Error Codes:
#   0-Correct Finger   1-Invalid Position    2-ID not in use   3-False
def identify(ldev):
    fps.SetLED(True)
    ctime = time.time()
    print 'Place finger on sensor for identification, please'
    #wait for the user to place their finger on the scanner
    while fps.IsPressFinger()==False:
        newtime = time.time()-ctime
        FPS.delay(.5)
        if (newtime) > 5.0:
            print '5 seconds'
            USB.writebuf(ldev,'$FPIDEN,ENDED')
            fps.SetLED(False)
            return 
        
    USB.writebuf(ldev,'$FPIDEN,WORKING')
    #capture an image and check if it is in the database
    fps.CaptureFinger(False)
    ID=fps.Identify1_N()#Verify1_1(0)
    #time.sleep(2)

    #print 'ERR %d' % ID
    #if the user exists and the ID# does not exceed 200, welcome. else, enroll
    if ID<200:
        print 'ID:'+ str(ID)
        print 'Welcome, User #%d'% (queue.returnfileID(ID))
        #return queue.phalangelist[ID].fileID
        USB.writebuf(ldev,('$FPIDEN,SUCC,'+str(queue.returnfileID(ID))))
    else:
        print 'User not found'
        USB.writebuf(ldev,'$FPIDEN,ERR,NOTFOUND')
        time.sleep(1)
    fps.SetLED(False)
    return

#function which contains entire enrollment process
#thoughts: consider making subfunction for each enroll to reduce reduncy and increase readability
#Enroll Error Codes:
#   0-ACK   1-Enrollment Failed    2-Bad Finger    3-ID in use
def enroll_ID(fileID,ldev):
##    if queue.returnscannerID(fileID) == -1:
##        print 'already exists'
##        return
    ctime = 0
    ID=0;count=0
    unavail=True
    prevcount=fps.GetEnrollCount()
    #increment through IDs in chronological order until one is available
    while unavail==True:
       unavail=fps.CheckEnrolled(ID)
       if(unavail==True):
           ID+=1

    fps.EnrollStart(ID)

    
    #wait for finger to be present on device
    FPS.delay(1)
    print 'Enrollment takes three scans.'
    print 'Place finger on sensor, please, user # %d' % (ID+1)
    USB.writebuf(ldev,'$FPENROLL,EN1')
    fps.SetLED(True)
    ctime = time.time()
    if fps.IsPressFinger():
        FPS.delay(.5)
    while fps.IsPressFinger()==False:
        newtime = time.time()-ctime
        FPS.delay(.5)
        if (newtime) > 5.0:
            print '5 seconds'
            USB.writebuf(ldev,'$FPENROLL,EN1,TIMEOUT')
            fps.SetLED(False)
            return

    capt=fps.CaptureFinger(True)
    captErr=0

    #if capture was successful, proceed to enroll1
    if capt==True:
        print 'Please remove finger'
        USB.writebuf(ldev,'$FPENROLL,RM1')
        captErr=fps.Enroll1()
        #print 'Err Status: %d' % captErr
        captErr=0

        #wait for finger to be removed from device
        while fps.IsPressFinger()==True:
            FPS.delay(1)

        print 'Place the same finger a second time'
        USB.writebuf(ldev,'$FPENROLL,EN2')
        #wait for finger to be present again
        ctime = time.time()
        while fps.IsPressFinger()==False:
            newtime = time.time()-ctime
            FPS.delay(.5)
            if (newtime) > 5.0:
                print '5 seconds'
                USB.writebuf(ldev,'$FPENROLL,EN2,TIMEOUT')
                fps.SetLED(False)
                return

        capt=fps.CaptureFinger(True)

        if capt==True:
            print 'Please remove finger'
            USB.writebuf(ldev,'$FPENROLL,RM2')
            captErr=fps.Enroll2()
            #print 'Err Status: %d' % captErr
            captErr=0

            while fps.IsPressFinger()==True:
                FPS.delay(1)

            print 'Place the same finger a third time'
            USB.writebuf(ldev,'$FPENROLL,EN3')
            ctime = time.time()
            while fps.IsPressFinger()==False:
                newtime = time.time()-ctime
                FPS.delay(1)
                if (newtime) > 5.0:
                    print '5 seconds'
                    USB.writebuf(ldev,'$FPENROLL,EN3,TIMEOUT')
                    fps.SetLED(False)
                    return

            capt=fps.CaptureFinger(True)

            if capt==True:
                print 'Please remove finger'
                USB.writebuf(ldev,'$FPENROLL,RM3')
                captErr=fps.Enroll3()
                #print 'Err Status: %d' % captErr
                currcount=fps.GetEnrollCount()
                if captErr==0:
                    if currcount!=prevcount:
                        print 'You have successfully been enrolled'
                        #queue.assign_scannerID(fileID, currcount)
                        save_template(fileID, ID)
                        if queue.assign_scannerID(fileID,ID) == -1:
                            fps.DeleteID(ID)
                            stng = ',NOTINQ'
                        else:
                            stng = ',ADDEDTOQ'
                        fps.SetLED(False)
                        return '$FPENROLL,SUCC,' + str(fileID) + stng
                    else:
                        print 'Enrollment unsuccessful. Error code: 2'
                        fps.SetLED(False)
                        return '$FPENROLL,ERR,2'
                else:
                    print 'Enrollment unsuccessful. Error code: %d' % captErr
                    #enroll(fps)
                    fps.SetLED(False)
                    return '$FPENROLL,ERR.' + str(captErr)
            else:
                print 'Enrollment unsuccessful, did not capture third finger.'
                fps.SetLED(False)
                return '$FPENROLL,ERR,3'
        else:
            print 'Enrollment unsuccessful, did not capture second finger.'
            fps.SetLED(False)
            return '$FPENROLL,ERR,2'
    else:
        print 'Enrollment unsuccessful, did not capture first finger.'
        fps.SetLED(False)
        return '$FPENROLL,ERR,1'

'''
fps = fps_init()
fpput_init()
fpget_init()
update_queue('$fp,16505559898,10000002108')
'''
