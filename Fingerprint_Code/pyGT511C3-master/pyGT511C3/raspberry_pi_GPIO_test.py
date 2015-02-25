'''
Code altered and drastically modified as of 02/22/2015

@author: Molly Graham

This program is being modified to design and test fingerprint functions to be carried onto final project

    Original created on 08/04/2014

    @author: jeanmachuca

    SAMPLE CODE:

    This script is a test for device connected to GPIO port in raspberry pi 

    For test purpose:

    Step 1:
    Connect the TX pin of the fingerprint GT511C3 to RX in the GPIO

    Step 2:
    Connect the RX pin of the fingerprint GT511C3 to TX in the GPIO 

    Step 3: 
    Connect the VCC pin of the fingerprint GTC511C3 to VCC 3,3 in GPIO

    Step 4: 
    Connect the Ground pin of fingerprint GT511C3 to ground pin in GPIO


    This may be works fine, if don't, try to change the fingerprint baud rate with baud_to_115200.py sample code


'''
import FPS, sys, time

#function which contains entire enrollment process
#thoughts: consider making subfunction for each enroll to reduce reduncy and increase readability
#Enroll Error Codes:
#   0-ACK   1-Enrollment Failed    2-Bad Finger    3-ID in use
def enroll(fps):
    ID=0;count=0
    unavail=True

    #increment through IDs in chronological order until one is available
    while unavail==True:
       unavail=fps.CheckEnrolled(ID)
       if(unavail==True):
           ID+=1

    fps.EnrollStart(ID)

    print 'Enrollment takes three scans.'
    print 'Place finger on sensor, please, user # %d' % (ID+1)

    #wait for finger to be present on device
    while fps.IsPressFinger()==False:
        FPS.delay(1)

    capt=fps.CaptureFinger(False)
    captErr=0

    #if capture was successful, proceed to enroll1
    if capt==True:
        print 'Please remove finger'
        captErr=fps.Enroll1()
        print 'Err Status: %d' % captErr
        captErr=0

        #wait for finger to be removed from device
        while fps.IsPressFinger()==True:
            FPS.delay(1)

        print 'Place the same finger a second time'

        #wait for finger to be present again
        while fps.IsPressFinger()==False:
            FPS.delay(1)

        capt=fps.CaptureFinger(False)

        if capt==True:
            print 'Please remove finger'
            captErr=fps.Enroll2()
            print 'Err Status: %d' % captErr
            captErr=0

            while fps.IsPressFinger()==True:
                FPS.delay(1)

            print 'Place the same finger a third time'

            while fps.IsPressFinger()==False:
                FPS.delay(1)

            capt=fps.CaptureFinger(False)

            if capt==True:
                print 'Please remove finger'
                captErr=fps.Enroll3()
                print 'Err Status: %d' % captErr
                captErr=0
                if captErr==0:
                    print 'You have successfully been enrolled'
                else:
                    print 'Enrollment unsuccessful. Error code: %d' % captErr
                    #enroll(fps)
            else:
                print 'Enrollment unsuccessful, did not capture third finger.'
        else:
            print 'Enrollment unsuccessful, did not capture second finger.'
    else:
        print 'Enrollment unsuccessful, did not capture first finger.'

        
#function which handles ID/verify process
#Identify returns the matched ID#
#Verify Error Codes:
#   0-Correct Finger   1-Invalid Position    2-ID not in use   3-False
def identify(fps,mode,num):
    print 'Place finger on sensor for identification, please'

    #wait for the user to place their finger on the scanner
    while fps.IsPressFinger()==False:
        FPS.delay(1)

    if mode==0:
        #capture an image and check if it is in the database
        fps.CaptureFinger(False)
        ID=fps.Identify1_N()#Verify1_1(0)
	#time.sleep(2)

	#print 'ERR %d' % ID
	#if the user exists and the ID# does not exceed 200, welcome. else, enroll
        if ID<200:
            print 'Welcome, User #%d'% (ID+1)
        else:
            print 'User not found'
            time.sleep(1)
            enroll(fps)
    elif mode==1:
        #capture an image and check if it is in the database
        fps.CaptureFinger(False)
        ID=fps.Verify1_1(num)
	#time.sleep(2)

	#print 'ERR %d' % ID
	#if the user exists and the ID# does not exceed 200, welcome. else, enroll
        if ID==1:
            print 'Welcome, User #%d'% (num+1)
        else:
            print 'User mismatch, check database for a match'
            time.sleep(1)
            identify(fps,0,0)


if __name__ == '__main__':
    fps =  FPS.FPS_GT511C3(device_name='/dev/ttyAMA0',baud=9600,timeout=2,is_com=False)
    fps.UseSerialDebug = True
    fps.SetLED(True) # Turns ON the CMOS LED
    FPS.delay(2) # wait 1 second
    #fps.DeleteAll()
    #fps.DeleteID(0)
    #enroll(fps)
    identify(fps,1,0)
    enrollcount=fps.GetEnrollCount()
    print 'Enroll count: %d' % enrollcount

##    print 'Put your finger in the scan'
##    counter = 0 # simple counter for wait 10 seconds
##    while counter < 10:
##        if fps.IsPressFinger():  #verify if the finger is in the scan
##            print 'Your finger is in the scan'
##            fps.SetLED(False) # Turns OFF the CMOS LED
##            break
##        else:
##            FPS.delay(1) #wait 1 second
##            counter = counter + 1
    
    fps.Close() # Closes serial connection
    pass

